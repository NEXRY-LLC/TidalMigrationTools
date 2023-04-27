package com.bluehouseinc.dataconverter.model.impl;

import org.apache.commons.lang3.builder.ToStringExclude;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvCalendar {

	public CsvCalendar(String name) {
		this.calendarName = name;
	}

	public CsvCalendar() {
	}

	@EqualsAndHashCode.Include
	@CsvBindByName
	String calendarName;

	@ToStringExclude
	@CsvBindByName
	String calendarNotes;

}
