package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.JobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvPlainJob extends BaseCsvJobObject {

	@Override
	public JobType getType() {
		return JobType.OVMJOB; // TODO: not sure...better check which one it is or introduce new JobType
	}
}
