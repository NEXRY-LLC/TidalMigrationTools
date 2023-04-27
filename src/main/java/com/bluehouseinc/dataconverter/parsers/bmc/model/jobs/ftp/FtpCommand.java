package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.ftp;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;

/**
 * Each FTPObject in BMC can have up to 5 commands per job.
 *
 * @author Brian Hayes
 *
 */
@Data
public class FtpCommand {

	private int index;
	private List<SetVarData> data;

	private String localPath;
	private String remotePath;
	private String upload; // 0 = get, 1 = put. 3 = get , 2 = put
	private String type; // A OR I ?? WHAT IS THIS
	private String minSize;
	private String timeLimit;
	private String srcOpt; // ??
	private String dstOpt;
	private String absTime;
	private String trim;
	private String unique;
	private String recursive;
	private String nullflds;
	private String verNum;
	private String caseifs;
	private String newName;
	private String excludeWildCard;
	private String ifExists;
	private String transferAll; // if we have this then we are a M <get or put>
	private String filePfx;
	private String delDestOnFailure;
	private String pgpEnable;
	private String pgpTemplateName;
	private String pgpKeepFiles;
	private String destNewName;
	private String overrrideWatchInterval;
	private String watchInterval;
	private String timelimitUntil;

	public FtpCommand(int index, List<SetVarData> data) {
		this.index = index; // 1 to 5
		this.data = data;
		this.init();
	}

	public boolean hasData() {
		return localPath != null; // simple test as it must exists for each on if there is more than one
	}

	/**
	 * Is this a Mput or Mget? Not done with the code as we need to handle list, del, etc..
	 *
	 * @return
	 */
	public boolean isMultiple() {
		if((this.transferAll != null) || this.getLocalPath().contains("*") || this.getRemotePath().contains("*")) {
			return true;
		}

		return false;
	}

	private void init() {

		for (SetVarData d : this.data) {
			String name = d.getNAME();

			// String findname = "%%FTP-"+elements[0]+this.index; // Up to 5 of these
			// potentially
			// System.out.println("%%FTP-" + elements[0] + this.index);
			if (name.equals("%%FTP-LPATH" + this.index)) {
				this.localPath = d.getVALUE();
			} else if (name.equals("%%FTP-RPATH" + this.index)) {
				this.remotePath = d.getVALUE();
			} else if (name.equals("%%FTP-UPLOAD" + this.index)) {
				this.upload = d.getVALUE();
			} else if (name.equals("%%FTP-TYPE" + this.index)) {
				this.type = d.getVALUE();
			} else if (name.equals("%%FTP-MINSIZE" + this.index)) {
				this.minSize = d.getVALUE();
			} else if (name.equals("%%FTP-TIMELIMIT" + this.index)) {
				this.timeLimit = d.getVALUE();
			} else if (name.equals("%%FTP-SRCOPT" + this.index)) {
				this.srcOpt = d.getVALUE();
			} else if (name.equals("%%FTP-DSTOPT" + this.index)) {
				this.dstOpt = d.getVALUE();
			} else if (name.equals("%%FTP-ABSTIME" + this.index)) {
				this.absTime = d.getVALUE();
			} else if (name.equals("%%FTP-TRIM" + this.index)) {
				this.trim = d.getVALUE();
			} else if (name.equals("%%FTP-UNIQUE" + this.index)) {
				this.unique = d.getVALUE();
			} else if (name.equals("%%FTP-RECURSIVE" + this.index)) {
				this.recursive = d.getVALUE();
			} else if (name.equals("%%FTP-NULLFLDS" + this.index)) {
				this.nullflds = d.getVALUE();
			} else if (name.equals("%%FTP-VERNUM" + this.index)) {
				this.verNum = d.getVALUE();
			} else if (name.equals("%%FTP-CASEIFS" + this.index)) {
				this.caseifs = d.getVALUE();
			} else if (name.equals("%%FTP-NEWNAME" + this.index)) {
				this.newName = d.getVALUE();
			} else if (name.equals("%%FTP-EXCLUDE_WILDCARD" + this.index)) {
				this.excludeWildCard = d.getVALUE();
			} else if (name.equals("%%FTP-IF_EXIST" + this.index)) {
				this.ifExists = d.getVALUE();
			} else if (name.equals("%%FTP-TRANSFER_ALL" + this.index)) {
				this.transferAll = d.getVALUE();
			} else if (name.equals("%%FTP-FILE_PFX" + this.index)) {
				this.filePfx = d.getVALUE();
			} else if (name.equals("%%FTP-DEL_DEST_ON_FAILURE" + this.index)) {
				this.delDestOnFailure = d.getVALUE();
			} else if (name.equals("%%FTP-PGP_ENABLE" + this.index)) {
				this.pgpEnable = d.getVALUE();
			} else if (name.equals("%%FTP-PGP_TEMPLATE_NAME" + this.index)) {
				this.pgpTemplateName = d.getVALUE();
			} else if (name.equals("%%FTP-PGP_KEEP_FILES" + this.index)) {
				this.pgpKeepFiles = d.getVALUE();
			} else if (name.equals("%%FTP-DEST_NEWNAME" + this.index)) {
				this.destNewName = d.getVALUE();
			} else if (name.equals("%%FTP-OVERRIDE_WATCH_INTERVAL" + this.index)) {
				this.overrrideWatchInterval = d.getVALUE();
			} else if (name.equals("%%FTP-WATCH_INTERVAL" + this.index)) {
				this.watchInterval = d.getVALUE();
			} else if (name.equals("%%FTP-TIMELIMIT_UNIT" + this.index)) {
				this.timelimitUntil = d.getVALUE();
			} else {
				// System.out.println("Unknown " + name);
			}

		}

	}

}
