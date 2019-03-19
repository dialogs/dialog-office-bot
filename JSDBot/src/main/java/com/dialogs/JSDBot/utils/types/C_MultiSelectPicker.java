package com.dialogs.JSDBot.utils.types;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dialogs.JSDBot.utils.RequestField;

public class C_MultiSelectPicker extends RequestField {

	public C_MultiSelectPicker( JSONObject jsonObject ) {
		super(jsonObject);
	}

	@Override
	public boolean isValidValue( String value ) {
		if ( !value.matches("[0-9a-zA-Z]+(,[0-9a-zA-Z]+)*") ) return false;
		
		String[] splitedValue = value.split(",");
		
		for ( String selectValue : splitedValue ){
			if ( this.validValues.length > 0 && !this.existValidValue(selectValue) ) return false;
		}
		
		return true;
	}

	@Override
	public void addJsonField( JSONObject jsonRequestFields ) {
		JSONArray jsonValue = new JSONArray();
		
		for ( String selectValue : this.stringValue.split(",") ) {
			jsonValue.put((new JSONObject()).put("value", selectValue));
		}
		
		jsonRequestFields.put(this.id, jsonValue);
	}

}
