package com.dialogs.JSDBot.utils;

import org.json.JSONObject;

public class RequestType {

	private int id;
	private String name, description, helpText;
	
	public RequestType( JSONObject jsonRequestType ) {
		this.id = jsonRequestType.getInt("id");
		this.name = jsonRequestType.getString("name");
		this.description = jsonRequestType.getString("description");
		this.helpText = jsonRequestType.getString("helpText");
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return description;
	}


	public String getHelpText() {
		return helpText;
	}


}
