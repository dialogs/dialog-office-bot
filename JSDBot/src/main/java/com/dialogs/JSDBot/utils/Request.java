package com.dialogs.JSDBot.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class Request {

	private int serviceDeskId, typeId, currentField;
	//private String name;
	private String description;
	//private String helpText;
	private RequestField[] fields;

	public Request( int serviceDeskId, int typeId, /*String name,*/ String description, JSONArray requestFields, RequestFieldValue[] serviceDeskAssignableUsers ){
		this.serviceDeskId = serviceDeskId;
		this.typeId = typeId;
		//this.name = name;
		this.description = description;
		//this.helpText = "";
		this.currentField = 0;
		
		this.fields = new RequestField[requestFields.length()];
		
		for( int i=0; i<requestFields.length(); i++ ){
			fields[i] = RequestField.fromJSON(requestFields.getJSONObject(i), serviceDeskAssignableUsers);
		}
	}
	
	public void setCurrentFieldValue( String value ){
		this.fields[currentField].setStringValue(value);
		this.currentField++;
	}
	
	public String getCurrentFieldType(){
		return this.fields[this.currentField].getType();
	}
	
	public String getCurrentFieldName(){
		return this.fields[this.currentField].getName();
	}

	public boolean isCompleted() {
		return this.currentField >= this.fields.length;
	}

	public RequestField getCurrentField() {
		return this.fields[this.currentField];
	}
	
	public JSONObject buildJsonRequest( String authorName ){
		JSONObject jsonRequest = new JSONObject();
		JSONObject jsonRequestFields = new JSONObject();
		//JSONArray jsonRequestParticipants = new JSONArray();
		
		for ( int i=0; i<this.fields.length; i++ ) {
			RequestField requestField = this.fields[i];
			requestField.addJsonField(jsonRequestFields);
		}
		
		//jsonRequestParticipants.put(requestParticipant);
		
		jsonRequest.put("serviceDeskId", this.serviceDeskId);
		jsonRequest.put("requestTypeId", this.typeId);
		jsonRequest.put("requestFieldValues", jsonRequestFields);
		jsonRequest.put("raiseOnBehalfOf", authorName);
		
		return jsonRequest;
	}

	public String getDescription() {
		return this.description;
	}
}
