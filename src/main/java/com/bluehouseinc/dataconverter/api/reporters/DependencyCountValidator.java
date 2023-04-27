package com.bluehouseinc.dataconverter.api.reporters;

import com.bluehouseinc.dataconverter.model.TidalDataModel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DependencyCountValidator extends TidalReporter{

	@Override
	public void doReport(TidalDataModel model) {

		log.trace("Total Dependencies To Process [" + model.getDependencies().size() + "]");

	}
}
