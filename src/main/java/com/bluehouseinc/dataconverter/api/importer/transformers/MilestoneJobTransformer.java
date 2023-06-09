package com.bluehouseinc.dataconverter.api.importer.transformers;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.impl.CsvFileWatcherJob;
import com.bluehouseinc.dataconverter.model.impl.CsvMilestoneJob;
import com.bluehouseinc.tidal.api.model.TrueFalse;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.filewatcher.FileWatcherJob;
import com.bluehouseinc.tidal.api.model.job.filewatcher.TimeUnit;
import com.bluehouseinc.tidal.api.model.job.milestone.MilestoneJob;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

public class MilestoneJobTransformer implements ITransformer<CsvMilestoneJob, MilestoneJob> {

	MilestoneJob base;
	TidalAPI tidal;

	public MilestoneJobTransformer(MilestoneJob base, TidalAPI tidal) {
		this.base = base;
		this.tidal = tidal;
	}

	@Override
	public MilestoneJob transform(CsvMilestoneJob in) throws TransformationException {

		this.base.setType(JobType.MILESTONE);

		
		return this.base;
	}

}
