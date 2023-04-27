package com.bluehouseinc.dataconverter.parsers.orsyp.model;

public class Root {
	Root root;
	ChildOk childOk;
	Data data;
	private String _id;

	// Getter Methods

	public Root getRoot() {
		return root;
	}

	public ChildOk getChildOk() {
		return childOk;
	}

	public Data getData() {
		return data;
	}

	public String get_id() {
		return _id;
	}

	// Setter Methods

	public void setRoot(Root rootObject) {
		this.root = rootObject;
	}

	public void setChildOk(ChildOk childOkObject) {
		this.childOk = childOkObject;
	}

	public void setData(Data dataObject) {
		this.data = dataObject;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
