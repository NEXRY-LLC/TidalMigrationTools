package com.bluehouseinc.dataconverter.model.impl;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvJobTag {

	public CsvJobTag(String name){
		this.name = name;
	}
	
	@EqualsAndHashCode.Include
	@CsvBindByName
	String name;
	
}
