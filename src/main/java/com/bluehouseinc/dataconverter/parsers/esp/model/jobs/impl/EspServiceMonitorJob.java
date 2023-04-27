package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitor;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspServiceMonitorJob extends EspAbstractJob {

	String serviceName;
	String status;
	Map<EspServiceMonitorJobOptionalStatement, String> optionalStatements = new HashMap<>();;

	public EspServiceMonitorJob(String name) {
		super(name);
	}

	public enum EspServiceMonitorJobOptionalStatement {
		JOBCLASS, USER
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.SERVICE_MON;
	}
}
