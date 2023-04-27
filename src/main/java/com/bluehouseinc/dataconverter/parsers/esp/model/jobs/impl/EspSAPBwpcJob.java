package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;



import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspSAPBwpcJob extends EspSapJob {

	String chain;

	public EspSAPBwpcJob(String name) {
		super(name);
	}

}
