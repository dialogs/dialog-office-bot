package com.dialogs.JSDBot.utils.types;

import org.json.JSONObject;

import com.dialogs.JSDBot.utils.RequestField;

public class C_MultiLine extends RequestField {

	public C_MultiLine( JSONObject jsonObject ) {
		super(jsonObject);
	}

	@Override
	public boolean isValidValue(String value) {
		return !value.isEmpty();
	}

	@Override
	public void addJsonField(JSONObject jsonRequestFields) {
		jsonRequestFields.put(this.getId(), this.stringValue);
	}

}
