package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.HashMap;
import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspSapEventJob extends EspAbstractJob {

	String event;
	Map<EspSapEventJobOptionalStatement, String> optionalStatements = new HashMap<>();

	public EspSapEventJob(String name) {
		super(name);
	}

	public enum EspSapEventJobOptionalStatement {
		RFCDEST, SAPCLIENT, SAPLANGUAGE, SAPUSER
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.SAPE;
	}
}
