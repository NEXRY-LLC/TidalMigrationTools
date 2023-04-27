package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspNoRunStatement {

	String criteria;

	public EspNoRunStatement(String criteria) {
		this.criteria = criteria;
	}
}
