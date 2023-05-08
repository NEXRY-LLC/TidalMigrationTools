package com.bluehouseinc.dataconverter.parsers.esp;


import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspAgentsByJobType;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspComplexSchedEventData;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithAdvancedDelaySubLogic;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithAdvancedDueOutLogic;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithIfLogic;

public class EspReporter implements IModelReport {



	@Override
	public List<IReporter> getReporters() {
		List<IReporter> core = new ArrayList<>();
		core.add(new EspAgentsByJobType());
		core.add(new EspJobsWithIfLogic());
		core.add(new EspJobsWithAdvancedDelaySubLogic());
		core.add(new EspJobsWithAdvancedDueOutLogic());
		core.add(new EspComplexSchedEventData());

		return core;
	}




}
