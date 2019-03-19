package com.dialogs.JSDBot.utils.types;

import org.json.JSONObject;

import com.dialogs.JSDBot.utils.RequestField;

public class C_URL extends RequestField {

	public C_URL( JSONObject jsonObject ) {
		super(jsonObject);
	}

	@Override
	public boolean isValidValue(String value) {
		return value.matches("");
	}

	@Override
	public void addJsonField(JSONObject jsonRequestFields) {
		jsonRequestFields.put(this.getId(), this.stringValue);
	}

}
