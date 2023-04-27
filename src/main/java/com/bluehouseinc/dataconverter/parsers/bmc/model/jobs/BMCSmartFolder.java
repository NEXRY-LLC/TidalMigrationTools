package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SmartFolder;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * We extend OS job Because we will build place holder jobs for all job types.
 *
 * @author Brian Hayes
 *
 */
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class BMCSmartFolder extends BaseBMCJobOrFolder {

	SmartFolder folder;

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.SIMPLEFOLDER;
	}

	@Override
	public String getPlaceHolderData() {
		return null;
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData data) {

	}

	@Override
	public String getNodeName() {
		return this.folder.getNODEID();
	}

	@Override
	public String getVarPrefix() {
		return null;
	}

}
