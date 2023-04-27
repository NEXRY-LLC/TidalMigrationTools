package com.bluehouseinc.dataconverter.parsers.esp.model.schedule;

import lombok.Data;

@Data
public class SchCalendar {


	String data;

	public SchCalendar(String data) {
		this.data = data;
	}

}
