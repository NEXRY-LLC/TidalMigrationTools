package com.bluehouseinc.dataconverter.model.csv;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class NameValuePairCsvMapping {

	public NameValuePairCsvMapping() {
	}

	public NameValuePairCsvMapping(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@CsvBindByName(column = "NAME")
	String name;

	@CsvBindByName(column = "VALUE")
	String value;
}
