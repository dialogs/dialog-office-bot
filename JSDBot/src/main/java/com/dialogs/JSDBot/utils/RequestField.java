package com.dialogs.JSDBot.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dialogs.JSDBot.utils.types.*;

/*
1- Summary
2- Description
3- Components
4- Due date
5- Labels
6- Checkbox custom field:
7- Date picker custom field: 
8- Date time picker custom field:
9- Labels custom field
10-Number custom field
11-Radio button custom field
12-Cascading select custom field
13-Multi-select custom field
14-Single-select custom field
15-Multi-line text custom field:
16-Text custom field
17-URL custom field
18-Single-user picker custom field
19-Multi-user picker custom field
20-Attachment
*/

public abstract class RequestField {

	protected String id;
	protected String name;
	protected boolean required;
	protected String type;
	protected String helpText;
	protected RequestFieldValue[] validValues;
	protected String stringValue;
	protected JSONObject jiraSchema;
	
	// Custom Field Prefix
	public static String CPF = "com.atlassian.jira.plugin.system.customfieldtypes:";
	
	public static RequestField fromJSON( JSONObject jsonField, RequestFieldValue[] serviceDeskAssignableUsers ) {
		
		JSONObject jsonSchema = jsonField.getJSONObject("jiraSchema");
		
		System.out.println(jsonSchema);
		
		String fieldType = jsonSchema.has("system") ? jsonSchema.getString("system") : jsonSchema.getString("custom");
		
		// SYSTEM TYPES
		if ( fieldType.equals("attachment") ) return new S_Attachment(jsonField);
		if ( fieldType.equals("components") ) return new S_Components(jsonField);
		if ( fieldType.equals("description") ) return new S_Description(jsonField);
		if ( fieldType.equals("duedate") ) return new S_DueDate(jsonField);
		if ( fieldType.equals("labels") ) return new S_Labels(jsonField);
		if ( fieldType.equals("summary") ) return new S_Summary(jsonField);
		
		// CUSTOM TYPES
		if ( fieldType.equals(CPF + "cascadingselect") ) return new C_CascadingSelect(jsonField);	
		if ( fieldType.equals(CPF + "multicheckboxes") ) return new C_CheckBox(jsonField);
		if ( fieldType.equals(CPF + "datepicker") ) return new C_DatePicker(jsonField);
		if ( fieldType.equals(CPF + "datetime") ) return new C_DateTimePicker(jsonField);
		if ( fieldType.equals(CPF + "labels") ) return new C_Labels(jsonField);
		if ( fieldType.equals(CPF + "textarea") ) return new C_MultiLine(jsonField);
		if ( fieldType.equals(CPF + "multiselect") ) return new C_MultiSelectPicker(jsonField);
		if ( fieldType.equals(CPF + "multiuserpicker") ) return new C_MultiUserPicker(jsonField, serviceDeskAssignableUsers);
		if ( fieldType.equals(CPF + "float") ) return new C_Number(jsonField);
		if ( fieldType.equals(CPF + "radiobuttons") ) return new C_RadioButton(jsonField);
		if ( fieldType.equals(CPF + "select") ) return new C_SingleSelectPicker(jsonField);
		if ( fieldType.equals(CPF + "userpicker") ) return new C_SingleUserPicker(jsonField, serviceDeskAssignableUsers);
		if ( fieldType.equals(CPF + "textfield") ) return new C_Text(jsonField);
		if ( fieldType.equals(CPF + "url") ) return new C_URL(jsonField);
		
		System.out.println("Unhandled field type : " + fieldType);
		
		return null;
	}
	
	public RequestField( JSONObject jsonObject ){
		this.id = jsonObject.getString("fieldId");
		this.name = jsonObject.getString("name");
		this.required = jsonObject.getBoolean("required");
		this.helpText = "";
		
		JSONArray jsonValidValues = jsonObject.getJSONArray("validValues");
		JSONObject jsonJiraSchema = jsonObject.getJSONObject("jiraSchema");
		
		this.jiraSchema = jsonJiraSchema;
		
		this.type = jsonJiraSchema.has("system") ? jsonJiraSchema.getString("system") : jsonJiraSchema.getString("custom");
		//this.type = jsonJiraSchema.getString("type");
		this.validValues = new RequestFieldValue[jsonValidValues.length()];
		
		for ( int i=0; i<jsonValidValues.length(); i++ ){
			JSONObject validValue = jsonValidValues.getJSONObject(i);
			RequestFieldValue[] childrens = parseFieldValueChildrens(validValue.getJSONArray("children"));
			this.validValues[i] = new RequestFieldValue(validValue.getString("value"), validValue.getString("label"), childrens);
		}
	}

	public abstract boolean isValidValue( String value );
	
	public abstract void addJsonField(JSONObject jsonRequestFields);
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}
	
	public String getHelpText(){
		return this.helpText;
	}
	
	public RequestFieldValue[] getValidValues() {
		return this.validValues;
	}
	
	public void setStringValue( String value ){
		this.stringValue = value;
	}

	protected boolean existValidValue( String value ) {
		for ( RequestFieldValue requestFieldValue : this.validValues ){
			if ( requestFieldValue.getValue().equals(value) ) return true;
		}
		return false;
	}
	
	protected boolean existNestedValidValue( String values[] ){
		RequestFieldValue[] currentNestedLevelValues = this.validValues;
		for ( String currentLevelValue : values ){
			boolean valueFound = false;
			for ( RequestFieldValue requestFieldValue : currentNestedLevelValues ){
				if ( requestFieldValue.getValue().equals(currentLevelValue) ) {
					currentNestedLevelValues = requestFieldValue.getChildrens();
					valueFound = true;
					break;
				}
			}
			if ( !valueFound ) return false;
		}
		return true;
	}
	
	private RequestFieldValue[] parseFieldValueChildrens( JSONArray jsonChildrens ) {
		RequestFieldValue[] childrens = new RequestFieldValue[jsonChildrens.length()];
		
		for ( int i=0; i<jsonChildrens.length(); i++ ) {
			JSONObject currentChild = jsonChildrens.getJSONObject(i);
			childrens[i] = new RequestFieldValue(currentChild.getString("value"), currentChild.getString("label"), parseFieldValueChildrens(currentChild.getJSONArray("children")));
		}
		
		return childrens;
	}
}
