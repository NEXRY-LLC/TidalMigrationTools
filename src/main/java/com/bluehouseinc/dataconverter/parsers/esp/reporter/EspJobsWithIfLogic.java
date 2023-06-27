package com.bluehouseinc.dataconverter.parsers.esp.reporter;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspDataModel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EspJobsWithIfLogic implements IReporter {
	EspDataModel espmodel;

	List<String> jobcount = new ArrayList<>();

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		espmodel = (EspDataModel) model;

		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

		espmodel.getDataObjects().forEach(f -> doCountIfLogicJobs(f));

		jobcount.forEach(f -> {

			log.trace("{}", f);


		});

		jobcount.clear();
		
		
		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

		
		log.trace("#######################################MODIFIED IF {}#######################################", this.getClass().getSimpleName());

		
		
		espmodel.getDataObjects().forEach(f -> doCountIfLogicCleaned(f));

		jobcount.forEach(f -> {

			log.trace("{}", f);


		});
		
		
		log.trace("#######################################MODIFIED IF {}#######################################", this.getClass().getSimpleName());
		
	}

	public void doCountIfLogicJobs(EspAbstractJob job) {



		if (job.isContainsIfLogic()) {
			String key = job.getFullPath();
			if (!jobcount.contains(key)) {
				jobcount.add(key);
			}

		}

		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(c -> doCountIfLogicJobs((EspAbstractJob) c));
		}
	}

	
	public void doCountIfLogicCleaned(EspAbstractJob job) {



		if (job.isContainsIfLogicCleaned()) {
			String key = job.getFullPath();
			if (!jobcount.contains(key)) {
				jobcount.add(key);
			}

		}

		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(c -> doCountIfLogicCleaned((EspAbstractJob) c));
		}
	}
	
}
