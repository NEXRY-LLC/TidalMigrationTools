package com.bluehouseinc.dataconverter.parsers.autosys.reports;



import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.autosys.model.AutosysDataModel;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AutoSysJobPrinter implements IReporter {

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {
		AutosysDataModel bmc = (AutosysDataModel) model;


		bmc.getDataObjects().forEach(job -> doPrint(job, ""));

	}
	
	private void doPrint(AutosysAbstractJob job, String append) {
		log.trace(append+"\t" + job.getName() );
		
		job.getChildren().forEach(child -> doPrint((AutosysAbstractJob) child,"\t"));
	}
}
