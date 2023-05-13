package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysJobVisitor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class AutosysFileWatcherJob extends AutosysAbstractJob {

	String profile;
	String watchInterval;
	String watchFile;
	String watchFileMinSize; //watch_file_min_size

	public AutosysFileWatcherJob(String name) {
		super(name);
	}

	@Override
	public void accept(AutosysJobVisitor autosysJobVisitor, List<String> lines) {
		autosysJobVisitor.visit(this, lines);
	}
}
