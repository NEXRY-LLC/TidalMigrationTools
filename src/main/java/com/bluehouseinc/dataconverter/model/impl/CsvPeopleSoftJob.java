package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.JobType;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = false)
public class CsvPeopleSoftJob extends BaseCsvJobObject {

	@CsvBindByName
	String extendedInfo;

	@Override
	public JobType getType() {
		return JobType.PSJOB;
	}

}
