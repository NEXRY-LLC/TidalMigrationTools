package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BMCBIMJob extends BaseBMCJobOrFolder {

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.BIM;
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData d) {

	}

	@Override
	public String getVarPrefix() {
		return "%%BIM-";
	}
}
