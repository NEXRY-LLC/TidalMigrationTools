package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysJobVisitor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class AutosysCommandLineJob extends AutosysAbstractJob {

	String chkFiles;
	String command;
	String elevated;
	int maxExitSuccess;
	String profile;
	String successCodes;
	String stdErrFile;
	String stdOutFile;

	public AutosysCommandLineJob(String name) {
		super(name);
	}

	@Override
	public void accept(AutosysJobVisitor autosysJobVisitor, List<String> lines, List<AutosysAbstractJob> parents) {
		autosysJobVisitor.visit(this, lines, parents);
	}
}
