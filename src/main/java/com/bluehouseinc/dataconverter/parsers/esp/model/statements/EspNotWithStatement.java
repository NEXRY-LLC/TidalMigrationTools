package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspNotWithStatement {

	String jobName;
	String jobGroupName;
	
	public EspNotWithStatement(String jobName, String jobgroupname) {
		this.jobName = jobName;
		this.jobGroupName = jobgroupname;
	}
}
