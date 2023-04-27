package com.bluehouseinc.dataconverter.model.impl;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvRecurse;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvActionEmail {

	@EqualsAndHashCode.Include
	@CsvBindByName
	String name;

	@CsvBindByName
	String toEmailAddresses;

	@CsvBindByName
	String subject;

	@CsvBindByName
	String message;

	@CsvRecurse
	CsvOwner owner;

}
