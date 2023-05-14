package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysJobVisitor;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.util.AutosysYesNoType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class AutosysFileTriggerJob extends AutosysAbstractJob {

	AutosysYesNoType continuous;
	WatchFileType watchFileType; //watch_file_type
	AutosysYesNoType watchFileRecursive; //watch_file_recursive: Y|N
	WatchFileChangeType watchFileChangeType;
	int watchFileChangeValue;
	String watchFile;
	String watchFileOwner; //watch_file_owner
	String watchFileWinUser; //watch_file_win_user
	String watchNoChange; // watch_no_change

	public AutosysFileTriggerJob(String name) {
		super(name);
	}

	@Override
	public void accept(AutosysJobVisitor autosysJobVisitor, List<String> lines) {
		autosysJobVisitor.visit(this, lines);
	}

	public enum WatchFileType {
		CREATE, DELETE, EXIST, EXPAND, NOTEXIST, SHRINK, UPDATE, GENERATE
	}

	public enum WatchFileChangeType {
		DELTA, PERCENT, SIZE
	}
}
