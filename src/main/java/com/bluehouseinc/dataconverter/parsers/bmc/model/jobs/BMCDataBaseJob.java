package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class BMCDataBaseJob extends BaseBMCJobOrFolder {

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.DATABASE;
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData d) {
		// throw new RuntimeException("Not used getVarPrefix()");
	}

	@Override
	public String getVarPrefix() {
		return "%%DB-";
	}
}
