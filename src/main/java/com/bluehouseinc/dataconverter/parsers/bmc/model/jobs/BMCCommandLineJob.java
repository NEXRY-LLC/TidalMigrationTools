package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BMCCommandLineJob extends BaseBMCJobOrFolder {
	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.OS;
	}

	public String getCommandLine() {
		String cmd = this.getJobData().getCMDLINE();

		if (StringUtils.isBlank(cmd)) {
			cmd = this.getJobData().getMEMNAME();
		}

		return cmd;
	}

	public String getWorkingDir() {
		return this.getJobData().getMEMLIB();
	}

	public void setCommandLine(String command) {
		this.getJobData().setCMDLINE(command);
	}

	@Override
	public String getPlaceHolderData() {
		return "";
	}

	@Override
	public String getVarPrefix() {
		return null;
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData data) {
		throw new RuntimeException("Not used getVarPrefix()");
	}

}
