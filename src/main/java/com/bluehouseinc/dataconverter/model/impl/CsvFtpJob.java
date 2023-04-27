package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.JobType;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvRecurse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvFtpJob extends BaseCsvJobObject {

	@CsvBindByName
	Boolean anonymous;

	@CsvBindByName
	Boolean asciiformat;

	@CsvBindByName
	String ftphost;

	@CsvRecurse
	CsvRuntimeUser remoteuser;

	@CsvRecurse
	FTPOperation ftpOperation;

	@CsvRecurse
	FtpProtocol ftpProtocol;

	@CsvRecurse
	Boolean userPasswordAuthetication;

	@CsvBindByName
	String fileName;

	@CsvBindByName
	String localPath;

	@CsvBindByName
	String remotePath;

	@CsvBindByName
	String privateKeyFileLocation;

	@CsvBindByName
	String privateKeyFilePassPhrase;

	@Override
	public JobType getType() {
		return JobType.FTPJOB;
	}

	public enum FTPOperation {
		GET, MGET, PUT, MPUT;
	}

	public enum FtpProtocol {

		ftp, sftp, ftps, local
	}

}
