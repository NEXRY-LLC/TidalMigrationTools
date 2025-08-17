package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.IReporter;

import lombok.extern.log4j.Log4j2;

/**
 * Reporter for Tivoli Mainframe OPC parser.
 * Generates analysis reports on parsed data including job counts, dependencies, and resources.
 */
@Log4j2

public class TivoliMainframeOPCReporter implements IModelReport {
	@Override
	public List<IReporter> getReporters() {

		return new ArrayList<IReporter>();
	}

}
