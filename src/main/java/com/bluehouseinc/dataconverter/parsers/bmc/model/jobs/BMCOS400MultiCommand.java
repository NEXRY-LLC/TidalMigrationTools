package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class BMCOS400MultiCommand extends BaseBMCJobOrFolder {

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.OS400MULTICOMMAND;
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData d) {
		throw new RuntimeException("Not used getVarPrefix()");
	}

	@Override
	public String getVarPrefix() {
		throw new RuntimeException("Not used getVarPrefix()");
	}

}
