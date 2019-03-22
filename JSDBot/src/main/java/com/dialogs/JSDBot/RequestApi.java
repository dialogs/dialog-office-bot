package com.dialogs.JSDBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dialogs.JSDBot.config.JSDConfig;

public class RequestApi {

	private HttpClient client = HttpClientBuilder.create().build();
	
	private RequestApiRouter router;
	private String getRequestTypesUrl;
	private String postRequestUrl;

	private String authString;
	private String authToken;
	
	public RequestApi( JSDConfig jsdConfig ){

		this.router = new RequestApiRouter(jsdConfig.getHost(), jsdConfig.getPort(), jsdConfig.getContext(), jsdConfig.getServiceDeskId());
		this.getRequestTypesUrl = this. router.buildGetRequestTypeUrl();
		this.postRequestUrl = this.router.buildPostRequestTypeUrl();
		
		System.out.println(getRequestTypesUrl);
		System.out.println(postRequestUrl);
		
		this.authString = jsdConfig.getUsername() + ":" + jsdConfig.getPassword();
		this.authToken = Base64.getEncoder().encodeToString(this.authString.getBytes());
		
		System.out.println(this.authString);
		System.out.println(this.authToken);
	}
	
	public JSONObject getRequestTypes() throws Exception {
		HttpGet request = this.buildDefaultHttpGet(this.getRequestTypesUrl);
		JSONObject jsonResult = new JSONObject(this.getGetStringResonse(request));
		
		return jsonResult;
	}
	
	public JSONObject getServiceDeskInfo() throws Exception {
		HttpGet request = this.buildDefaultHttpGet(this.router.buildGetServiceDeskInfoUrl());
		JSONObject jsonResult = new JSONObject(this.getGetStringResonse(request));
		
		return jsonResult;
	}
	
	public JSONArray getServiceDeskUsers( String projectKey ) throws Exception {
		HttpGet request = this.buildDefaultHttpGet(this.router.buildGetServiceDeskUsersUrl(projectKey));
		JSONArray jsonResult = new JSONArray(this.getGetStringResonse(request));
		
		return jsonResult;
	}
	
	public JSONObject getRequestTypeFields( int requestTypeId ) throws Exception {
		HttpGet request = this.buildDefaultHttpGet(this.router.buildGetRequestTypeFieldsUrl(requestTypeId));
		JSONObject jsonResult = new JSONObject(this.getGetStringResonse(request));
		
		return jsonResult;
	}
	
	public JSONObject postRequest( JSONObject requestParams ) throws Exception {
		HttpPost request = this.buildDefaultHttpPost(this.postRequestUrl);

		System.out.println(requestParams.toString());
		
		StringEntity entity = new StringEntity(requestParams.toString(), ContentType.APPLICATION_JSON);
		
		request.setEntity(entity);

		JSONObject jsonResult = new JSONObject(this.getPostStringResonse(request));
		
		System.out.println(jsonResult);
		
		return jsonResult;
	}
	
	public JSONObject postAttachment( File attachmentFile ) throws Exception {
		HttpPost request = this.buildDefaultHttpPost(this.router.buildPostAttachment());
		
		request.setHeader("X-Atlassian-Token", "no-check");
		request.setHeader("X-ExperimentalApi", "opt-in");
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		   
	    builder.addPart("file", new FileBody(attachmentFile));
		
	    request.setEntity(builder.build());
		
		JSONObject jsonResult = new JSONObject(this.getPostStringResonse(request));
		
		return jsonResult;
	}
	
	private HttpGet buildDefaultHttpGet( String url ){
		HttpGet request = new HttpGet(url);

		request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + this.authToken);
		request.setHeader(HttpHeaders.ACCEPT, "application/json");
		
		return request;
	}
	
	private HttpPost buildDefaultHttpPost( String url ){
		HttpPost request = new HttpPost(url);

		request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + this.authToken);
		request.setHeader(HttpHeaders.ACCEPT, "application/json");
		
		return request;
	}
	
	private String getGetStringResonse( HttpGet request ) throws ClientProtocolException, IOException {
		HttpResponse response = client.execute(request);
		
		int responseCode = response.getStatusLine().getStatusCode();

		System.out.println("\nSending 'POST' request to URL : " + this.router.buildPostAttachment());
		System.out.println("Response Code : " + responseCode);

		String stringResponse = this.parseStringResponse(response);

		request.releaseConnection();
		
		return stringResponse;
	}

	private String getPostStringResonse( HttpPost request ) throws Exception {
		HttpResponse response = client.execute(request);

		int responseCode = response.getStatusLine().getStatusCode();

		System.out.println("\nSending 'POST' request to URL : " + this.router.buildPostAttachment());
		System.out.println("Response Code : " + responseCode);

		String stringResponse = this.parseStringResponse(response);
		
		request.releaseConnection();
		
		return stringResponse;
	}
	
	private String parseStringResponse( HttpResponse response ) throws UnsupportedOperationException, IOException{
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		return result.toString();
	}
}
