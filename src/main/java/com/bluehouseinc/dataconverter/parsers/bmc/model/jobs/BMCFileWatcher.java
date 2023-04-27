package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class BMCFileWatcher extends BaseBMCJobOrFolder {

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.FileWatch;
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData d) {

	}

	@Override
	public String getVarPrefix() {
		return "%%FileWatch-";
	}
}
