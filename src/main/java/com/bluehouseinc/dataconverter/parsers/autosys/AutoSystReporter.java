package com.bluehouseinc.dataconverter.parsers.autosys;


import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.IReporter;

public class AutoSystReporter implements IModelReport {



	@Override
	public List<IReporter> getReporters() {
		List<IReporter> core = new ArrayList<>();

		core.add(new AutoSysJobPrinter());
		
		return core;
	}




}
