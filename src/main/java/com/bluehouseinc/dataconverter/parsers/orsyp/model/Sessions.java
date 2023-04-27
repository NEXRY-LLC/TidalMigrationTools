package com.bluehouseinc.dataconverter.parsers.orsyp.model;

// import org.springframework.boot.web.servlet.server.Session;

public class Sessions {
	Session session;
	private String _class;
	private String _id;

	// Getter Methods

	public Session getSession() {
		return session;
	}

	public String get_class() {
		return _class;
	}

	public String get_id() {
		return _id;
	}

	// Setter Methods

	public void setSession(Session SessionObject) {
		this.session = SessionObject;
	}

	public void set_class(String _class) {
		this._class = _class;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
