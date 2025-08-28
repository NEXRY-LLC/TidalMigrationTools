package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.parsers.tivoli.TivoliDataModel;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.TivoliMainframeOPCDataModel;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl.CA7BaseJobObject;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl.CA7BaseJobObject.JobType;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TivoliJobsByType implements IReporter {

	TivoliMainframeOPCDataModel datamodel;
	List<String> jobcount = new ArrayList<>();
	private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
	
	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		datamodel = (TivoliMainframeOPCDataModel) model;

		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

		Map<JobType, Long> jobTypeCounts  = getJobTypeDistribution();
		long totalJobs = ObjectUtils.toFlatStream(datamodel.getDataObjects()).count();

		jobTypeCounts.entrySet().stream().sorted(Map.Entry.<JobType, Long>comparingByValue().reversed()).forEach(entry -> {
			double percentage = totalJobs > 0 ? (entry.getValue() * 100.0 / totalJobs) : 0;
			log.info("{} {} {}%", entry.getKey().toString(), numberFormat.format(entry.getValue()), percentage);
		});
		
		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

	}


	private Map<JobType, Long> getJobTypeDistribution() {
		return ObjectUtils.toFlatStream(datamodel.getDataObjects()).collect(Collectors.groupingBy(CA7BaseJobObject::getJobType, Collectors.counting()));
	}
	
}
