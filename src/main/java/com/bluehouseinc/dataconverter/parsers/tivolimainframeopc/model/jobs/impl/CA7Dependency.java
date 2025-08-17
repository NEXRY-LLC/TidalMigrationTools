package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl;

import lombok.Data;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@ToString
public class CA7Dependency {
	private String predecessorApplicationId;
	private String predecessorWorkstationId;
	private Integer predecessorOperationNumber;
	private String conditionSelection;
	private boolean mandatory;

	// Constructors

	public CA7Dependency() {
	}

	public CA7Dependency(String predecessorWorkstationId, int predecessorOperationNumber) {
		this.predecessorWorkstationId = predecessorWorkstationId;
		this.predecessorOperationNumber = predecessorOperationNumber;
	}

}