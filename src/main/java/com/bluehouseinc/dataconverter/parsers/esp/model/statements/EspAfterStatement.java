package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobDependencyTerminationStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspAfterStatement {

	String jobName;
	EspJobDependencyTerminationStatus terminationStatus;

	public EspAfterStatement(String jobName, EspJobDependencyTerminationStatus terminationStatus) {
		this.jobName = jobName;
		this.terminationStatus = terminationStatus;
	}
}
