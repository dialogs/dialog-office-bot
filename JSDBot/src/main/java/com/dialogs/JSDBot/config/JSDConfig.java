package com.dialogs.JSDBot.config;

import org.json.JSONObject;

public class JSDConfig {

	private String host;
	private int port;
	private String context;
	private int serviceDeskId;
	private String username;
	private String password;
	private String authorDomain;

	public JSDConfig(JSONObject jsonConfig) {
		this.host = jsonConfig.getString("host");
		this.port = jsonConfig.getInt("port");
		this.context = jsonConfig.getString("context");
		this.serviceDeskId = jsonConfig.getInt("serviceDeskId");
		this.username = jsonConfig.getString("username");
		this.password = jsonConfig.getString("password");
		this.authorDomain = jsonConfig.getString("authorDomain");
	}

	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}

	public String getContext() {
		return context;
	}

	public int getServiceDeskId() {
		return serviceDeskId;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getAuthorDomain() {
		return authorDomain;
	}

}
