package com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum AutosysDependencyLogicOperator {
	AND("&", "and"), OR("|", "or");

	final String expression;
	final String operatorName;

	AutosysDependencyLogicOperator(String expression, String operatorName) {
		this.expression = expression;
		this.operatorName = operatorName;
	}

	static AutosysDependencyLogicOperator getAutosysDependencyLogicOperatorByExpression(String expression) {
		return Arrays.stream(values()).filter(logicOperator -> logicOperator.getExpression().equals(expression)).findFirst()
				.orElseThrow(IllegalArgumentException::new);
	}

	static AutosysDependencyLogicOperator getAutosysDependencyLogicOperatorByOperatorName(String operatorName) {
		return Arrays.stream(values()).filter(logicOperator -> logicOperator.getOperatorName().equals(operatorName)).findFirst()
				.orElseThrow(IllegalArgumentException::new);
	}

	public static AutosysDependencyLogicOperator getAutosysDependencyLogicOperator(String criteria) {
		if (criteria.length() == 1) {
			return getAutosysDependencyLogicOperatorByExpression(criteria);
		}
		return getAutosysDependencyLogicOperatorByOperatorName(criteria);
	}

}
