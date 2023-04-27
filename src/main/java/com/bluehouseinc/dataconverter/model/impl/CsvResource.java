package com.bluehouseinc.dataconverter.model.impl;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvRecurse;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvResource {

	public CsvResource(String name, CsvOwner owner) {
		this.name = name;
		this.limit = 1;
		this.owner = owner;
	}

	@EqualsAndHashCode.Include
	@CsvBindByName
	String name;

	@CsvBindByName
	Integer limit;

	@CsvRecurse
	CsvOwner owner;

}
