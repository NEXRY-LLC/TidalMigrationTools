package com.bluehouseinc.dataconverter.parsers.bmc.reporters;

import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCDataModel;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOS400Job;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.util.ObjectUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BMCListOS400LDAJobs implements IReporter {


	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {
		BMCDataModel bmc = (BMCDataModel) model;
		log.trace("############################################### OS400 LDA JOBS");

		List<BaseBMCJobOrFolder> data = ObjectUtils.toFlatListByUnknownType(bmc.getDataObjects(), BMCOS400Job.class);
		log.trace("Todal OS400 Jobs + " + data.size());

		data.forEach(j -> {
			BMCOS400Job os = (BMCOS400Job) j;
			if (!os.getLdaData().isEmpty()) {
				log.trace(j.getFullPath());
			}
		});

		log.trace("############################################### OS400 LDA JOBS");

	}
}
