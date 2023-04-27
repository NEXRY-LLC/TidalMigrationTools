package com.bluehouseinc.dataconverter.model.impl;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvOwner {

	public CsvOwner(String name) {
		this.ownerName = name;
	}

	@EqualsAndHashCode.Include
	@CsvBindByName
	String ownerName;

}
