package com.bluehouseinc.dataconverter.parsers.esp;


import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspAgentsByJobType;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspComplexSchedEventData;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsContainsREALNOWInEarlySub;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsContainsRELDELAY;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithAdvancedDelaySubLogic;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithAdvancedDueOutLogic;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithComplexCccchk;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithIfLogic;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithMultipleExitCodes;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithRequestAttribute;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspJobsWithScopeAttribute;
import com.bluehouseinc.dataconverter.parsers.esp.reporter.EspSAPJobsWithMultipleJobSteps;

public class EspReporter implements IModelReport {



	@Override
	public List<IReporter> getReporters() {
		List<IReporter> core = new ArrayList<>();
		core.add(new EspAgentsByJobType());
		core.add(new EspJobsWithIfLogic());
		core.add(new EspJobsWithAdvancedDelaySubLogic());
		core.add(new EspJobsWithAdvancedDueOutLogic());
		core.add(new EspJobsWithMultipleExitCodes());
		core.add(new EspComplexSchedEventData());
		core.add(new EspJobsWithComplexCccchk());
		core.add(new EspSAPJobsWithMultipleJobSteps());
		core.add(new EspJobsWithRequestAttribute());
		core.add(new EspJobsWithScopeAttribute());
		core.add(new EspJobsContainsREALNOWInEarlySub());
		core.add(new EspJobsContainsRELDELAY());
		return core;
	}




}
