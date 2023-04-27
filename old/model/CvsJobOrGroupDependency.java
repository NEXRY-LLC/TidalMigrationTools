package com.dataconverter.csv.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CvsJobOrGroupDependency extends CvsAbstractDependency {

	@CsvIgnore
	private CvsAbstractJobOrGroup dependsOnJob;

	@CsvBindByName(column = "operator")
	private int operator;

	@CsvBindByName(column = "status")
	private int status;
	
	@CsvBindByName(column = "depends_on_job_id")
	private int dependsOnJobId;
	
	public CvsJobOrGroupDependency() {
		this.operator=0; // always equals
		this.status=101; // always completed normal
	}
	

	public void setDependsOnJob(CvsAbstractJobOrGroup dependsOnJob) {
		this.dependsOnJob = dependsOnJob;
		this.dependsOnJobId = dependsOnJob.getId();
	}

	
}
