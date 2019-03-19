package com.dialogs.JSDBot.utils.types;

import org.json.JSONObject;
import com.dialogs.JSDBot.utils.RequestField;

public class C_RadioButton extends RequestField {

	public C_RadioButton( JSONObject jsonObject ) {
		super(jsonObject);
	}

	@Override
	public boolean isValidValue( String value ) {
		if ( !value.matches("[0-9a-zA-Z]+*") ) return false;
		if ( this.validValues.length > 0 && !this.existValidValue(value) ) return false;
		return true;
	}

	@Override
	public void addJsonField( JSONObject jsonRequestFields ) {
		jsonRequestFields.put(this.id, (new JSONObject()).put("value", this.stringValue));
	}

}
