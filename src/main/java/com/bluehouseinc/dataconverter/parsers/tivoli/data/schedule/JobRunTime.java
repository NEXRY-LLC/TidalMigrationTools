package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule;

import lombok.Data;

@Data
public class JobRunTime {

	String atTime;
	
	
	public JobRunTime(String attime){
		this.atTime = attime;
	}
	
}
