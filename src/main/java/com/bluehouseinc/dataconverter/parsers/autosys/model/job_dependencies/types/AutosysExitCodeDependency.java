package com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.AutosysBaseDependency;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class AutosysExitCodeDependency extends AutosysBaseDependency {



	// TODO: Fix this RegEx to match following expression:
	// 	(n(EMB_FACE_1150_910.ETS_CNY_CAPS_ACTV_IN_KILLSET) & e(EMB_FACE_1150_005.ETS_EMBX_CNY_CAPS_ACTV_IN) = 1012 & s(EMB_FACE_1150_007.ETS_EMBX_CNY_GHIPPO_NYCAPS_IN)) | (n(EMB_FACE_1150_910.ETS_CNY_CAPS_ACTV_IN_KILLSET) & s(EMB_FACE_1150_005.ETS_EMBX_CNY_CAPS_ACTV_IN) & e(EMB_FACE_1150_007.ETS_EMBX_CNY_GHIPPO_NYCAPS_IN) = 1012)
	//  ...add something like `Positive Lookahead Token` into RegEx for matching more than one occurrence of it!
	public static final String EXIT_CODE_DEPENDENCY_REGEX = "(e)(\\((?>[^()]+)*\\))\\s(=|<=|>=|<|>)\\s([0-9]*)";
	public static final Pattern EXIT_CODE_DEPENDENCY_PATTERN = Pattern.compile(EXIT_CODE_DEPENDENCY_REGEX);

	int exitCode;
	AutosysExitCodeDependencyOperator operator;

	public AutosysExitCodeDependency(String jobName) {
		super(jobName);
	}

	public AutosysExitCodeDependency(String jobName, int exitCode) {
		super(jobName);
		this.exitCode = exitCode;
	}

	public AutosysExitCodeDependency(String jobName, AutosysExitCodeDependencyOperator operator, int exitCode) {
		super(jobName);
		this.exitCode = exitCode;
		this.operator = operator;
	}

	@Getter
	public enum AutosysExitCodeDependencyOperator {
		EQUALS("EQUALS", "="), DOES_NOT_EQUAL("DOES_NOT_EQUAL", "!="), LESS_THAN("LESS_THAN", "<"), GREATER_THAN("GREATER_THAN", ">"),
		LESS_THAN_OR_EQUAL_TO("LESS_THAN_OR_EQUAL_TO", "<="), GREATER_THAN_OR_EQUAL_TO("GREATER_THAN_OR_EQUAL_TO", ">=");

		final String name;
		final String expression;

		AutosysExitCodeDependencyOperator(String name, String expression) {
			this.name = name;
			this.expression = expression;
		}

		private static AutosysExitCodeDependencyOperator getOperatorByExpression(String expression) {
			return Arrays.stream(values()).filter(operator -> operator.getExpression().equals(expression)).findFirst()
					.orElseThrow(IllegalArgumentException::new);
		}

		private static AutosysExitCodeDependencyOperator getOperatorByName(String name) {
			return Arrays.stream(values()).filter(operator -> operator.getName().equals(name)).findFirst().orElseThrow(IllegalArgumentException::new);
		}

		public static AutosysExitCodeDependencyOperator getOperator(String value) {
			if (value.length() <= 2) {
				return getOperatorByExpression(value);
			}
			return getOperatorByName(value);
		}
	}

}
