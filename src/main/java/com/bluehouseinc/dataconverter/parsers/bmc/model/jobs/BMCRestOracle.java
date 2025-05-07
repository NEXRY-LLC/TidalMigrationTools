package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class BMCRestOracle extends BaseBMCJobOrFolder {

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.RESTORACLE;
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData d) {
		throw new RuntimeException("Not used getVarPrefix()");
	}

	@Override
	public String getVarPrefix() {
		return "%%RESTORACLE";
	}
}
