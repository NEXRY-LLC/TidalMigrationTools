package com.bluehouseinc.dataconverter.model.csv;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class NameCsvMapping {

	public NameCsvMapping() {
	}

	public NameCsvMapping(String name) {
		this.name = name;
	}

	@CsvBindByName(column = "NAME")
	String name;

}
