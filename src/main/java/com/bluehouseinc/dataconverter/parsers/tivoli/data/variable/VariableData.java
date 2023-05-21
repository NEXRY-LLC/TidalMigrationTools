package com.bluehouseinc.dataconverter.parsers.tivoli.data.variable;

import lombok.Data;

/*
MAIN_TABLE.APPSIFMX "/apps/ifmx/prod/"
 */
@Data
public class VariableData {

	private String name;
	private String value;

}
