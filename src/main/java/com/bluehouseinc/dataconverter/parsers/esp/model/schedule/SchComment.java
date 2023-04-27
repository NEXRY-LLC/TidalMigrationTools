package com.bluehouseinc.dataconverter.parsers.esp.model.schedule;

import lombok.Data;

@Data
public class SchComment {

	String data;

	public SchComment(String data) {
		this.data = data;
	}

}
