package com.bluehouseinc.dataconverter.parsers.esp.model.jobs;

import java.util.HashMap;

import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspLinkProcessData extends EspAbstractJob {

	
	Map<EspLISOptionalStatements, String> optionalStatements = new HashMap<>();
	
	public EspLinkProcessData(String name) {
		super(name);
	}

	public enum EspLISOptionalStatements {
		ECHO, MEMBER
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.LINK_PROCESS;
	}
}
