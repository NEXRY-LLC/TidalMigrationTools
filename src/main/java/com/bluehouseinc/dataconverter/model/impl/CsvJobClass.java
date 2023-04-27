package com.bluehouseinc.dataconverter.model.impl;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvJobClass {

	@CsvBindByName
	@EqualsAndHashCode.Include
	String name;

	public CsvJobClass(String name) {
		this.name = name;
	}
}
