package com.dialogs.JSDBot.utils.types;

import org.json.JSONObject;

import com.dialogs.JSDBot.utils.RequestField;

public class C_DateTimePicker extends RequestField {

	public C_DateTimePicker( JSONObject jsonObject ) {
		super(jsonObject);
	}

	@Override
	public boolean isValidValue(String value) {
		return value.matches("^(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z)?$");
	}

	@Override
	public void addJsonField(JSONObject jsonRequestFields) {
		jsonRequestFields.put(this.id, this.stringValue);
	}

}
