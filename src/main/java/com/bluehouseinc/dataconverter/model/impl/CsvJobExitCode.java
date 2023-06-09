package com.bluehouseinc.dataconverter.model.impl;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvJobExitCode {

	
	public CsvJobExitCode(){
		exitLogic = ExitLogic.EQ;
	}
	
	@CsvBindByName
	Integer exitStart;

	@CsvBindByName
	Integer exitEnd;

	@CsvBindByName
	ExitLogic exitLogic;
	
	
	public enum ExitLogic {
		EQ, LT, GT, LTE, GTE, NE, ODD, EVEN
	}

}
