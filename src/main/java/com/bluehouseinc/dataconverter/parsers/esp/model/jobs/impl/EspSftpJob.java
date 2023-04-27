package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitor;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspSftpJob extends EspAbstractJob {

	String remoteDir;
	Map<EspSftpJobOptionalStatement, String> optionalStatements = new HashMap<>();;

	public EspSftpJob(String name) {
		super(name);
	}

	public enum EspSftpJobOptionalStatement {
		CREATETARGETDIRECTORIES, DELETESOURCEFILE, DELETESOURCEDIRECTORY, KEYFILE, LOCALUSER, OSUSER
	}
	
	@Override
	public EspJobType getJobType() {
		return EspJobType.SFTP;
	}
}
