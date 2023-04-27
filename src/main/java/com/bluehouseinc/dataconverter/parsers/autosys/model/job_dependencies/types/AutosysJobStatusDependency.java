package com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types;

import java.util.regex.Pattern;

import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.AutosysBaseDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util.AutosysJobStatus;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class AutosysJobStatusDependency extends AutosysBaseDependency {


	public final static String JOB_STATUS_REGEX = "(s|success|n|notrunning|f|failure|t|terminated)";
	@Getter(AccessLevel.PRIVATE)
	@Setter(AccessLevel.PRIVATE)
	private String expression = "\\((.*)\\)\\s(\\&|\\|)*(\\))*";
	public static final String JOB_STATUS_DEPENDENCY_REGEX = JOB_STATUS_REGEX + "\\((.*)\\)\\s(\\&|\\|)*";
	public static final Pattern JOB_STATUS_DEPENDENCY_PATTERN = Pattern.compile(JOB_STATUS_DEPENDENCY_REGEX);

	// TODO: Modify this data structure, so it can store values of multiple operators (check )
	private AutosysJobStatus status;

	public AutosysJobStatusDependency(String dependencyName) {
		super(dependencyName);
	}

	public AutosysJobStatusDependency(String dependencyName, AutosysJobStatus status) {
		super(dependencyName);
		this.status = status;
	}

}
