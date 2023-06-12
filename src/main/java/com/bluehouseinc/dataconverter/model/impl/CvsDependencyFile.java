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
public class CvsDependencyFile extends BaseCvsDependency {

	public CvsDependencyFile() {
	}

	@EqualsAndHashCode.Include
	@CsvIgnore
	String filename ;

}
