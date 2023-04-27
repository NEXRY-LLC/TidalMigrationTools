package com.bluehouseinc.dataconverter.parsers.orsyp.model;

public class NextSibling {
	Root root;
	Parent parent;
	NextSibling nextSibling;
	PreviousSibling previousSibling;
	Data data;
	private String _id;

	// Getter Methods

	public Root getRoot() {
		return root;
	}

	public Parent getParent() {
		return parent;
	}

	public NextSibling getNextSibling() {
		return nextSibling;
	}

	public PreviousSibling getPreviousSibling() {
		return previousSibling;
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

	public void setParent(Parent parentObject) {
		this.parent = parentObject;
	}

	public void setNextSibling(NextSibling nextSiblingObject) {
		this.nextSibling = nextSiblingObject;
	}

	public void setPreviousSibling(PreviousSibling previousSiblingObject) {
		this.previousSibling = previousSiblingObject;
	}

	public void setData(Data dataObject) {
		this.data = dataObject;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
