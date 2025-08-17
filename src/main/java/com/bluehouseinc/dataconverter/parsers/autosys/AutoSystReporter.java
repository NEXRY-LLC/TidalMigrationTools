package com.bluehouseinc.dataconverter.parsers.autosys;


import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.autosys.reports.AutoSysComplexRerunLogicJobPrinter;
import com.bluehouseinc.dataconverter.parsers.autosys.reports.AutoSysJobPrinter;

public class AutoSystReporter implements IModelReport {



	@Override
	public List<IReporter> getReporters() {
		List<IReporter> core = new ArrayList<>();
		core.add(new AutoSysComplexRerunLogicJobPrinter());
		//core.add(new AutoSysJobPrinter());
		
		return core;
	}




}
