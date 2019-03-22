package com.dialogs.JSDBot.utils.types;

import org.json.JSONObject;
import com.dialogs.JSDBot.utils.RequestField;
import com.dialogs.JSDBot.utils.RequestFieldValue;

public class C_SingleUserPicker extends RequestField {

	public C_SingleUserPicker( JSONObject jsonObject, RequestFieldValue[] serviceDeskAssignableUsers ) {
		super(jsonObject);
		
		this.validValues = serviceDeskAssignableUsers;
	}

	@Override
	public boolean isValidValue( String value ) {
		if ( !value.matches("[0-9a-zA-Z]+*") ) return false;
		if ( !this.existValidValue(value) ) return false;
		return true;
	}

	@Override
	public void addJsonField( JSONObject jsonRequestFields ) {
		jsonRequestFields.put(this.id, (new JSONObject()).put("name", this.stringValue));
	}

}
