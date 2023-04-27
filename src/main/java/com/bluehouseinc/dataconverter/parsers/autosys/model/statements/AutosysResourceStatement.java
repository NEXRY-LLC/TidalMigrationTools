package com.bluehouseinc.dataconverter.parsers.autosys.model.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AutosysResourceStatement {

	String resourceName;
	int quantity;
	String free;

	public AutosysResourceStatement(String resourceName, int quantity) {
		this.resourceName = resourceName;
		this.quantity = quantity;
	}

	public AutosysResourceStatement(String resourceName, int quantity, String free) {
		this.resourceName = resourceName;
		this.quantity = quantity;
		this.free = free;
	}
}
