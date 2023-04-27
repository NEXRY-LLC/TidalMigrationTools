package com.dataconverter.csv.model;

import lombok.Data;

import com.opencsv.bean.CsvBindByName;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CvsVariable {

	@CsvBindByName(column = "name")
	private String name;

	@CsvBindByName(column = "value")
	private String value;

	/**
	 * 
	 * @return 
	 */
	public String getTokenName() {
		return "%%"+this.name+"%%";
	}
}
