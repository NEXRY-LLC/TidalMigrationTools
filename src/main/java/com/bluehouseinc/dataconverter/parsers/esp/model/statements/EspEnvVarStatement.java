package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspEnvVarStatement {

	String environmentVariableName;
	String environmentVariableValue;

	public EspEnvVarStatement(String environmentVariableName, String environmentVariableValue) {
		this.environmentVariableName = environmentVariableName;
		this.environmentVariableValue = environmentVariableValue;
	}
}
