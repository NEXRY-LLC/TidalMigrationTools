package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspSetVarStatement {

	String variableName;
	String variableValue;

	public EspSetVarStatement(String variableName, String variableValue) {
		this.variableName = variableName;
		this.variableValue = variableValue;
	}
}
