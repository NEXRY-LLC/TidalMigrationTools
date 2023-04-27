package com.bluehouseinc.dataconverter.parsers.bmc.reporters;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCDataModel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BMCCountByBMCType implements IReporter {


	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		BMCDataModel bmc = (BMCDataModel) model;

		log.trace("############################################### Job Total Per Job Type");

		bmc.getJobtypes().keySet().stream().forEach(f -> {
			int cnt = bmc.getJobtypes().get(f).size();
			log.trace(String.format("Type[%s] Total Jobs[%d]\n", f.toString(), cnt));

		});

		log.trace("############################################### Job Total Per Job Type");

	}


}
