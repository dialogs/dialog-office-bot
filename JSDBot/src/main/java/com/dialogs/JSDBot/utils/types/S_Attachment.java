package com.dialogs.JSDBot.utils.types;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dialogs.JSDBot.utils.RequestField;

public class S_Attachment extends RequestField {

	public S_Attachment(JSONObject jsonObject ) {
		super(jsonObject);
	}

	@Override
	public boolean isValidValue(String value) {
		return false; // value can't by valid by text, the value will be forced when upload files
	}

	@Override
	public void addJsonField(JSONObject jsonRequestFields) {
		jsonRequestFields.put(this.id, new JSONArray(this.stringValue.split(",")));
	}

}
