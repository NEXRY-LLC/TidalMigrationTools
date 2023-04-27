package com.bluehouseinc.dataconverter.api.reporters;


import java.util.ArrayList;
import java.util.List;

public abstract class TidalModelReporterData {

	public static List<TidalReporter> getReporters() {
		List<TidalReporter> core = new ArrayList<>();

		core.add(new JobCountValidator());
		core.add(new CalendarCountValidator());
		core.add(new OwnerValidator());
		core.add(new DependencyCountValidator());
		core.add(new RunTimeValidator());
		core.add(new NodeValidator());
		core.add(new JobByTypeValidator());
		core.add(new AgentTypeAndJobCountValidator());
		core.add(new TimeZoneValidator());

		return core;
	}
}
