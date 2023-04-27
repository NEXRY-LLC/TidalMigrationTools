package com.bluehouseinc.dataconverter.api.reporters;

import com.bluehouseinc.dataconverter.model.TidalDataModel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RunTimeValidator extends TidalReporter {

	@Override
	public void doReport(TidalDataModel model) {

		log.trace("Total Runtime Users to Process [" + model.getRuntimeusers().size() + "]");

		log.trace("Total Runtime Users Processed [" + model.getRuntimeusers().size() + "] ");

		model.getRuntimeusers().stream().forEach(s -> log.trace(s));

		log.trace(" Total Runtime Users Processed [" + model.getRuntimeusers().size() + "] ");
	}

}
