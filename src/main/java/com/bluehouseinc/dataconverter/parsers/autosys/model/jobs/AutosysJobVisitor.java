package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysBoxJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysCommandLineJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysFileTriggerJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysFileWatcherJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysSqlAgentJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysWindowsServiceMonitoringJob;

public interface AutosysJobVisitor {

	void visit(AutosysBoxJob autosysBoxJob, List<String> lines);

	void visit(AutosysCommandLineJob autosysCommandLineJob, List<String> lines);

	void visit(AutosysFileTriggerJob autosysFileTriggerJob, List<String> lines);

	void visit(AutosysFileWatcherJob autosysFileWatcherJob, List<String> lines);

	void visit(AutosysSqlAgentJob autosysSqlAgentJob, List<String> lines);

	void visit(AutosysWindowsServiceMonitoringJob autosysWindowsServiceMonitoringJob, List<String> lines);
}
