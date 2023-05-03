package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.HashMap;
import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspTextMonJob extends EspAbstractJob {

	String searchRange;
	String textFile;
	String textString;
	Map<EspTextMonJobOptionalStatement, String> optionalStatements = new HashMap<>();

	public EspTextMonJob(String name) {
		super(name);
	}

	public enum EspTextMonJobOptionalStatement {
		JOBCLASS, TIMEFORMAT, WAITMODE
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.TEXT_MON;
	}
}
