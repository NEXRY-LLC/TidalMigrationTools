package com.bluehouseinc.dataconverter.parsers.autosys.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.autosys.model.AutosysDataModel;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AutoSysComplexRerunLogicJobPrinter implements IReporter {

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {
		AutosysDataModel bmc = (AutosysDataModel) model;

		log.trace("############################################### COMPLEX RERUN JOBS");
		bmc.getDataObjects().forEach(job -> findJobsWithFalseField(job));
		log.trace("############################################### COMPLEX RERUN JOBS");
	}

	// Best performance - collect matching jobs in one traversal
	public List<AutosysAbstractJob> findJobsWithFalseField(AutosysAbstractJob root) {
		List<AutosysAbstractJob> matches = new ArrayList<>();

		traverse(root, job -> {
			if (job.isComplexTimeSetup()) {
				matches.add(job);
				log.trace(String.format("Job[%s],  StartAfterHour=%s, RunWindow=%s", job.getName(), job.getStartMins(), job.getRunWindow()));
			}
		});

		return matches;
	}

	private void traverse(AutosysAbstractJob job, Consumer<AutosysAbstractJob> action) {
		action.accept(job);
		job.getChildren().forEach(child -> traverse((AutosysAbstractJob) child, action));
	}
}
