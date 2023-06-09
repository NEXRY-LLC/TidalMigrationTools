package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.JobType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvZOSJob extends CsvOSJob {

	@Override
	public JobType getType() {
		return JobType.OSJOB;
	}

}
