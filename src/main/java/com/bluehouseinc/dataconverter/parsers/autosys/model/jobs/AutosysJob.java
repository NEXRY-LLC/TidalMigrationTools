package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs;

import java.util.List;

public interface AutosysJob {

	// It is mandatory to pass in `parents` as param to keep track of parent-child relationship
	void accept(AutosysJobVisitor autosysJobVisitor, List<String> lines);

}
