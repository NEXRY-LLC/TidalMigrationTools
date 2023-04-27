package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.JobType;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;

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

	@Override
	public JobType getType() {
		return JobType.OSJOB;
	}

}
