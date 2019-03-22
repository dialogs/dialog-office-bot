package com.dialogs.JSDBot;

public class RequestApiRouter {

	private static final String BASE_URL_PATTER = "%s:%d%s/rest";
	private static final String GET_SERVICEDESK_INFO_PATTERN = "/servicedeskapi/servicedesk/%d";
	private static final String GET_SERVICEDESK_USERS_PATTERN = "/api/3/user/assignable/search?maxResults=1000&project=%s";
	private static final String GET_REQUEST_TYPE_URL_PATTERN = "/servicedeskapi/servicedesk/%d/requesttype";
	private static final String GET_REQUEST_TYPE_FIELDS_URL_PATTERN = "/servicedeskapi/servicedesk/%d/requesttype/%d/field";
	private static final String POST_REQUEST_URL_PATTERN = "/servicedeskapi/request";
	private static final String POST_ATTACHMENT_URL_PATTERN = "/servicedeskapi/servicedesk/%d/attachTemporaryFile";
	
	private String baseUrl;
	private int serviceDeskId;
	
	public RequestApiRouter(String host, int port, int serviceDeskId) {
		this(host, port, "", serviceDeskId);
	}

	public RequestApiRouter(String host, int port, String context, int serviceDeskId) {
		this.serviceDeskId = serviceDeskId;
		this.baseUrl = String.format(BASE_URL_PATTER, host, port, context);
	}
	
	public String buildGetRequestTypeUrl(){
		return this.baseUrl + String.format(GET_REQUEST_TYPE_URL_PATTERN, this.serviceDeskId);
	}
	
	public String buildGetServiceDeskInfoUrl(){
		return this.baseUrl + String.format(GET_SERVICEDESK_INFO_PATTERN, this.serviceDeskId);
	}
	
	public String buildGetServiceDeskUsersUrl( String projectKey ){
		return this.baseUrl + String.format(GET_SERVICEDESK_USERS_PATTERN, projectKey);
	}
	
	public String buildGetRequestTypeFieldsUrl( int requestTypeId ){
		return this.baseUrl + String.format(GET_REQUEST_TYPE_FIELDS_URL_PATTERN, this.serviceDeskId, requestTypeId);
	}
	
	public String buildPostRequestTypeUrl(){
		return this.baseUrl + String.format(POST_REQUEST_URL_PATTERN);
	}
	
	public String buildPostAttachment() {
		return this.baseUrl + String.format(POST_ATTACHMENT_URL_PATTERN, this.serviceDeskId);
	}
	
}
