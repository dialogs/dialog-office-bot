package com.dialogs.JSDBot.utils.types;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dialogs.JSDBot.utils.RequestField;
import com.dialogs.JSDBot.utils.RequestFieldValue;

public class C_MultiUserPicker extends RequestField {

	public C_MultiUserPicker( JSONObject jsonObject, RequestFieldValue[] serviceDeskAssignableUsers ) {
		super(jsonObject);
		
		this.validValues = serviceDeskAssignableUsers;
	}

	@Override
	public boolean isValidValue( String value ) {
		if ( !value.matches("[0-9a-zA-Z._-]+(,[0-9a-zA-Z._-]+)*") ) return false;
		
		String[] splitedValue = value.split(",");
		
		for ( String username : splitedValue ){
			if ( !this.existValidValue(username) ) return false;
		}
		
		return true;
	}

	@Override
	public void addJsonField( JSONObject jsonRequestFields ) {
		JSONArray jsonValue = new JSONArray();
		
		for ( String username : this.stringValue.split(",") ) {
			jsonValue.put((new JSONObject()).put("name", username));
		}
		
		jsonRequestFields.put(this.id, jsonValue);
	}

}
