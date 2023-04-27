package com.bluehouseinc.dataconverter.parsers.esp;


import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspAgentsByJobType;
import com.bluehouseinc.dataconverter.parsers.reporters.GenericJobTypeReporter;

public class EspReporter implements IModelReport {



	@Override
	public List<IReporter> getReporters() {
		List<IReporter> core = new ArrayList<>();
		core.add(new EspAgentsByJobType());
		return core;
	}




}
