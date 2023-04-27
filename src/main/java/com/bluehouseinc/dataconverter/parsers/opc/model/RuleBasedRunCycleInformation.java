package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class RuleBasedRunCycleInformation implements Serializable {
	// 1st row
	private String validFrom;
	private String appStatus;
	private String ruleDefinition;
	private String inputArrive;
	private String deadline;
	private String freeDayRule;

	// 2nd row optional description
	private String runCycleDescription;

	// 2nd|3rd row rule
	private AdRule adRule;
}
