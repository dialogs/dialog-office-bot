package com.dialogs.JSDBot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONObject;

import com.dialogs.JSDBot.config.DialogConfig;
import com.dialogs.JSDBot.config.JSDConfig;

public class Main {

	public static final String CONFIG_PATH = "config.json";
	
	public static void main(String[] args) throws Exception {
		
		Path configFilePath = Paths.get( args.length == 0 ? CONFIG_PATH : args[0] );
		
		String textJsonConfig = new String(Files.readAllBytes(configFilePath));
		
		JSONObject jsonConfig = new JSONObject(textJsonConfig);
		JSONObject jsonDialogsConfig = jsonConfig.getJSONObject("dialogsConfig");
		JSONObject jsonJSDConfig = jsonConfig.getJSONObject("jiraServiceDeskConfig");
		
		DialogConfig dialogConfig = new DialogConfig(jsonDialogsConfig);
		JSDConfig jsdConfig = new JSDConfig(jsonJSDConfig);
		
		JSDBot bot = new JSDBot(dialogConfig, jsdConfig);
		
		bot.start();
		
	}

}
