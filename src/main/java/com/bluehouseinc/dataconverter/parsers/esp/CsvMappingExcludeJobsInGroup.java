package com.bluehouseinc.dataconverter.parsers.esp;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class CsvMappingExcludeJobsInGroup {

	public CsvMappingExcludeJobsInGroup() {
	}

	public CsvMappingExcludeJobsInGroup(String groupname, String jobname) {
		this.groupName = groupname;
		this.jobName = jobname;
	}

	@CsvBindByName(column = "GROUPNAME")
	String groupName;

	@CsvBindByName(column = "JOBNAME")
	String jobName;
}
