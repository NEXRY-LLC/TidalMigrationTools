package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.HashMap;
import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspSecureCopyJob extends EspAbstractJob {

	String remoteDir;
	String remoteName;
	String serverAddr;
	TransferDirection transferDirection;
	String localName;
	Map<EspSecureCopyJobOptionalStatement, String> optionalStatements = new HashMap<>();

	public EspSecureCopyJob(String name) {
		super(name);
	}


	public enum EspSecureCopyJobOptionalStatement {
		KEYFILE, LOCALUSER, OSUSER, SERVERPORT, TARGETOSTYPE
	}

	public enum TransferDirection {
		DOWNLOAD, UPLOAD, D, U
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.SCP;
	}
}
