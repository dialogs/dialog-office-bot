package com.dialogs.JSDBot.utils.types;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dialogs.JSDBot.utils.RequestField;

public class S_Labels extends RequestField {

	public S_Labels( JSONObject jsonObject ) {
		super(jsonObject);
	}

	@Override
	public boolean isValidValue( String value ) {
		return value.matches("[0-9a-zA-Z]+(,[0-9a-zA-Z]+)*");
	}

	@Override
	public void addJsonField( JSONObject jsonRequestFields ) {
		jsonRequestFields.put(this.id, new JSONArray(this.stringValue.split(",")));
	}

}
