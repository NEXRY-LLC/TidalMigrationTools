package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.JobType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvMsSqlJob extends BaseCsvJobObject {

	@Override
	public JobType getType() {
		return JobType.DATAMOVERJOB; // what is exactly a Data Mover job?
	}
}
