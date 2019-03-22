package com.dialogs.JSDBot.config;

import org.json.JSONObject;

public class DialogConfig {

	private String host;
	private int port;
	private String token;

	public DialogConfig(JSONObject jsonConfig) {
		this.host = jsonConfig.getString("host");
		this.port = jsonConfig.getInt("port");
		this.token = jsonConfig.getString("token");
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getToken() {
		return token;
	}

}
