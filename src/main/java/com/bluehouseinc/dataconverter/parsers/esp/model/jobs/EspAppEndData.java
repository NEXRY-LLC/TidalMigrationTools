package com.bluehouseinc.dataconverter.parsers.esp.model.jobs;

import java.util.HashMap;

import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspAppEndData extends EspAbstractJob {

	
	Map<EspOptionalStatements, String> optionalStatements = new HashMap<>();
	
	public EspAppEndData(String name) {
		super(name);
	}

	public enum EspOptionalStatements {
		ESPNOMSG
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.APPLEND;
	}

}
