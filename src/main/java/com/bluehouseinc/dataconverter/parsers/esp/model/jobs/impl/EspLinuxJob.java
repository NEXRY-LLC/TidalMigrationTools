package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspLinuxJob extends EspOSJOb {

	//ABANDON DEPENDENCIES 19.00
	String abandon;
	
	public EspLinuxJob(String name) {
		super(name);
	}

}
