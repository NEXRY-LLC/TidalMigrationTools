package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspUnixJob extends EspOSJOb  {

	public EspUnixJob(String name) {
		super(name);
	}


}
