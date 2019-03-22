package com.dialogs.JSDBot;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dialogs.JSDBot.config.JSDConfig;
import com.dialogs.JSDBot.utils.Request;
import com.dialogs.JSDBot.utils.RequestField;
import com.dialogs.JSDBot.utils.RequestFieldValue;
import com.dialogs.JSDBot.utils.RequestType;

public class RequestHandler {

	private RequestApi requestApi;
	private ConcurrentHashMap<Integer, Request> currentRequests;
	private HashMap<Integer, RequestType> serviceDeskRequestTypes;
	private RequestFieldValue[] serviceDeskAssignableUsers;
	private int serviceDeskId;
	private String authorDomain;
	
	public RequestHandler( JSDConfig jsdConfig ) throws Exception {
		this.serviceDeskId = jsdConfig.getServiceDeskId();
		this.authorDomain = jsdConfig.getAuthorDomain();
		this.requestApi = new RequestApi(jsdConfig);
		this.currentRequests = new ConcurrentHashMap<Integer, Request>();
		
		JSONObject getServiceDeskInfoResponse = this.requestApi.getServiceDeskInfo();
		JSONObject getRequestTypeResponse = this.requestApi.getRequestTypes();
		JSONArray jsonAssignableUsers = this.requestApi.getServiceDeskUsers(getServiceDeskInfoResponse.getString("projectKey"));
		JSONArray jsonRequestTypes = getRequestTypeResponse.getJSONArray("values");
		
		this.serviceDeskAssignableUsers = new RequestFieldValue[jsonAssignableUsers.length()];
		this.serviceDeskRequestTypes = new HashMap<Integer, RequestType>();
		
		for ( int i=0; i<jsonAssignableUsers.length(); i++ ) {
			JSONObject jsonAssignableUser = jsonAssignableUsers.getJSONObject(i);
			String name = jsonAssignableUser.getString("name");
			String displayName = jsonAssignableUser.getString("displayName");;
			this.serviceDeskAssignableUsers[i] = new RequestFieldValue(name, displayName, new RequestFieldValue[0]);
		}
		
		for ( int i=0; i<jsonRequestTypes.length(); i++ ) {
			RequestType requestType = new RequestType(jsonRequestTypes.getJSONObject(i));
			this.serviceDeskRequestTypes.put(requestType.getId(), requestType);
		}
	}
	
	public List<RequestType> findRequestTypes( String textPattern ) throws Exception {
		String pattern = "(.*)" + textPattern + "(.*)";
		
		List<RequestType> filteredRequestTypes = new ArrayList<RequestType>();
		
		this.serviceDeskRequestTypes.forEach((requestTypeId, requestType) -> {
			String name = requestType.getName();
			String description = requestType.getDescription();
			String helpText = requestType.getHelpText();
			boolean matchFound = name.matches(pattern) || description.matches(pattern) || helpText.matches(pattern);
			if (matchFound) {
				filteredRequestTypes.add(requestType);
			}
		});
		
		return filteredRequestTypes;
	}
	
	public Request createRequest( int peerId, int requestTypeId ) throws UserInputException, Exception {
		if ( this.hasCurrentRequest(peerId) ) throw new UserInputException("Sorry, You already have a request in progress");
		if ( !this.serviceDeskRequestTypes.containsKey(requestTypeId) ) throw new UserInputException("Sorry, the requestTypeId is invalid");
		
		RequestType requestType = this.serviceDeskRequestTypes.get(requestTypeId);
		JSONObject jsonResponse = this.requestApi.getRequestTypeFields(requestTypeId);
		JSONArray jsonRequestFields = jsonResponse.getJSONArray("requestTypeFields");
		boolean canRaiseOnBehalfOf = jsonResponse.getBoolean("canRaiseOnBehalfOf");
		boolean canAddRequestParticipants = jsonResponse.getBoolean("canAddRequestParticipants");
		System.out.println("CAN RAISE " + canRaiseOnBehalfOf + " : CAN ADD " + canAddRequestParticipants);
		Request request = new Request(this.serviceDeskId, requestTypeId, /*requestType.getName(),*/ requestType.getDescription(), jsonRequestFields, serviceDeskAssignableUsers);
		this.currentRequests.put(peerId, request);
		
		return request;
	}
	
	public void setRequestField(int peerId, String fieldValue, boolean forceValidation) throws UserInputException, Exception {
		if ( !this.hasCurrentRequest(peerId) ) throw new UserInputException("Sorry, you do not have a request now");
		if ( this.isRequestCompleted(peerId) ) throw new UserInputException("Sorry, your request is completed");
		if ( !forceValidation && !this.isFieldValueValid(peerId, fieldValue) ) throw new UserInputException("Sorry, your value is invalid");
				
		this.currentRequests.get(peerId).setCurrentFieldValue(fieldValue);
	}
	
	public String uploadAttachmentFile( String fileName, String fileUrl ) throws Exception {
		InputStream attachmentInputStream = new URL(fileUrl).openStream();
		
		File attachmentFile = File.createTempFile(FilenameUtils.getBaseName(fileName), "." + FilenameUtils.getExtension(fileName));
		
		FileUtils.copyInputStreamToFile(attachmentInputStream, attachmentFile);
		
		JSONObject jsonResponse = requestApi.postAttachment(attachmentFile);
		JSONArray jsonFilesArray = jsonResponse.getJSONArray( "temporaryAttachments");
		
		//attachmentFile.delete();
		
		String[] hashnamesArray = new String[jsonFilesArray.length()];
		
		for ( int i=0; i<jsonFilesArray.length(); i++) {
			hashnamesArray[i] = jsonFilesArray.getJSONObject(i).getString("temporaryAttachmentId");
		}
		
		String hashnameArrayStringByComas = String.join(",", hashnamesArray);
		
		return hashnameArrayStringByComas;
	}
	
	public void cancelRequest(int peerId) throws UserInputException{
		if ( !this.hasCurrentRequest(peerId) ) throw new UserInputException("Sorry, you do not have a request now");
		
		this.currentRequests.remove(peerId);
	}
	
	public JSONObject sendRequest(int peerId, String authorUsername) throws UserInputException, Exception {
		if ( !this.hasCurrentRequest(peerId) ) throw new UserInputException("Sorry, you do not have a request now");
		if ( !this.isRequestCompleted(peerId) ) throw new UserInputException("Sorry, your request is not completed");
				
		Request request = this.currentRequests.get(peerId);
		JSONObject requestParams = request.buildJsonRequest(authorUsername + this.authorDomain);
		JSONObject response = this.requestApi.postRequest(requestParams);
		
		if ( !response.has("issueId") ) throw new Exception("Malformed sendRequest response");
		
		this.currentRequests.remove(peerId);
		
		return response;
	}
	
	public boolean hasCurrentRequest(int peerId){
		return this.currentRequests.containsKey(peerId);
	}
	
	public boolean isRequestCompleted(int peerId) {
		return this.currentRequests.get(peerId).isCompleted();
	}

	public RequestField getCurrentField(int peerId) {
		return this.currentRequests.get(peerId).getCurrentField();
	}

	public String getCurrentFieldType(int peerId) {
		return this.currentRequests.get(peerId).getCurrentFieldType();
	}

	private boolean isFieldValueValid( int peerId, String fieldValue) {
		return this.currentRequests.get(peerId).getCurrentField().isValidValue(fieldValue);
	}
}
