package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.ftp;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;

/**
 * BMC supports two connections and up to 5 sources and destinations per job.
 *
 * @author Brian Hayes
 *
 */
@Data
public class FtpObject {

	// private String[] elements = { "ACCOUNT", "LOSTYPE", "LUSER", "ROSTYPE",
	// "RUSER", "PATH", "RPF", "LHOST", "RHOST", "LPASSIVE", "RPASSIVE", "Is",
	// "AUTOREFRESH", "CONNTYPE1", "CONNTYPE2",
	// "TRANSFER_NUM", "USE_DEF_NUMRETRIES", "NUM_RETRIES","CM_VER","CONT_EXE_NOTOK"
	// };

	// Matcher matcher = pattern.matcher("");

	private String account;
	private String localUser;
	private String remoteUser;
	private FtpOsTypes localOSType;
	private FtpOsTypes remoteOSType;
	private String path;
	private String fpf;
	private String localHost;
	private String remoteHost;
	private String localPassive;
	private String remotePassive;
	private String is;
	private String autoRefreash;
	private List<SetVarData> data;
	private FtpConnectTypes localConnectType; // Only ever two for FTP-CONNTYPE1 and FTP-CONNTYPE2
	private FtpConnectTypes remoteConnectType; // Only ever two for FTP-CONNTYPE1 and FTP-CONNTYPE2
	private int transferNum; // This tells us how many we have
	private String useDefRetry;
	private String numRetrys;
	private String cmVer;
	private String continueExeNotOk;

	private List<FtpCommand> ftpCommands = new ArrayList<>();

	public FtpObject(List<SetVarData> data) {
		this.data = data;
	}

	public void init() {
		List<SetVarData> newlist = new ArrayList<>();

		for (SetVarData d : this.data) {
			String name = d.getNAME();

			if (name.equals("%%FTP-ACCOUNT")) {
				this.account = d.getVALUE();
			} else if (name.equals("%%FTP-LOSTYPE")) {
				this.localOSType = FtpOsTypes.valueOf(d.getVALUE());
			} else if (name.equals("%%FTP-LUSER")) {
				this.localUser = d.getVALUE();
			} else if (name.equals("%%FTP-ROSTYPE")) {
				this.remoteOSType = FtpOsTypes.valueOf(d.getVALUE());
			} else if (name.equals("%%FTP-RUSER")) {
				this.remoteUser = d.getVALUE();
			} else if (name.equals("%%FTP-PATH")) {
				this.path = d.getVALUE();
			} else if (name.equals("%%FTP-RPF")) {
				this.fpf = d.getVALUE();
			} else if (name.equals("%%FTP-LHOST")) {
				this.localHost = d.getVALUE();
			} else if (name.equals("%%FTP-RHOST")) {
				this.remoteHost = d.getVALUE();
			} else if (name.equals("%%FTP-LPASSIVE")) {
				this.localPassive = d.getVALUE();
			} else if (name.equals("%%FTP-RPASSIVE")) {
				this.remotePassive = d.getVALUE();
			} else if (name.equals("%%FTP-Is")) {
				this.is = d.getVALUE();
			} else if (name.equals("%%FTP-AUTOREFRESH")) {
				this.autoRefreash = d.getVALUE();
			} else if (name.equals("%%FTP-CONNTYPE1")) {
				String type = d.getVALUE().replaceAll("-", "").trim();
				this.localConnectType = FtpConnectTypes.valueOf(type); // to handle FTP-SSL as a type
				// System.out.println(localConnectType);
			} else if (name.equals("%%FTP-CONNTYPE2")) {
				String type = d.getVALUE().replaceAll("-", "").trim();
				this.remoteConnectType = FtpConnectTypes.valueOf(type); // to handle FTP-SSL as a type
			} else if (name.equals("%%FTP-TRANSFER_NUM")) {
				this.transferNum = Integer.valueOf(d.getVALUE());
			} else if (name.equals("%%FTP-USE_DEF_NUMRETRIES")) {
				this.useDefRetry = d.getVALUE();
			} else if (name.equals("%%FTP-NUM_RETRIES")) {
				this.numRetrys = d.getVALUE();
			} else if (name.equals("%%FTP-CM_VER")) {
				this.cmVer = d.getVALUE();
			} else if (name.equals("%%FTP-CONT_EXE_NOTOK")) {
				this.continueExeNotOk = d.getVALUE();
			} else {
				newlist.add(d); // Not used here so send to our FtpCommands
			}

		}

		// do our 5
		for (int i = 1; i <= this.transferNum; i++) {
			FtpCommand cmd = new FtpCommand(i, newlist);
			if (cmd.hasData()) {
				ftpCommands.add(cmd);
				// System.out.println(cmd);
			}
		}

	}

	public List<FtpCommand> getFtpCommands() {
		if (this.ftpCommands.isEmpty()) {
			init();
		}

		return ftpCommands;
	}

	public void setFtpCommand(FtpCommand cmd) {
		List<FtpCommand> list = new ArrayList<>();
		list.add(cmd);
		this.ftpCommands = list;
	}

}
