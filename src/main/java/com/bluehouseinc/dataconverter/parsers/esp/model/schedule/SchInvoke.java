package com.bluehouseinc.dataconverter.parsers.esp.model.schedule;

import lombok.Data;

@Data
public class SchInvoke {

	String data;

	String folderName;
	
	public SchInvoke(String data) {

		//INVOKE 'SWS.ESP.APPL.PROCLIB(ANA00010)'
		int ids = data.lastIndexOf("(");
		int idx = data.lastIndexOf(")");
		
		this.folderName = data.substring(ids+1, idx);
		this.data = data;
	}
}
