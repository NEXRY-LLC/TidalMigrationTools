package com.bluehouseinc.dataconverter.parsers.orsyp.model;

public class ExecutionContext {
	private String type;
	SyntaxCheck syntaxCheck;
	private String _id;

	// Getter Methods

	public String getType() {
		return type;
	}

	public SyntaxCheck getSyntaxCheck() {
		return syntaxCheck;
	}

	public String get_id() {
		return _id;
	}

	// Setter Methods

	public void setType(String type) {
		this.type = type;
	}

	public void setSyntaxCheck(SyntaxCheck syntaxCheckObject) {
		this.syntaxCheck = syntaxCheckObject;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
