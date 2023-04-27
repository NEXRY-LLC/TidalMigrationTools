package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysJobVisitor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class AutosysWindowsServiceMonitoringJob extends AutosysAbstractJob {

	MonitorModeType monitorMode;
	String winServiceName;
	AutosysWinServiceStatus winServiceStatus;

	public AutosysWindowsServiceMonitoringJob(String name) {
		super(name);
	}

	@Override
	public void accept(AutosysJobVisitor autosysJobVisitor, List<String> lines, List<AutosysAbstractJob> parents) {
		autosysJobVisitor.visit(this, lines, parents);
	}

	// https://techdocs.broadcom.com/us/en/ca-enterprise-software/intelligent-automation/autosys-workload-automation/12-0-01/reference/ae-job-information-language/jil-job-definitions/win-service-status-attribute-specify-the-status-of-the-windows-service-to-be-monitored.html
	public enum AutosysWinServiceStatus {
		RUNNING, STOPPED, CONTINUE_PENDING, PAUSE_PENDING, PAUSED, START_PENDING, STOP_PENDING, EXISTS, NOTEXISTS
	}

	public enum MonitorModeType {
		WAIT, NOW
	}
}
