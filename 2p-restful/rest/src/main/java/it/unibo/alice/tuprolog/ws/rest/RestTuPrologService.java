package it.unibo.alice.tuprolog.ws.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jose4j.lang.JoseException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoMoreSolutionException;
import alice.tuprolog.SolveInfo;
import it.unibo.alice.tuprolog.ws.persistence.StorageService;
import it.unibo.alice.tuprolog.ws.core.PrologSolution;
import it.unibo.alice.tuprolog.ws.core.StatelessEngine;

/**
 * This is the component that implements the public part of the server's user interface. 
 * As such, it contains methods to access some of the the configurations of the server and,
 * more important, methods to request the resolution (with or without session state support)
 * of a goal from the goalList.</br>
 * The methods don't require any authentication, and therefore no login method is provided.</br>
 * All the public methods are exposed to the client as RESTful web services.
 * 
 * @author Andrea Muccioli
 *
 */
@Path("/main")
@Stateless
public class RestTuPrologService {
	
	@EJB
	private StatelessEngine engine;
	
	@EJB
	private it.unibo.alice.tuprolog.ws.security.SecurityManager security;
	
	@EJB
	private StorageService manager;
	
	@PostConstruct
	private void setup() {
		
	}
	
	/**
	 * Gets a XML representation of the service containing all the methods with their simpleName,
	 * the MIME types of what they consume and produce, the relative URL,the HTTP method and, if present,
	 * the query parameters needed to invoke the respective REST service.
	 * 
	 * @return the String containing the service representation as XML.
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getInfo() {
		Element root = new Element("Service");
		Document doc = new Document();
		Class service = this.getClass();
		String classPath = ((Path)service.getAnnotation(Path.class)).value();
		
		Element serviceName = new Element("Name");
		serviceName.addContent(service.getSimpleName());
		root.addContent(serviceName);
		
		Element serviceMethods = new Element("Methods");

		Method[] methods = service.getDeclaredMethods();
		for (Method cur : methods) {
			if (Modifier.isPublic(cur.getModifiers()) && !Modifier.isStatic(cur.getModifiers()))
			{
				String produces = "";
				String consumes = "";
				String url = classPath;
				String httpMethod = "";
				Annotation[] annotations = cur.getAnnotations();
				for (Annotation ann : annotations) {
					if (ann instanceof GET || ann instanceof POST || ann instanceof DELETE || ann instanceof PUT)
						httpMethod = ann.annotationType().getSimpleName();
					else if (ann instanceof Produces)
					{
						produces = Arrays.toString(((Produces)ann).value());
						
					} else if (ann instanceof Consumes)
					{
						consumes = Arrays.toString(((Consumes)ann).value());
					} else if (ann instanceof Path)
					{
						String thisPath = ((Path)ann).value();
						url +=  "/" + thisPath;
					}
				}
				
				Hashtable<String, String> dictionary = new Hashtable<String, String>();
				Annotation[][] a = cur.getParameterAnnotations();
				for(int i = 0; i < a.length; i++) {
					if (a[i].length > 0) {
			            for (Annotation anno : a[i])
			            {
			            	if (anno instanceof QueryParam)
			            	{
			            		String queryParameterValue = cur.getParameters()[i].getName();
			            		String queryParameterName = ((QueryParam)anno).value();
			            		dictionary.put(queryParameterName, queryParameterValue);
			            	}
			            }
					}
				}
				
				Element methodElement = new Element("Method");
				Element name = new Element("Name");
				Element http = new Element("HttpMethod");
				Element methodUrl = new Element("URL");
				Element queryParameters = new Element("QueryParams");
				Element producesElement = new Element("Produces");
				Element consumesElement = new Element("Consumes");
				name.addContent(cur.getName());
				http.addContent(httpMethod);
				methodUrl.addContent(url);
				producesElement.addContent(produces);
				consumesElement.addContent(consumes);
				methodElement.addContent(name);
				methodElement.addContent(http);
				methodElement.addContent(methodUrl);
				
				if (dictionary.size()>0)
				{
					dictionary.forEach((key, value) -> {
						Element queryParam = new Element("Parameter");
						Element queryName = new Element("Name");
						Element queryValue = new Element("Value");
						queryName.addContent(key.toString());
						queryValue.addContent(value.toString());
						queryParam.addContent(queryName);
						queryParam.addContent(queryValue);
						queryParameters.addContent(queryParam);
					});
				}
				methodElement.addContent(queryParameters);
				
				methodElement.addContent(producesElement);
				methodElement.addContent(consumesElement);
				serviceMethods.addContent(methodElement);
			}
			
		}
		
		root.addContent(serviceMethods);
		doc.setRootElement(root);
		XMLOutputter outter=new XMLOutputter();
		outter.setFormat(Format.getPrettyFormat());
		return outter.outputString(doc);
	}
	
	
	/**
	 * Gets the theory currently set
	 * 
	 * @return A String containing the theory
	 */
	@Path("theory")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getTheory() {
		return manager.getConfiguration().getTheory();
	}
	
	/**
	 * Gets the current list of possible goals and returns it as a JSON string.
	 * 
	 * @return A JSON representation of the List<String> containing the goals list.
	 */
	@Path("goalList")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGoalsListAsJSON() {
		System.out.println("Ricevuta richiesta GET getGoalListAsJSON");
		Gson gson = new Gson();
		String json = gson.toJson(manager.getConfiguration().getGoals());	
		return Response.ok().entity(json).build();
	}
	
	
	/**
	 * Redirects to the goal of the goals list with the name specified.
	 * 
	 * @param goalName : the query parameter that specifies the name of the goal to get.
	 * @param uriInfo : 
	 * @return redirects the client to an URL of type goalList/{index}
	 */
	@Path("goalList/byName")
	@GET
	public Response getGoalByName(@QueryParam("goal") String goalName, @Context UriInfo uriInfo) {
		System.out.println("Ricevuta richiesta GET getGoalByName");
		int index = manager.getConfiguration().getGoals().indexOf(goalName);
		if (index < 0)
			return Response.status(Status.PRECONDITION_FAILED)
					.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": goal is not contained in goals list.").build();
		String uriString = uriInfo.getBaseUri().toString() + "main/goalList/"+index;
		URI location = URI.create(uriString);
		return Response.seeOther(location).build();
	}
	
	
	/**
	 * Gets the goal at the index position in current list of possible goals
	 * 
	 * @param index : the index at which the goal is located in the current goals list. Must be an integer.
	 * @return the goal requested as plain text
	 */
	@Path("goalList/{n}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getGoalAtIndex(@PathParam("n") String index) {
		System.out.println("Ricevuta richiesta GET getGoalAtIndex");
		int i;
		if (index!=null)
		{
			try {
				i = Integer.parseInt(index);
			} catch (NumberFormatException e) {
				return Response.status(Status.PRECONDITION_FAILED)
						.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": index is not an int").build();
			}
			if (i<0 || i>=manager.getConfiguration().getGoals().size())
				return Response.status(Status.PRECONDITION_FAILED)
						.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": index is out of bound").build();
			return Response.ok().entity(manager.getConfiguration().getGoals().get(i)).build();
		}
		return Response.status(Status.PRECONDITION_FAILED)
				.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": index is required").build();
	}
	
	
	/**
	 * Gets the first solution using the goal provided. This method doesn't
	 * supply session state.
	 * 
	 * @param goal : the goal needs to be part of the current goals list.
	 * @return the solution as plain text.
	 */
	@Path("solution")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getSolution(String goal) {
		if (!manager.getConfiguration().getGoals().contains(goal))
		{
			return Response.status(Status.PRECONDITION_FAILED)
					.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": goal is not contained in goals list").build();
		}
		String toReturn = "";
		try {
			SolveInfo info = engine.solve(goal);
			toReturn = info.toString();
		} catch (MalformedGoalException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": malformed goal").build();
		}
		return Response.ok().entity(toReturn).build();
	}
	
	
	
	/**
	 * Gets the first n solutions using the goal provided. If less than n
	 * solutions are possible, all the possible solutions will be returned.</br>
	 * If n is null all the solutions will be returned.</br>
	 * This method doesn't supply session state.
	 * 
	 * @param goal : the goal needs to be part of the current goals list.
	 * @param n : the number of solutions needed. If present, must be an integer. Can be null if all the solutions are needed.
	 * @return a JSON string representation of the list containing the solutions as String.
	 */
	@Path("solutions")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSolutionsAsJSON(String goal, @QueryParam("n") String n) {
		if (!manager.getConfiguration().getGoals().contains(goal))
		{
			return Response.status(Status.PRECONDITION_FAILED)
					.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": goal is not contained in goals list").build();
		}
		int numSolutions;
		List<SolveInfo> result = null;
		if (n!=null)
		{
			try {
				numSolutions = Integer.parseInt(n);
			} catch (NumberFormatException e) {
				return Response.status(Status.PRECONDITION_FAILED)
						.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": n is not an int").build();
			}
			
			try {
				result = engine.solve(goal, numSolutions);
				
			} catch (MalformedGoalException e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": malformed goal").build();
			} catch (NoMoreSolutionException e) {
				e.printStackTrace();
				return Response.status(Status.NOT_MODIFIED)
						.entity(""+Status.NOT_MODIFIED.getStatusCode()+": No more solutions").build();
			}
		}
		else
		{
			try {
				result = engine.solveAll(goal);
			} catch (MalformedGoalException e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": malformed goal").build();
			} catch (NoMoreSolutionException e) {
				e.printStackTrace();
				return Response.status(Status.NOT_MODIFIED)
						.entity(""+Status.NOT_MODIFIED.getStatusCode()+": No more solutions").build();
			}
		}
		List<String> toReturn = new ArrayList<String>();
		result.forEach(res -> toReturn.add(res.toString()));
		Gson gson = new Gson();
		String json = gson.toJson(toReturn);
		return Response.ok().entity(json).build();
		
	}
	
	/**
	 * Gets one solution using the goal and the engine state provided. It returns
	 * the first possible solution.</br>
	 * The goal is provided as property of the JSON Object with property name "goal",
	 * and the engine state as property of the same JSON Object with property name "engine".</br>
	 * At the first invocation with session, the goal must be provided and the state must not.</br>
	 * At following invocations with session, the state must be provided and the state must not.</br>
	 * This method supply session state. Consequent invocations of getSolution/s with session
	 * take account of the previous results.
	 * 
	 * @param jsonObject : a string representing a JSON Object. It can contain two properties: "goal" and "engine".
	 * @return a string representing a JSON Object. It contains two properties: "solution" containing the solution as a string
	 * and "engine" containing a JWE string representation of the engine state. The JWE string needs to be provided as it is
	 * by the client in the following invocations of the services which supports session state, if the client wants to maintain it.
	 */
	@Path("solutionWithSession")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSolutionWithSession(String jsonObject) {
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(jsonObject).getAsJsonObject();
		
		JsonElement goalElement = o.get("goal");
		JsonElement stateElement = o.get("engine");
		
		String goal = null;
		String engineToken = null;
		
		
		if (goalElement != null)
		{
			goal = goalElement.getAsString();
		}
		if (stateElement != null)
		{
			engineToken = stateElement.getAsString();
		}
		
		//Goal is set and state is not: it's the first call of the session. solve() call is needed
		if ((!goal.isEmpty() && goal != null) && (engineToken.isEmpty() || engineToken == null))
		{
			if (!manager.getConfiguration().getGoals().contains(goal))
				return Response.status(Status.PRECONDITION_FAILED)
						.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": goal is not contained in goals list").build();
			try {
				PrologSolution sol = engine.solveWithSession(goal);
				SolveInfo info = sol.getFirstInfo();
				String state = sol.getEngineState();
				String encState;
				try {
					encState = security.signAndEncryptEngine(state);
				} catch (JoseException e) {
					e.printStackTrace();
					return Response.status(Status.INTERNAL_SERVER_ERROR)
							.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": Signing/Encryption error!").build();
				}
				
				JsonObject obj = new JsonObject();
				obj.addProperty("goal", goal);
				obj.addProperty("solution", info.toString());
				obj.addProperty("engine", encState);
				
				return Response.ok().entity(obj.toString()).build();
			} catch (MalformedGoalException e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": malformed goal").build();
			}
		}//State is set and goal is not: it's not the first call of the session. solve() is already been called, solveNext() is needed
		else if ((!engineToken.isEmpty() && engineToken != null) && (goal.isEmpty() || goal == null))
		{
			String engineJson = "";
			try {
				engineJson = security.decryptAndVerifyEngine(engineToken);
			} catch (JoseException e) {
				return Response.status(Status.FORBIDDEN)
						.entity(""+Status.FORBIDDEN.getStatusCode()+": Engine decryption and/or verification failed!").build();
			}
			try {
				PrologSolution sol = engine.solveNext(engineJson);
				SolveInfo info = sol.getFirstInfo();
				String state = sol.getEngineState();
				String encState;
				try {
					encState = security.signAndEncryptEngine(state);
				} catch (JoseException e) {
					e.printStackTrace();
					return Response.status(Status.INTERNAL_SERVER_ERROR)
							.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": Signing/Encryption error!").build();
				}
				
				JsonObject obj = new JsonObject();
				obj.addProperty("solution", info.toString());
				obj.addProperty("engine", encState);
				
				return Response.ok().entity(obj.toString()).build();
			} catch (NoMoreSolutionException e) {
				e.printStackTrace();
				return Response.status(Status.NOT_MODIFIED)
						.entity(""+Status.NOT_MODIFIED.getStatusCode()+": No more solutions").build();
			}
			
		} else
		{
			return Response.status(Status.PRECONDITION_FAILED)
					.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": Only one of 'goal' or 'engine' properties must be set").build();
		}
	}
	
	/**
	 * Gets n solutions using the goal and the engine state provided. It returns
	 * the first possible n solutions, if less than n solutions are possible, all
	 * the possible solutions will be returned.</br>
	 * If n is null, all the solutions will be returned.</br>
	 * The goal is provided as property of the JSON Object with property name "goal",
	 * and the engine state as property of the same JSON Object with property name "engine".</br>
	 * At the first invocation with session, the goal must be provided and the state must not.</br>
	 * At following invocations with session, the state must be provided and the state must not.</br>
	 * This method supply session state. Consequent invocations of getSolution/s with session
	 * take account of the previous results.
	 * 
	 * @param jsonObject : a string representing a JSON Object. It can contain two properties: "goal" and "engine".
	 * @param n : the number of solutions needed. If present, must be an integer. Can be null if all the
	 * remaining solutions are needed.
	 * @return a string representing a JSON Object. It contains two properties: "solution" containing a JSON string
	 * representation of the list containing the solutions as String, and "engine" containing a JWE string
	 * representation of the engine state. The JWE string needs to be provided as it is by the client in the
	 * following invocations of the services which supports session state, if the client wants to maintain it.
	 */
	@Path("solutionsWithSession")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSolutionsWithSession(String jsonObject, @QueryParam("n") String n) {
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(jsonObject).getAsJsonObject();
		
		JsonElement goalElement = o.get("goal");
		JsonElement stateElement = o.get("engine");
		
		String goal = null;
		String engineToken = null;
		
		
		if (goalElement != null)
		{
			goal = goalElement.getAsString();
		}
		if (stateElement != null)
		{
			engineToken = stateElement.getAsString();
		}
		
		
		int numSolutions = 0;
		boolean allSolutions = true;
		if (n!=null) //n solutions
		{
			try {
				numSolutions = Integer.parseInt(n);
			} catch (NumberFormatException e) {
				return Response.status(Status.PRECONDITION_FAILED)
						.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": n is not an int").build();
			}
			allSolutions = false;
		}
		
		//Goal is set and state is not: it's the first call of the session. solve() call is needed
		if ((!goal.isEmpty() && goal != null) && (engineToken.isEmpty() || engineToken == null))
		{
			if (!manager.getConfiguration().getGoals().contains(goal))
				return Response.status(Status.PRECONDITION_FAILED)
						.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": goal is not contained in goals list").build();
			try {
				PrologSolution sol = null;
				if (allSolutions)
					sol = engine.solveAllWithSession(goal, null);
				else
					sol = engine.solveWithSession(goal, numSolutions, null);
				
				List<String> infoList = new ArrayList<String>();
				sol.getInfo().forEach(el -> infoList.add(el.toString()));
				String state = sol.getEngineState();
				
				String encState;
				try {
					encState = security.signAndEncryptEngine(state);
				} catch (JoseException e) {
					e.printStackTrace();
					return Response.status(Status.INTERNAL_SERVER_ERROR)
							.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": Signing/Encryption error!").build();
				}
				Gson gson = new Gson();
				String infoListJson = gson.toJson(infoList);
				JsonObject obj = new JsonObject();
				obj.addProperty("goal", goal);
				obj.addProperty("solution", infoListJson);
				obj.addProperty("engine", encState);
				
				return Response.ok().entity(obj.toString()).build();
			} catch (MalformedGoalException e) {
				e.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": malformed goal").build();
			} catch (NoMoreSolutionException e1) {
				e1.printStackTrace();
				return Response.status(Status.NOT_MODIFIED)
						.entity(""+Status.NOT_MODIFIED.getStatusCode()+": No more solutions").build();
			}
		}//State is set and goal is not: it's not the first call of the session. solve() is already been called, solveNext() is needed
		else if ((!engineToken.isEmpty() && engineToken != null) && (goal.isEmpty() || goal == null))
		{
			String engineJson = "";
			try {
				engineJson = security.decryptAndVerifyEngine(engineToken);
			} catch (JoseException e) {
				return Response.status(Status.FORBIDDEN)
						.entity(""+Status.FORBIDDEN.getStatusCode()+": Engine decryption and/or verification failed!").build();
			}
			try {
				
				PrologSolution sol = null;
				if (allSolutions)
					sol = engine.solveAllWithSession(null, engineJson);
				else
					sol = engine.solveWithSession(null, numSolutions, engineJson);
				
				List<String> infoList = new ArrayList<String>();
				sol.getInfo().forEach(el -> infoList.add(el.toString()));
				String state = sol.getEngineState();
				
				String encState;
				try {
					encState = security.signAndEncryptEngine(state);
				} catch (JoseException e) {
					e.printStackTrace();
					return Response.status(Status.INTERNAL_SERVER_ERROR)
							.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": Signing/Encryption error!").build();
				}
				
				Gson gson = new Gson();
				String infoListJson = gson.toJson(infoList);
				JsonObject obj = new JsonObject();
				obj.addProperty("solution", infoListJson);
				obj.addProperty("engine", encState);
				
				return Response.ok().entity(obj.toString()).build();
			} catch (NoMoreSolutionException e) {
				e.printStackTrace();
				return Response.status(Status.NOT_MODIFIED)
						.entity(""+Status.NOT_MODIFIED.getStatusCode()+": No more solutions").build();
			} catch (MalformedGoalException e1) {
				e1.printStackTrace();
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": malformed goal").build();
			}
			
		} else
		{
			return Response.status(Status.PRECONDITION_FAILED)
					.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": Only one of 'goal' or 'engine' properties must be set").build();
		}
	}

}
