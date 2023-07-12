package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspZosJob extends EspAbstractJob {

	boolean isLinkProcess; // contains LINK PROCESS clause as part of JOB Statement
	boolean isHold; // contains HOLD keyword as part of JOB Statement
	boolean isTask; // contains TASK keyword which indicates that JOB is a task
	boolean isMultipleExitCodes;
	boolean isComplexCcCheck;
	
	String abandon;
	List<String> echos = new ArrayList<>();
	String encParam;
	String dataSet;
	String member;
	String send;
	String deQueue;
	List<CcCheck> ccchks = new ArrayList<>(); // Specify the action taken if a job, step, procstep or program produces a specified condition code.

	String commandLine;
	
	Map<EspZoSOptionalStatements, String> optionalStatements = new HashMap<>();

	public EspZosJob(String name) {
		super(name);
	}

	public enum EspZoSOptionalStatements {
		VINCR, VGET, ESP, VS
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.ZOS;
	}
	
}
