package it.unibo.alice.tuprolog.ws.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MainRestClient {
	
	private HttpClient client = null;
	private String urlRootService = "";
	private String currentGoal = "";
	
	private String state = "";
	
	public MainRestClient() {
		client = HttpClientBuilder.create().build();
	}
	public MainRestClient(String rootURL) {
		this.urlRootService = rootURL;
		client = HttpClientBuilder.create().build();
	}
	
	public String getInfo() {
		HttpUriRequest request = RequestBuilder.get().setUri(urlRootService).build();
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		String info;
		try {
			info = readTextFromResponse(response);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		return info;
		
	}
	
	
	public List<String> getGoalsList() {
		String createdURL = urlRootService+getPathFromInfo("getGoalsListAsJSON");
		HttpUriRequest request = RequestBuilder.get().setUri(createdURL).build();
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		
		int code = response.getStatusLine().getStatusCode();
		if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("get goals list unsuccesful: "+response.toString());
		}
		
		String json;
		try {
			json = readTextFromResponse(response);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		Gson gson = new Gson();
		List<String> lista = (List<String>) gson.fromJson(json, List.class);
		return lista;
	}
	
	public String getGoal() {
		return currentGoal;
	}
	
	public String getTheory() {
		String createdURL = urlRootService+getPathFromInfo("getTheory");
		HttpUriRequest request = RequestBuilder.get().setUri(createdURL).build();
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		int code = response.getStatusLine().getStatusCode();
		if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("get theory unsuccesful: "+response.toString());
		}
		String theory;
		try {
			theory = readTextFromResponse(response);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		return theory;
	}
	
	public void setGoal(String goal) {
		List<String> lista = this.getGoalsList();
		if (lista.contains(goal))
		{
			if (!currentGoal.equals(goal))
				this.closeSession();
			this.currentGoal = goal;
		}
		else
			throw new IllegalArgumentException("Goals List doesn't contain selected goal.");
	}
	
	public String getSolution(){
		if (this.currentGoal.isEmpty())
			throw new IllegalStateException("current goal is not set");
		String createdURL = urlRootService+getPathFromInfo("getSolution");
		StringEntity goalEntity;
		try {
			goalEntity = new StringEntity(currentGoal);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set goal as body of HTTP request. Unsupported encoding.");
		}
		
		HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(goalEntity).build();
		
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		
		int code = response.getStatusLine().getStatusCode();
		if (code == Status.PRECONDITION_FAILED.getStatusCode()) {
			throw new IllegalArgumentException("Goals List doesn't contain selected goal.");
		} else if (code == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		} else if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		}
		
		String result;
		try {
			result = readTextFromResponse(response);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		return result;
	}
	
	public List<String> getSolutions(int numSolutions) {
		if (this.currentGoal.isEmpty())
			throw new IllegalStateException("currentGoal is not set");
		if (numSolutions < 1)
			throw new IllegalArgumentException("numSolutions needs to be >=1");
		String createdURL = urlRootService+getPathFromInfo("getSolutionsAsJSON")+"?n="+numSolutions;
		StringEntity goalEntity;
		try {
			goalEntity = new StringEntity(currentGoal);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set goal as body of HTTP request. Unsupported encoding.");
		}
		
		HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(goalEntity).build();
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		
		int code = response.getStatusLine().getStatusCode();
		if (code == Status.PRECONDITION_FAILED.getStatusCode()) {
			throw new IllegalArgumentException("Server response: "+response.getStatusLine().toString());
		} else if (code == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
			throw new IllegalStateException("get solutions unsuccesful: "+response.toString());
		} else if (code == Status.NOT_MODIFIED.getStatusCode()) {
			return new ArrayList<String>();
		} else if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		}
		
		String json;
		try {
			json = readTextFromResponse(response);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		Gson gson = new Gson();
		List<String> lista = (List<String>) gson.fromJson(json, List.class);
		return lista;
	}
	
	public List<String> getSolutions() {
		if (this.currentGoal.isEmpty())
			throw new IllegalStateException("currentGoal is not set");
		String createdURL = urlRootService+getPathFromInfo("getSolutionsAsJSON");
		StringEntity goalEntity;
		try {
			goalEntity = new StringEntity(currentGoal);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set goal as body of HTTP request. Unsupported encoding.");
		}
		
		HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(goalEntity).build();
		
		
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		
		int code = response.getStatusLine().getStatusCode();
		if (code == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
			throw new IllegalStateException("get solutions unsuccesful: "+response.toString());
		} else if (code == Status.NOT_MODIFIED.getStatusCode()) {
			return new ArrayList<String>();
		} else if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		}
		
		String json;
		try {
			json = readTextFromResponse(response);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		Gson gson = new Gson();
		List<String> lista = (List<String>) gson.fromJson(json, List.class);
		return lista;
	}
	
	public String getSolutionWithSession() {
		if (this.currentGoal.isEmpty())
			throw new IllegalStateException("currentGoal is not set");
		String createdURL = urlRootService+getPathFromInfo("getSolutionWithSession");
		BasicHeader contentJSONHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		
		JsonObject obj = new JsonObject();
		if (state.isEmpty())
		{
			obj.addProperty("goal", currentGoal);
			obj.addProperty("engine", "");
		} else
		{
			obj.addProperty("goal", "");
			obj.addProperty("engine", state);
		}

		StringEntity requestEntity;
		try {
			requestEntity = new StringEntity(obj.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set goal as body of HTTP request. Unsupported encoding.");
		}
		
		HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(requestEntity)
				.setHeader(contentJSONHeader).build();
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		
		int code = response.getStatusLine().getStatusCode();
		if (code == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		} else if (code == Status.PRECONDITION_FAILED.getStatusCode()) {
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		} else if (code == Status.FORBIDDEN.getStatusCode()) {
			throw new IllegalStateException("state is no more valid");
		} else if (code == Status.NOT_MODIFIED.getStatusCode()) {
			return "";
		} else if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		}
		
		String result;
		try {
			result = readTextFromResponse(response);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(result).getAsJsonObject();
		
		JsonElement solElement = o.get("solution");
		JsonElement stateElement = o.get("engine");
		
		Gson gson = new Gson();
		String solution = (String) gson.fromJson(solElement, String.class);
		state = (String) gson.fromJson(stateElement, String.class);

		return solution;
	}
	
	public List<String> getSolutionsWithSession(int numSolutions) {
		if (this.currentGoal.isEmpty())
			throw new IllegalStateException("currentGoal is not set");
		if (numSolutions < 1)
			throw new IllegalArgumentException("numSolutions needs to be >=1");
		String createdURL = urlRootService+getPathFromInfo("getSolutionsWithSession")+"?n="+numSolutions;
		BasicHeader contentJSONHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		
		JsonObject obj = new JsonObject();
		if (state.isEmpty())
		{
			obj.addProperty("goal", currentGoal);
			obj.addProperty("engine", "");
		} else
		{
			obj.addProperty("goal", "");
			obj.addProperty("engine", state);
		}

		StringEntity requestEntity;
		try {
			requestEntity = new StringEntity(obj.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set goal as body of HTTP request. Unsupported encoding.");
		}
		
		
		HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(requestEntity)
				.setHeader(contentJSONHeader).build();
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		
		int code = response.getStatusLine().getStatusCode();
		if (code == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		} else if (code == Status.PRECONDITION_FAILED.getStatusCode()) {
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		} else if (code == Status.FORBIDDEN.getStatusCode()) {
			throw new IllegalStateException("state is no more valid");
		} else if (code == Status.NOT_MODIFIED.getStatusCode()) {
			return new ArrayList<String>();
		} else if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		}

		String json;
		try {
			json = readTextFromResponse(response);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(json).getAsJsonObject();
		
		JsonElement solElement = o.get("solution");
		JsonElement stateElement = o.get("engine");
		state = stateElement.toString();
		
		Gson gson = new Gson();
		String listaString = (String) gson.fromJson(solElement, String.class);
		List<String> lista = (List<String>) gson.fromJson(listaString, List.class);
		return lista;
	}
	
	public List<String> getSolutionsWithSession() {
		if (this.currentGoal.isEmpty())
			throw new IllegalStateException("currentGoal is not set");
		String createdURL = urlRootService+getPathFromInfo("getSolutionsWithSession");
		BasicHeader contentJSONHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		
		JsonObject obj = new JsonObject();
		if (state.isEmpty())
		{
			obj.addProperty("goal", currentGoal);
			obj.addProperty("engine", "");
		} else
		{
			obj.addProperty("goal", "");
			obj.addProperty("engine", state);
		}

		StringEntity requestEntity;
		try {
			requestEntity = new StringEntity(obj.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set goal as body of HTTP request. Unsupported encoding.");
		}
		
		
		HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(requestEntity)
				.setHeader(contentJSONHeader).build();
		HttpResponse response;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		
		int code = response.getStatusLine().getStatusCode();
		if (code == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		} else if (code == Status.PRECONDITION_FAILED.getStatusCode()) {
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		} else if (code == Status.FORBIDDEN.getStatusCode()) {
			throw new IllegalStateException("state is no more valid");
		} else if (code == Status.NOT_MODIFIED.getStatusCode()) {
			return new ArrayList<String>();
		} else if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("get solution unsuccesful: "+response.toString());
		}

		String json;
		try {
			json = readTextFromResponse(response);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new IllegalStateException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		JsonParser parser = new JsonParser();
		JsonObject o = parser.parse(json).getAsJsonObject();
		
		JsonElement solElement = o.get("solution");
		JsonElement stateElement = o.get("engine");
		state = stateElement.toString();
		
		Gson gson = new Gson();
		String listaString = (String) gson.fromJson(solElement, String.class);
		List<String> lista = (List<String>) gson.fromJson(listaString, List.class);
		return lista;
	}
	
	
	
	
	
	public void closeSession() {
		state = "";
	}

	
	private String getPathFromInfo(String methodName) {
		String infoXml = getInfo();
		
		SAXBuilder builder = new SAXBuilder();
		InputStream stream;
		Document doc;
		try {
			stream = new ByteArrayInputStream(infoXml.getBytes("UTF-8"));
			doc = builder.build(stream);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("Charset UTF-8 is not supported.");
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error during parsing of the XML file.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't complete parsing of the document.");
		}

		Element methods = doc.getRootElement().getChild("Methods");
		Optional<Element> methodOpt = methods.getChildren().stream()
				.filter(methodElement -> methodElement.getChildText("Name").equals(methodName)).findFirst();
		if (methodOpt.isPresent())
		{
			Element method = methodOpt.get();
			String url = method.getChildText("URL");
			String[] pieces = url.split("/main");
			String toReturn = pieces[1];
			return toReturn;
		}
		return null;
	}
	
	
	private String readTextFromResponse(HttpResponse response) throws UnsupportedOperationException, IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ((line = rd.readLine()) != null) {
			builder.append(line+"\n");
		}
		rd.close();
		return builder.toString();
	}

}
