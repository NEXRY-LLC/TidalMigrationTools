package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitor;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspDStrigJob extends EspAbstractJob {

	// The DSNAME statement specifies the requirements of the data set trigger or USS file trigger.
	// When these requirements are met, ESP completes the trigger object.
	String dsName; // E.g.: DSNAME 'PS.PRD.WEEKLY.INDUCTN.FROMSAP.G-'

	public EspDStrigJob(String name) {
		super(name);
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.DSTRIG;
	}
}
