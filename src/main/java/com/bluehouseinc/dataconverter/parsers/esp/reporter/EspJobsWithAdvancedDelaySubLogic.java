package com.bluehouseinc.dataconverter.parsers.esp.reporter;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspDataModel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EspJobsWithAdvancedDelaySubLogic implements IReporter {
	EspDataModel espmodel;

	List<String> jobcount = new ArrayList<>();

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		espmodel = (EspDataModel) model;

		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

		espmodel.getDataObjects().forEach(f -> doCount(f));

		jobcount.forEach(f -> {

			log.trace("{}", f);


		});

		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

	}

	public void doCount(EspAbstractJob job) {

		if(job.getFullPath().contains("RESTENC6")) {
			job.getName().getBytes();
		}


		if (job.isContainsAdvancedDueOutLogic()) {
			String key = job.getFullPath();
			if (!jobcount.contains(key)) {
				jobcount.add(key);
			}

		}

		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(c -> doCount((EspAbstractJob) c));
		}
	}

}
