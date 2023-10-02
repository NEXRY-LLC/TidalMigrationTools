package com.bluehouseinc.dataconverter.parsers.tivoli.reporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.tivoli.TivoliDataModel;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TivoliJobsWithConmanCommand implements IReporter {

	String DOLLAR_PATTERN = ".*conman.*";

	TivoliDataModel datamodel;
	List<String> jobcount = new ArrayList<>();

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		datamodel = (TivoliDataModel) model;

		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

		datamodel.getDataObjects().forEach(job -> doCount(job));

		log.trace("TOTAL JOBS {}", jobcount.size());
		jobcount.stream().forEach(c -> log.trace("{}", c));
		log.trace("TOTAL JOBS {}", jobcount.size());

		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

	}

	public void doCount(TivoliJobObject job) {

		if (!StringUtils.isBlank(job.getDoCommand())) {

			Collection<String> data = RegexHelper.extractAllMatches(job.getDoCommand(), DOLLAR_PATTERN);

			data.stream().forEach(f -> {

				if (!jobcount.contains(f)) {
					jobcount.add(f);
				}
			});

		}
		
		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(c -> doCount((TivoliJobObject) c));
		}

	}
}
