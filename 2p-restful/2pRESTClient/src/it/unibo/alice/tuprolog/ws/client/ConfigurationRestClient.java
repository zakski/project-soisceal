package it.unibo.alice.tuprolog.ws.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

import alice.tuprolog.Theory;

public class ConfigurationRestClient {
	private HttpClient client = null;
	private String urlRootService = "";
	
	private String authToken = "";
	
	public ConfigurationRestClient() {
		client = HttpClientBuilder.create().build();
	}
	public ConfigurationRestClient(String rootURL) {
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
			return info;
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			throw new UnsupportedOperationException("Response entity content cannot be represented as java.io.InputStream");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't create stream from response entity.");
		}
		
	}
	
	public void login(String username, String password) {
		String createdURL = urlRootService+getPathFromInfo("login");
		BasicHeader userHeader = new BasicHeader("username", username);
		BasicHeader pswHeader = new BasicHeader("password", password);
		HttpUriRequest request = RequestBuilder.get().setUri(createdURL)
				.setHeader(userHeader).setHeader(pswHeader).build();
		
		HttpResponse response;
		try {
			response = client.execute(request);
			int code = response.getStatusLine().getStatusCode();
			if (code == Status.UNAUTHORIZED.getStatusCode())
			{
				throw new IllegalArgumentException("Username or password are not valid");
			} else if (code == Status.PRECONDITION_FAILED.getStatusCode()) {
				throw new IllegalArgumentException("Username or password not set");
			} else if (code == Status.OK.getStatusCode())
			{
				authToken = this.readTextFromResponseInOneLine(response);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}

	}
	
	public String getTheory() {
		BasicHeader tokenHeader = new BasicHeader("token", authToken);
		
		String createdURL = urlRootService+getPathFromInfo("getTheory");
		HttpUriRequest request = RequestBuilder.get().setUri(createdURL)
				.setHeader(tokenHeader).build();
		HttpResponse response;
		try {
			response = client.execute(request);
			int code = response.getStatusLine().getStatusCode();
			if (code == Status.FORBIDDEN.getStatusCode()) {
				throw new IllegalArgumentException("Access Denied!");
			} else if (code == Status.OK.getStatusCode())
			{
				String theory = readTextFromResponse(response);
				return theory;
			}
			throw new IllegalStateException("Unexpected response: "+response.toString());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
	}
	
	public void setTheory(Theory toSet) {
		BasicHeader tokenHeader = new BasicHeader("token", authToken);
		
		String createdURL = urlRootService+getPathFromInfo("setTheory");
		StringEntity theoryEntity;
		HttpResponse response;
		try {
			theoryEntity = new StringEntity(toSet.toString());
			HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(theoryEntity)
					.setHeader(tokenHeader).build();
			response = client.execute(request);
			int code = response.getStatusLine().getStatusCode();
			if (code == Status.FORBIDDEN.getStatusCode()) {
				throw new IllegalArgumentException("Access Denied!");
			} else if (code != Status.CREATED.getStatusCode())
			{
				throw new IllegalStateException("Theory not set: "+response.toString());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set theory as body of HTTP request. Unsupported encoding.");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
	}

	
	public void setGoals(List<String> goalsList) {
		BasicHeader tokenHeader = new BasicHeader("token", authToken);
		BasicHeader contentJSONHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		
		String createdURL = urlRootService+getPathFromInfo("setGoalList");
		Gson gson = new Gson();
		String json = gson.toJson(goalsList);
		StringEntity goalListEntity;
		HttpResponse response;
		try {
			goalListEntity = new StringEntity(json.toString());
			HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(goalListEntity)
					.setHeader(contentJSONHeader).setHeader(tokenHeader).build();
			response = client.execute(request);
			int code = response.getStatusLine().getStatusCode();
			if (code == Status.FORBIDDEN.getStatusCode()) {
				throw new IllegalArgumentException("Access Denied!");
			} else if (code == Status.PRECONDITION_FAILED.getStatusCode()) {
				throw new IllegalStateException("Server can't convert back generated json to List.");
			} else if (code != Status.CREATED.getStatusCode())
			{
				throw new IllegalStateException("GoalsList not set: "+response.toString());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set goals as body of HTTP request. Unsupported encoding.");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
		
	}
	
	public List<String> getGoals() {
		BasicHeader tokenHeader = new BasicHeader("token", authToken);
		
		String createdURL = urlRootService+getPathFromInfo("getGoalListAsJSON");
		HttpUriRequest request = RequestBuilder.get().setUri(createdURL)
				.setHeader(tokenHeader).build();
		HttpResponse response = null;
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
		if (code == Status.FORBIDDEN.getStatusCode()) {
			throw new IllegalArgumentException("Access Denied!");
		} else if (code == Status.OK.getStatusCode())
		{
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
			List<String> list = (List<String>)gson.fromJson(json, List.class);
			return list;
		}
		throw new IllegalStateException("Unexpected response: "+response.toString());
		
		
	}
	
	public void addGoal(String goal) {
		BasicHeader tokenHeader = new BasicHeader("token", authToken);
		
		String createdURL = urlRootService+getPathFromInfo("addGoal");
		StringEntity goalEntity;
		try {
			goalEntity = new StringEntity(goal.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set goal as body of HTTP request. Unsupported encoding.");
		}
		HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(goalEntity)
				.setHeader(tokenHeader).build();
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
		if (code == Status.FORBIDDEN.getStatusCode()) {
			throw new IllegalArgumentException("Access Denied!");
		} else if (code == Status.NOT_MODIFIED.getStatusCode())
			return;
		else if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("Goal not added: "+response.toString());
		}
	}
	
	public void removeGoal(String goal) {
		BasicHeader tokenHeader = new BasicHeader("token", authToken);
		
		String createdURL = urlRootService+getPathFromInfo("removeGoal");
		StringEntity goalEntity;
		try {
			goalEntity = new StringEntity(goal.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new IllegalStateException("Can't set goal as body of HTTP request. Unsupported encoding.");
		}
		HttpUriRequest request = RequestBuilder.post().setUri(createdURL).setEntity(goalEntity)
				.setHeader(tokenHeader).build();
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
		if (code == Status.FORBIDDEN.getStatusCode()) {
			throw new IllegalArgumentException("Access Denied!");
		} else if (code == Status.NOT_MODIFIED.getStatusCode())
			throw new IllegalArgumentException("Current Goals list doesn't contain goal to remove");
		else if (code != Status.OK.getStatusCode())
		{
			throw new IllegalStateException("Goal not removed: "+response.toString());
		}
	}
	
	public void reset() {
		BasicHeader tokenHeader = new BasicHeader("token", authToken);
		
		String createdURL = urlRootService;
		HttpUriRequest request = RequestBuilder.delete().setUri(createdURL)
				.setHeader(tokenHeader).build();
		HttpResponse response;
		try {
			response = client.execute(request);
			int code = response.getStatusLine().getStatusCode();
			if (code == Status.FORBIDDEN.getStatusCode()) {
				throw new IllegalArgumentException("Access Denied!");
			} else if (code != Status.OK.getStatusCode())
			{
				throw new IllegalStateException("Reset unsuccesful: "+response.toString());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new IllegalStateException("HTTP protocol error. Can't obtain response.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Connection aborted. Can't obtain response.");
		}
	}

	private String getPathFromInfo(String methodName){
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
			String[] pieces = url.split("/configuration");
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
	private String readTextFromResponseInOneLine(HttpResponse response) throws UnsupportedOperationException, IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ((line = rd.readLine()) != null) {
			builder.append(line);
		}
		rd.close();
		return builder.toString();
	}

}
