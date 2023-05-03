package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.TrueFalse;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.filewatcher.FileActivity;
import com.bluehouseinc.tidal.api.model.job.filewatcher.TimeUnit;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvFileWatcherJob extends BaseCsvJobObject {

	@CsvBindByName
	String directory;

	@CsvBindByName
	String filemask;

	@CsvBindByName
	Integer pollInterval;

	@CsvBindByName
	Integer pollMaxDuration;

	@CsvBindByName
	FileActivity fileActivity;

	@CsvBindByName
	Integer fileActivityInterval;

	@CsvBindByName
	TimeUnit fileActivityTimeUnit;

	@CsvBindByName
	TrueFalse fileExist = TrueFalse.YES; // Default

	@Override
	public JobType getType() {
		return JobType.FILEWATCHER;
	}

}
