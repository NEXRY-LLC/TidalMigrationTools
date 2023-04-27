package com.dataconverter.csv.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class CvsOSJob extends CvsAbstractJobOrGroup {

	@CsvBindByName(column = "command")
	private String command;

	@CsvBindByName(column = "params")
	private String params;
	
	@CsvBindByName(column = "workingdirectory")
	private String workingDirectory;
	
	
}
