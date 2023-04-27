package com.bluehouseinc.dataconverter.parsers.orsyp.model;

public class Data {
	private String uprocName;
	private String numOrdSession;
	ExecutionContext executionContext;
	Atom atom;
	private String _id;

	// Getter Methods

	public String getUprocName() {
		return uprocName;
	}

	public String getNumOrdSession() {
		return numOrdSession;
	}

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	public Atom getAtom() {
		return atom;
	}

	public String get_id() {
		return _id;
	}

	// Setter Methods

	public void setUprocName(String uprocName) {
		this.uprocName = uprocName;
	}

	public void setNumOrdSession(String numOrdSession) {
		this.numOrdSession = numOrdSession;
	}

	public void setExecutionContext(ExecutionContext executionContextObject) {
		this.executionContext = executionContextObject;
	}

	public void setAtom(Atom atomObject) {
		this.atom = atomObject;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
