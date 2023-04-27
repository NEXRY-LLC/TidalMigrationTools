package com.bluehouseinc.dataconverter.parsers.orsyp.model;

public class Identifier {
	SyntaxRules syntaxRules;
	private String name;
	private String version;
	private String _class;
	private String _id;

	// Getter Methods

	public SyntaxRules getSyntaxRules() {
		return syntaxRules;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String get_class() {
		return _class;
	}

	public String get_id() {
		return _id;
	}

	// Setter Methods

	public void setSyntaxRules(SyntaxRules syntaxRulesObject) {
		this.syntaxRules = syntaxRulesObject;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void set_class(String _class) {
		this._class = _class;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
