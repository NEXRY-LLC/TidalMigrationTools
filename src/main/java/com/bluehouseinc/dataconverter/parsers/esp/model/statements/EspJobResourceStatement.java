package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspJobResourceStatement {

	int limit;
	String resourceName;

	public EspJobResourceStatement(int limit, String resourceName) {
		this.limit = limit;
		this.resourceName = resourceName;
	}

}
