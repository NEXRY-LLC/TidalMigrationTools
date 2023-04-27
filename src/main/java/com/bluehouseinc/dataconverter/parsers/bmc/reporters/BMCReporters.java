package com.bluehouseinc.dataconverter.parsers.bmc.reporters;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.IReporter;

public class BMCReporters implements IModelReport {



	@Override
	public List<IReporter> getReporters() {
		List<IReporter> core = new ArrayList<>();

		core.add(new BMCCountByBMCType());
		core.add(new BMCJobReportPerRunTimeUser());
		core.add(new BMCListOS400LDAJobs());
		core.add(new BMCListOS400MultiStepCount());
		core.add(new BMCListRuntimeUserPerJobType());
		core.add(new BMCNodesPerJobType());
		core.add(new BMCListRerunningJobs());

		return core;
	}




}
