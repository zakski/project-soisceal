package it.unibo.alice.tuprolog.ws.rest;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import it.unibo.alice.tuprolog.ws.core.AuthCheckInterceptor;
import it.unibo.alice.tuprolog.ws.core.RequiresAuth;
import it.unibo.alice.tuprolog.ws.core.StatelessEngine;
import it.unibo.alice.tuprolog.ws.persistence.StorageService;
import it.unibo.alice.tuprolog.ws.security.Role;


/**
 * @author Andrea Muccioli
 *
 */
@Path("/configuration")
@Stateless
@Interceptors({AuthCheckInterceptor.class})
public class RestConfigurationService {
	
	@EJB
	private StatelessEngine engine;
	
	@EJB
	private it.unibo.alice.tuprolog.ws.security.SecurityManager security;
	
	@EJB
	private StorageService manager;
	
//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
//	public String getInfo() {
//		StringBuilder sb = new StringBuilder();
//		Class service = this.getClass();
//		String classPath = ((Path)service.getAnnotation(Path.class)).value();
//		sb.append(service.getSimpleName());
//		sb.append("\n\n");
//		sb.append("Methods:\n");
//		Method[] methods = service.getDeclaredMethods();
//		for (Method cur : methods) {
//			if (Modifier.isPublic(cur.getModifiers()) && !Modifier.isStatic(cur.getModifiers()))
//			{
//				String produces = "";
//				String consumes = "";
//				String url = classPath;
//				String httpMethod = "";
//				boolean requireAuth = false;
//				Annotation[] annotations = cur.getAnnotations();
//				for (Annotation ann : annotations) {
//					String simpleName = ann.annotationType().getSimpleName();
//					if (simpleName.equals(GET.class.getSimpleName()) || simpleName.equals(POST.class.getSimpleName()) || simpleName.equals(DELETE.class.getSimpleName()))
//						httpMethod = simpleName;
//					else if (simpleName.equals(Produces.class.getSimpleName()))
//					{
//						produces = Arrays.toString(((Produces)ann).value());
//						
//					} else if (simpleName.equals(Consumes.class.getSimpleName()))
//					{
//						consumes = Arrays.toString(((Consumes)ann).value());
//					} else if (simpleName.equals(Path.class.getSimpleName()))
//					{
//						String thisPath = ((Path)ann).value();
//						url +=  "/" + thisPath;
//					} else if (simpleName.equals(RequireAuth.class.getSimpleName()))
//					{
//						requireAuth = true;
//					}
//				}
//				
//				sb.append(cur.getName()+"\n");
//				sb.append("\tHttp Method: "+httpMethod+"\n");
//				sb.append("\tURL: "+url+"\n");
//				sb.append("\tLogin Required: "+requireAuth+"\n");
//				sb.append("\tProduces: "+produces+"\n");
//				sb.append("\tConsumes: "+consumes+"\n");
//			}
//			
//		}
//
//		return sb.toString();
//	}
	
	/**
	 * Get a XML representation of the service containing all the methods with their simpleName,
	 * the MIME types of what they consume and produce, the relative URL and the HTTP method
	 * needed to invoke the respective REST service, and if the method require a login or not.
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
				boolean requireAuth = false;
				Annotation[] annotations = cur.getAnnotations();
				for (Annotation ann : annotations) {
					String simpleName = ann.annotationType().getSimpleName();
					if (simpleName.equals(GET.class.getSimpleName()) || simpleName.equals(POST.class.getSimpleName()) || simpleName.equals(DELETE.class.getSimpleName()))
						httpMethod = simpleName;
					else if (simpleName.equals(Produces.class.getSimpleName()))
					{
						produces = Arrays.toString(((Produces)ann).value());
						
					} else if (simpleName.equals(Consumes.class.getSimpleName()))
					{
						consumes = Arrays.toString(((Consumes)ann).value());
					} else if (simpleName.equals(Path.class.getSimpleName()))
					{
						String thisPath = ((Path)ann).value();
						url +=  "/" + thisPath;
					} else if (simpleName.equals(RequiresAuth.class.getSimpleName()))
					{
						requireAuth = true;
					}
				}
				
				Element methodElement = new Element("Method");
				Element name = new Element("Name");
				Element http = new Element("HttpMethod");
				Element methodUrl = new Element("URL");
				Element loginRequired = new Element("LoginRequired");
				Element producesElement = new Element("Produces");
				Element consumesElement = new Element("Consumes");
				name.addContent(cur.getName());
				http.addContent(httpMethod);
				methodUrl.addContent(url);
				loginRequired.addContent(""+requireAuth);
				producesElement.addContent(produces);
				consumesElement.addContent(consumes);
				methodElement.addContent(name);
				methodElement.addContent(http);
				methodElement.addContent(methodUrl);
				methodElement.addContent(loginRequired);
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
	 * Resets the configuration entity leaving only an empty one.
	 * 
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return
	 */
	@RequiresAuth
	@DELETE
	public Response reset(@HeaderParam("token") String token) {
		manager.resetConfiguration();
		return Response.ok().build();
	}
	
	/**
	 * Gets the theory currently set.
	 * 
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return A String containing the theory
	 */
	@Path("theory")
	@RequiresAuth
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getTheory(@HeaderParam("token") String token) {
		return Response.ok().entity(manager.getConfiguration().getTheory().toString()).build();
	}
	

	/**
	 * Sets the theory in the configuration with the one passed as argument.
	 * 
	 * @param newTheory : A String containing the theory to set.
	 * @param uriInfo
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return the URL of the service to get the newly set theory.
	 */
	@Path("theory")
	@RequiresAuth
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public Response setTheory(String newTheory, @Context UriInfo uriInfo, @HeaderParam("token") String token) {
		System.out.println("Ricevuta richiesta POST setTheory");
		manager.getConfiguration().setTheory(newTheory);
//		engine.reloadConfiguration();
		System.out.println("Request uri: " +uriInfo.getAbsolutePath());
		return Response.created(uriInfo.getAbsolutePath()).build();
	}
	
	
	/**
	 * Adds the goal passed as argument to the current list of possible goals in the configuration.
	 * 
	 * @param toAdd : the String containing the goal to add.
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return a notModified response if the goal can't be added. A ok response if the goal has
	 * been added successfully.
	 */
	@Path("goalList/add")
	@RequiresAuth
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public Response addGoal(String toAdd, @HeaderParam("token") String token) {
		System.out.println("Ricevuta richiesta POST addGoal");
		if (manager.getConfiguration().getGoals().contains(toAdd))
			return Response.status(Status.NOT_MODIFIED)
					.entity(""+Status.NOT_MODIFIED.getStatusCode()+": goal to add is already contained in goals list.").build();
		manager.getConfiguration().addGoal(toAdd);
		return Response.ok().build();
	}
	
	/**
	 * Removes the goal passed as argument from the current list of possible goals in the configuration.
	 * 
	 * @param toRemove : the String containing the goal to remove.
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return a notModified response if the goal can't be removed. A ok response if the goal has
	 * been removed successfully.
	 */
	@Path("goalList/remove")
	@RequiresAuth
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public Response removeGoal(String toRemove, @HeaderParam("token") String token) {
		System.out.println("Ricevuta richiesta POST removeGoal");
		if (!manager.getConfiguration().getGoals().contains(toRemove))
			return Response.status(Status.NOT_MODIFIED)
					.entity(""+Status.NOT_MODIFIED.getStatusCode()+": goal to remove is not contained in goals list.").build();
		manager.getConfiguration().removeGoal(toRemove);
		return Response.ok().build();
	}
	
	/**
	 * Gets the list of possible goals currently set in the configuration as a JSON.
	 * 
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return A JSON representation of the List<String> containing the goals list.
	 */
	@Path("goalList")
	@RequiresAuth
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGoalListAsJSON(@HeaderParam("token") String token) {
		System.out.println("Ricevuta richiesta GET getGoalListAsJSON");
		Gson gson = new Gson();
		String json = gson.toJson(manager.getConfiguration().getGoals());	
		return Response.ok().entity(json).build();
	}
	
	/**
	 * Gets the goal at the index position in current list of possible goals
	 * 
	 * @param index : the index at which the goal is located in the current goals list. Must be an integer.
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return the goal requested as plain text
	 */
	@Path("goalList/{n}")
	@RequiresAuth
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getGoalAtIndex(@PathParam("n") String index, @HeaderParam("token") String token) {
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
	 * Removes the goal at the index position in the current list of possible goals
	 * 
	 * @param index : the index at which the goal is located in the current goals list. Must be an integer.
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return a ok response if the goal has been removed. A precondition failed (412) response if the
	 * goal has not been removed.
	 */
	@Path("goalList/{n}")
	@RequiresAuth
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	public Response removeGoalAtIndex(@PathParam("n") String index, @HeaderParam("token") String token) {
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
			manager.getConfiguration().removeAt(i);
			return Response.ok().build();
		}
		return Response.status(Status.PRECONDITION_FAILED)
				.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": index is required").build();
	}
	
	/**
	 * Sets the list of possible goals in the configuration.
	 * 
	 * @param newGoalListAsJSON : the goals list to set in a JSON string format.
	 * @param uriInfo 
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return a created response with the URL of the service to get the newly set goals list.
	 * A precondition failed (412) if the the list provided can't be set.
	 */
	@Path("goalList")
	@RequiresAuth
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setGoalList(String newGoalListAsJSON, @Context UriInfo uriInfo, @HeaderParam("token") String token) {
		System.out.println("Ricevuta richiesta POST setGoalList");
		Gson gson = new Gson();
		try {
			List<String> lista = (List<String>) gson.fromJson(newGoalListAsJSON, List.class);
			System.out.println("Ricevuta goalsList: "+lista);
			manager.getConfiguration().setGoals(lista);
			System.out.println("Request uri: " +uriInfo.getAbsolutePath());
			return Response.created(uriInfo.getAbsolutePath()).build();
		} catch (JsonSyntaxException e) {
			return Response.status(Status.PRECONDITION_FAILED)
					.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": can't convert json to list!").build();
		}
		
	}
	
	/**
	 * Gets the configuration
	 * 
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return the configuration as String.
	 */
	@Path("configuration")
	@RequiresAuth
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getConfiguration(@HeaderParam("token") String token) {
		return Response.ok().entity(manager.getConfiguration().getConfiguration().toString()).build();
	}
	
	/**
	 * Sets the configuration
	 * 
	 * @param configuration : the configuration to set.
	 * @param uriInfo
	 * @param token : The string representing the JWT signed and encrypted by the server. Needed
	 * to authenticate and execute the method. It can be obtained with a successful login attempt.
	 * @return
	 */
	@Path("configuration")
	@RequiresAuth
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public Response setConfiguration(String configuration, @Context UriInfo uriInfo, @HeaderParam("token") String token) {
		System.out.println("Ricevuta richiesta POST setConfiguration");
		manager.getConfiguration().setConfiguration(configuration);
		System.out.println("Request uri: " +uriInfo.getAbsolutePath());
		return Response.created(uriInfo.getAbsolutePath()).build();
	}
	
	/**
	 * Tries to login providing username and password. If the login is successful the String representation
	 * of a JWE is provided to the client, that may be used in the invocations
	 * of others services that require authentication.
	 * 
	 * @param username : the username
	 * @param password : the password
	 * @return a unauthorized (401) response if the credentials are not valid. A ok response with the
	 * string serialization of a JWE, needed to authenticate in other services.
	 */
	@Path("login")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password)
	{
		if (username == null)
		{
			System.out.println("USER NULL");
			return Response.status(Status.PRECONDITION_FAILED)
				.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": Username is missing!").build();
		}
		
		if (password == null)
		{
			System.out.println("PSW NULL");
			return Response.status(Status.PRECONDITION_FAILED)
				.entity(""+Status.PRECONDITION_FAILED.getStatusCode()+": Password is missing!").build();
		}
		
		Role role = security.validate(username, password);
		if (role == null)
		{
			return Response.status(Status.UNAUTHORIZED)
				.entity(""+Status.UNAUTHORIZED.getStatusCode()+": Credentials are not valid!").build();
		}
		JwtClaims claims = security.getConfigurationClaims(username, role);
		
		System.out.println("signing...");
		String innerJwt = null;
		try {
			innerJwt = security.signClaimsAndGetSerialization(claims);
		} catch (JoseException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": claims signing failed").build();
		}
		
		System.out.println("encrypting...");
		String jweSerialization = null;
		try {
			jweSerialization = security.encryptAndGetSerialization(innerJwt);
		} catch (JoseException e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(""+Status.INTERNAL_SERVER_ERROR.getStatusCode()+": claims encryption failed").build();
		}
		
		return Response.ok().entity(jweSerialization).build();
		
	}
	
}