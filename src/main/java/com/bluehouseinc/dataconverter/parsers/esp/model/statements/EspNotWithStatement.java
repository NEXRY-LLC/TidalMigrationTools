package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspNotWithStatement {

	String jobName;

	public EspNotWithStatement(String jobName) {
		this.jobName = jobName;
	}
}
