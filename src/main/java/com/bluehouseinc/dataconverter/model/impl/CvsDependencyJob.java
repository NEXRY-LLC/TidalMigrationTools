package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.dependency.job.DepLogic;
import com.bluehouseinc.tidal.api.model.dependency.job.DependentJobStatus;
import com.bluehouseinc.tidal.api.model.dependency.job.ExitCodeOperator;
import com.bluehouseinc.tidal.api.model.dependency.job.Operator;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CvsDependencyJob extends BaseCvsDependency {

	public CvsDependencyJob() {
		// THe defacto standards
		this.logic = DepLogic.MATCH;
		this.operator = Operator.EQUAL;
		this.jobStatus = DependentJobStatus.COMPLETED_NORMAL;
	}

	@EqualsAndHashCode.Include
	@CsvIgnore
	BaseCsvJobObject dependsOnJob;

	@CsvBindByName
	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private int dependsOnJobId;

	@CsvBindByName(column = "logic")
	private DepLogic logic;

	@CsvBindByName(column = "operator")
	private Operator operator;

	@CsvBindByName(column = "status")
	private DependentJobStatus jobStatus;

	@CsvBindByName(column = "dateOffset")
	private Integer dateOffset;

	public void setDependsOnJob(BaseCsvJobObject job) {
		this.dependsOnJob = job;
		this.dependsOnJobId = job.getId();
	}

	@CsvBindByName
	private ExitCodeOperator exitCodeOperator;

	@CsvBindByName
	private Integer exitcodeStart;

	@CsvBindByName
	private Integer exitcodeEnd;


	public boolean hasExitCodeLogic() {

		if((exitcodeStart != null) || (exitcodeEnd != null)) {
			return true;
		}

		return false;
	}

}
