package com.dialogs.JSDBot.utils;

public class RequestFieldValue {

	private String value;
	private String label;
	private RequestFieldValue[] childrens;
	
	public RequestFieldValue( String value, String label, RequestFieldValue[] childrens ){
		this.value = value;
		this.label = label;
		this.childrens = childrens;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public String getLabel(){
		return this.label;
	}
	
	public RequestFieldValue[] getChildrens(){
		return this.childrens;
	}
}
