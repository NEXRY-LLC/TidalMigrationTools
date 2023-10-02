package com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types;

import java.util.regex.Pattern;

import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.AutosysBaseDependency;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class AutosysVariableDependency extends AutosysBaseDependency {

	public static final String VARIABLE_DEPENDENCY_REGEX = "(v|value)(\\((?>[^()]+)*\\))\\s=\\s(\\\"\\w+\")";
	public static final Pattern VARIABLE_DEPENDENCY_PATTERN = Pattern.compile(VARIABLE_DEPENDENCY_REGEX);

	String variableValue;

	public AutosysVariableDependency(String dependencyName) {
		super(dependencyName);
	}

	public AutosysVariableDependency(String variableName, String variableValue) {
		super(variableName);
		this.variableValue = variableValue;
	}

}
