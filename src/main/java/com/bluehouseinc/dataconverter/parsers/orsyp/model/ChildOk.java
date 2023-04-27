package com.bluehouseinc.dataconverter.parsers.orsyp.model;

// public class Data {
// private String uprocName;
// private String numOrdSession;
// ExecutionContext ExecutionContextObject;
// Atom AtomObject;
// private String _id;
//
//
// // Getter Methods
//
// public String getUprocName() {
// return uprocName;
// }
//
// public String getNumOrdSession() {
// return numOrdSession;
// }
//
// public ExecutionContext getExecutionContext() {
// return ExecutionContextObject;
// }
//
// public Atom getAtom() {
// return AtomObject;
// }
//
// public String get_id() {
// return _id;
// }
//
// // Setter Methods
//
// public void setUprocName(String uprocName) {
// this.uprocName = uprocName;
// }
//
// public void setNumOrdSession(String numOrdSession) {
// this.numOrdSession = numOrdSession;
// }
//
// public void setExecutionContext(ExecutionContext executionContextObject) {
// this.ExecutionContextObject = executionContextObject;
// }
//
// public void setAtom(Atom atomObject) {
// this.AtomObject = atomObject;
// }
//
// public void set_id(String _id) {
// this._id = _id;
// }
// }
// public class Atom {
// private String _reference;
//
//
// // Getter Methods
//
// public String get_reference() {
// return _reference;
// }
//
// // Setter Methods
//
// public void set_reference(String _reference) {
// this._reference = _reference;
// }
// }
// public class ExecutionContext {
// private String type;
// SyntaxCheck SyntaxCheckObject;
// private String _id;
//
//
// // Getter Methods
//
// public String getType() {
// return type;
// }
//
// public SyntaxCheck getSyntaxCheck() {
// return SyntaxCheckObject;
// }
//
// public String get_id() {
// return _id;
// }
//
// // Setter Methods
//
// public void setType(String type) {
// this.type = type;
// }
//
// public void setSyntaxCheck(SyntaxCheck syntaxCheckObject) {
// this.SyntaxCheckObject = syntaxCheckObject;
// }
//
// public void set_id(String _id) {
// this._id = _id;
// }
// }
// public class SyntaxCheck {
// private String current;
// private String previous;
// private String _id;
//
//
// // Getter Methods
//
// public String getCurrent() {
// return current;
// }
//
// public String getPrevious() {
// return previous;
// }
//
// public String get_id() {
// return _id;
// }
//
// // Setter Methods
//
// public void setCurrent(String current) {
// this.current = current;
// }
//
// public void setPrevious(String previous) {
// this.previous = previous;
// }
//
// public void set_id(String _id) {
// this._id = _id;
// }
// }
public class ChildOk {
	Root root;
	Parent parent;
	NextSibling nextSibling;
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

	public void setData(Data dataObject) {
		this.data = dataObject;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
