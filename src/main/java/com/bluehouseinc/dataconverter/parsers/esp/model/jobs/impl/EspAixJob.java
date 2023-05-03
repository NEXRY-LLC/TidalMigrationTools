package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;



import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspAixJob extends EspOSJOb {

	public EspAixJob(String name) {
		super(name);
	}

}
