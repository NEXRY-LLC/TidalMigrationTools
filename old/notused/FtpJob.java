package com.dataconverter.csv.model.notused;

import com.dataconverter.csv.model.CvsAbstractJobOrGroup;


public class FtpJob extends CvsAbstractJobOrGroup {

	private String remotehost;
	private String remotelogin;
	
	
	public String getRemotehost() {
		return remotehost;
	}
	public void setRemotehost(String remotehost) {
		this.remotehost = remotehost;
	}
	public String getRemotelogin() {
		return remotelogin;
	}
	public void setRemotelogin(String remotelogin) {
		this.remotelogin = remotelogin;
	}
	
}
