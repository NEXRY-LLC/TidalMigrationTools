package com.bluehouseinc.dataconverter.api.importer.transformers;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.impl.CsvFtpJob;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.ftp.FTPJob;
import com.bluehouseinc.tidal.api.model.job.ftp.FtpOperationType;
import com.bluehouseinc.tidal.api.model.job.ftp.FtpProtocolType;
import com.bluehouseinc.tidal.api.model.users.Users;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

public class FTPJobTransformer implements ITransformer<CsvFtpJob, FTPJob> {

	FTPJob base;
	TidalAPI tidal;

	public FTPJobTransformer(FTPJob base, TidalAPI tidal) {
		this.base = base;
		this.tidal = tidal;
	}

	@Override
	public FTPJob transform(CsvFtpJob in) throws TransformationException {

		this.base.setType(JobType.FTPJOB);

		if (in.getAnonymous()) {
			this.base.setAnonymous(YesNoType.YES);
		} else {
			this.base.setAnonymous(YesNoType.NO);
		}

		if (in.getAsciiformat()) {
			this.base.setAsciiformat(YesNoType.YES);
		} else {
			this.base.setAsciiformat(YesNoType.NO);
		}

		this.base.setFilename(in.getFileName());

		this.base.setFtphost(in.getFtphost());

		switch (in.getFtpOperation()) {
		case GET:
			this.base.setFtpoperation(FtpOperationType.GET);
			break;

		case MGET:
			this.base.setFtpoperation(FtpOperationType.MGET);
			break;

		case PUT:
			this.base.setFtpoperation(FtpOperationType.PUT);
			break;

		case MPUT:
			this.base.setFtpoperation(FtpOperationType.MPUT);
			break;

		default:
			throw new TidalException("Missing getFtpOperation");
		}

		switch (in.getFtpProtocol()) {
		case ftp:
			this.base.setFtpprotocol(FtpProtocolType.ftp);
			break;

		case ftps:
			this.base.setFtpprotocol(FtpProtocolType.ftps);
			break;

		case sftp:
			this.base.setFtpprotocol(FtpProtocolType.sftp);
			if (!StringUtils.isBlank(in.getPrivateKeyFileLocation())) {
				this.base.setPrivatekeylocation(in.getPrivateKeyFileLocation());
				this.base.setPassphrase("bluehouse");
				this.base.setUserpasswordauthentication(YesNoType.NO);
			} else {
				this.base.setUserpasswordauthentication(YesNoType.YES);
			}

			break;
		case local:
			this.base.setFtpprotocol(FtpProtocolType.ftp);
			break;

		default:
			throw new TidalException("Missing getFtpProtocol");
		}

		this.base.setLocalpath(in.getLocalPath());

		this.base.setRemotepath(in.getRemotePath());

		String username = null;

		if (in.getRemoteuser() != null) {
			username = in.getRemoteuser().getRunTimeUserName();
		}

		if (username != null) {
			// System.out.println("TRANSFORMER Locating RTE[" + username + "] FOR
			// JOB["+in.getName()+"]");
			Users usr = this.tidal.getUserByAccountNameAndDomain(username,null);

			if (usr != null) {
				this.base.setFtpuserid(usr.getId());
				this.base.setAnonymous(YesNoType.NO);
			}
		} else {
			this.base.setAnonymous(YesNoType.YES);
		}

		return this.base;
	}

}
