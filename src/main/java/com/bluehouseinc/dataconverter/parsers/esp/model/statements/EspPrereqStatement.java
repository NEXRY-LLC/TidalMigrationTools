package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobDependencyTerminationStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspPrereqStatement {

	String fullName;
	EspJobDependencyTerminationStatus terminationStatus;

	public EspPrereqStatement(String fullName, EspJobDependencyTerminationStatus terminationStatus) {
		this.fullName = fullName;
		this.terminationStatus = terminationStatus;
	}
}
