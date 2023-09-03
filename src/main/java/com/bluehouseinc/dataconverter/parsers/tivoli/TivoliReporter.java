package com.bluehouseinc.dataconverter.parsers.tivoli;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.tivoli.reporter.TivoliJobsWithDoCommandEnvVariables;
import com.bluehouseinc.dataconverter.parsers.tivoli.reporter.TivoliJobsWithDoCommandLawsonToEsp;
import com.bluehouseinc.dataconverter.parsers.tivoli.reporter.TivoliJobsWithDoCommandWithDollarVariable;
import com.bluehouseinc.dataconverter.parsers.tivoli.reporter.TivoliJobsWithMultipleExitCodeStatements;
import com.bluehouseinc.dataconverter.parsers.tivoli.reporter.TivoliJobsWithRecoveryRerun;

public class TivoliReporter implements IModelReport {

	@Override
	public List<IReporter> getReporters() {
		List<IReporter> core = new ArrayList<>();
		core.add(new TivoliJobsWithRecoveryRerun());
		core.add(new TivoliJobsWithMultipleExitCodeStatements());
		core.add(new TivoliJobsWithDoCommandEnvVariables());
		core.add(new TivoliJobsWithDoCommandLawsonToEsp());
		core.add(new TivoliJobsWithDoCommandWithDollarVariable());
		return core;
	}

}
