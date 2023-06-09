package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;


import java.util.HashMap;
import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspAs400Job extends EspAbstractJob {

	String as400File;
	String clpName;
	String command;
	String As400Lib;
	String ccExit;
	String Curlib;

	Map<EspAs400JobOptionalStatement, String> optionalStatements = new HashMap<>();

	public EspAs400Job(String name) {
		super(name);
	}

	public enum EspAs400JobOptionalStatement {
		AS400LIB, CCEXIT, CURLIB, JOBD, JOBNAME, JOBQ, LDA, LIBL, OTHERS, PARAM,PRIORITY, PROCESS_PRIORITY, USER
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.AS400;
	}


}
