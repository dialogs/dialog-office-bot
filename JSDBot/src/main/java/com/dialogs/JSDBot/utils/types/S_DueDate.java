package com.dialogs.JSDBot.utils.types;

import org.json.JSONObject;

import com.dialogs.JSDBot.utils.RequestField;

public class S_DueDate extends RequestField {

	public S_DueDate(JSONObject jsonObject ) {
		super(jsonObject);
	}

	@Override
	public boolean isValidValue(String value) {
		return value.matches("\\d{4}-\\d{2}-\\d{2}");
	}

	@Override
	public void addJsonField(JSONObject jsonRequestFields) {
		jsonRequestFields.put(this.id, this.stringValue);
	}

}
