package com.bluehouseinc.dataconverter.api.importer.transformers;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.os.OSJob;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

public class OSJobTransformer implements ITransformer<CsvOSJob, OSJob> {

	OSJob base;
	TidalAPI tidal;

	public OSJobTransformer(OSJob base, TidalAPI tidal) {
		this.base = base;
		this.tidal = tidal;
	}

	@Override
	public OSJob transform(CsvOSJob in) throws TransformationException {

		this.base.setType(JobType.OSJOB);

		if (in.getCommandLine() != null) {
			this.base.setCommand(in.getCommandLine());
		} else {
			this.base.setCommand("ERROR");
		}

		this.base.setParameters(in.getParamaters());
		this.base.setWorkingdirectory(in.getWorkingDirectory());
		this.base.setEnvironmentfile(in.getEnvironmentFile());
		this.base.setAlternateoutputfile(in.getAlternativeOutputFile());
		return this.base;
	}

}
