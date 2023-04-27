package com.bluehouseinc.dataconverter.parsers.opc.model;

import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * Extending the OS Job object to add our internal operationName identifier for
 * OPC data. Not needed for conversions but is needed to locate job names
 *
 * @author Brian Hayes
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OPCOSJob extends CsvOSJob {

	String operationName;

	public OPCOSJob(String opname) {
		super();
		this.operationName = opname;
	}
}
