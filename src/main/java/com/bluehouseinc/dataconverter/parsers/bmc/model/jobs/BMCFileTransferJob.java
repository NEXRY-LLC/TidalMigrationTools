package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.ftp.FtpObject;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class BMCFileTransferJob extends BaseBMCJobOrFolder {

	FtpObject ftpObject;

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.FILE_TRANS;
	}

	public FtpObject getFtpObject() {
		if (this.ftpObject == null) {
			this.ftpObject = new FtpObject(getJobSpecificVariables());
		}

		return this.ftpObject;
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData d) {
		// Not needed here..
	}

	@Override
	public String getVarPrefix() {
		return "%%FTP-";
	}
}
