package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspEnvVarStatement;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspWindowsJob extends EspOSJOb  {

	EspEnvVarStatement environmentVariable;


	public EspWindowsJob(String name) {
		super(name);
	}


}
