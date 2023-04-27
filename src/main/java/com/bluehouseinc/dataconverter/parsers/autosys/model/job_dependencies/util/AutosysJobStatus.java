package com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util;

import java.util.Arrays;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
public enum AutosysJobStatus {

	SUCCESS("success", "s"), DONE("done", "d"), NOTRUNNING("notrunning", "n"), FAILURE("failure", "f"), TERMINATED("terminated", "t"),
	EXITCODE("exitcode", "e"), VALUE("value", "v");

	final String fullStatus;
	final String code;

	AutosysJobStatus(String fullStatus, String code) {
		this.fullStatus = fullStatus;
		this.code = code;
	}

	public static AutosysJobStatus getAutosysDependencyJobStatusByFullStatus(String fullStatus) {
		return Arrays.stream(values()).filter(jobStatus -> jobStatus.getFullStatus().equals(fullStatus)).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	public static AutosysJobStatus getAutosysDependencyJobStatusByCode(String code) {
		return Arrays.stream(values()).filter(jobStatus -> jobStatus.getCode().equals(code)).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	public static AutosysJobStatus getStatus(String value) {
		if (value.length() == 1) {
			return getAutosysDependencyJobStatusByCode(value);
		}
		return getAutosysDependencyJobStatusByFullStatus(value);
	}

}
