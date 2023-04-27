package com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions;

import lombok.Data;

@Data
public abstract class SchAction{

	protected String data;

	SchAction(String data ){
		this.data = data;
	}


}
