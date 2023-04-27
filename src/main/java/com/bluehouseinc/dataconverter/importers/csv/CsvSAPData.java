package com.bluehouseinc.dataconverter.importers.csv;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CsvSAPData {
	// JOBNAME,PROGNAME,VARIANT,PDEST,PRCOP,PLIST,PRTXT,PRBER

	@Include
	@CsvBindByName(column = "JOBNAME")
	String jobName;

	@CsvBindByName(column = "PROGNAME")
	String programName;

	@CsvBindByName(column = "VARIANT")
	String variant;

	@CsvBindByName(column = "PDEST")
	String pdest;

	@CsvBindByName(column = "PRCOP")
	String prcop;

	@CsvBindByName(column = "PLIST")
	String plist;

	@CsvBindByName(column = "PRTXT")
	String prtxt;

	@CsvBindByName(column = "PRBER")
	String prber;

}
