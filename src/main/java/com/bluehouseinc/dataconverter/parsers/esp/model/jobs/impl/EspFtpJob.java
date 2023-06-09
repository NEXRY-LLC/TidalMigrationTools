package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspFtpJob extends EspAbstractJob {

	String remoteFileName;
	String serverPort;
	String ftpFormat;
	String localFileName;
	TransferDirection transferDirection;

	public EspFtpJob(String name) {
		super(name);
	}


	public enum TransferDirection {
		DOWNLOAD, UPLOAD, D, U
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.FTP;
	}

}
