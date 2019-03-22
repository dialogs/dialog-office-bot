package com.dialogs.JSDBot.utils.types;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dialogs.JSDBot.utils.RequestField;

public class S_Components extends RequestField {

	public S_Components( JSONObject jsonObject ) {
		super(jsonObject);
	}

	@Override
	public boolean isValidValue( String value ) {
		if ( !value.matches("[0-9a-zA-Z._-]+(,[0-9a-zA-Z._-]+)*") ) return false;
		
		String[] splitedValue = value.split(",");
		
		for ( String componentName : splitedValue ){
			if ( !this.existValidValue(componentName) ) return false;
		}
		
		return true;
	}

	@Override
	public void addJsonField( JSONObject jsonRequestFields ) {
		JSONArray jsonValue = new JSONArray();
		
		for ( String componentName : this.stringValue.split(",") ) {
			jsonValue.put((new JSONObject()).put("name", componentName));
		}
		
		jsonRequestFields.put(this.id, jsonValue);
	}

}
