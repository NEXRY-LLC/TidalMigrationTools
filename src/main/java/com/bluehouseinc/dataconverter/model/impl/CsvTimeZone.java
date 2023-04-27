package com.bluehouseinc.dataconverter.model.impl;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvTimeZone {

	public CsvTimeZone(String name) {
		this.name = name;
	}

	public CsvTimeZone() {
	}

	@EqualsAndHashCode.Include
	@CsvBindByName
	String name;

	@CsvBindByName
	String timezoneId;

}
