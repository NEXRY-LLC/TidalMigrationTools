package com.dataconverter.csv.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public abstract class CvsAbstractDependency {

	@CsvIgnore
	private CvsAbstractJobOrGroup job;

	@CsvBindByName(column = "job_id")
	private int jobId;
	
	
	public CvsAbstractJobOrGroup getJob() {
		return job;
	}


	public void setJob(CvsAbstractJobOrGroup job) {
		this.job = job;
		this.jobId = job.getId();
	}

}
