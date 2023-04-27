package com.bluehouseinc.dataconverter.model.csv;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class NameNewNamePairCsvMapping {

	public NameNewNamePairCsvMapping() {
	}

	public NameNewNamePairCsvMapping(String name, String newname) {
		this.name = name;
		this.newName = newname;
	}

	@CsvBindByName(column = "NAME")
	String name;

	@CsvBindByName(column = "NEWNAME")
	String newName;
}
