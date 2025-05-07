package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.JobType;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvRecurse;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvOSJob extends BaseCsvJobObject {

	@CsvBindByName
	String commandLine;

	@CsvBindByName
	String paramaters;

	@CsvBindByName
	String workingDirectory;

	@CsvBindByName
	String environmentFile;

	@CsvRecurse
	CsvJobExitCode exitcode;

	@CsvBindByName
	Boolean sourceProfile = false;

	@Override
	public JobType getType() {
		return JobType.OSJOB;
	}

}

