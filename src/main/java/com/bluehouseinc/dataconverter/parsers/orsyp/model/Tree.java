package com.bluehouseinc.dataconverter.parsers.orsyp.model;

public class Tree {
	Root root;
	private String _id;

	// Getter Methods

	public Root getRoot() {
		return root;
	}

	public String get_id() {
		return _id;
	}

	// Setter Methods

	public void setRoot(Root rootObject) {
		this.root = rootObject;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
