package com.bluehouseinc.dataconverter.model.impl;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvVariable {

	public CsvVariable(String name) {
		this.varName = name;
		this.varValue = "";
	}

	@EqualsAndHashCode.Include
	@CsvBindByName
	String varName;

	@CsvBindByName
	String varValue;

	/**
	 *
	 * @return
	 */
	public String getTokenName() {
		return "$$" + this.varName + "$$";
	}
}
