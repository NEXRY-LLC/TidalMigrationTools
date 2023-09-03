package com.bluehouseinc.dataconverter.parsers.bmc.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvFtpJob;
import com.bluehouseinc.dataconverter.model.impl.CsvFtpJob.FTPOperation;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCFileTransferJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.ftp.FtpCommand;
import com.bluehouseinc.dataconverter.parsers.bmc.model.xml.AccountParser;
import com.bluehouseinc.dataconverter.parsers.bmc.model.xml.Host;
import com.bluehouseinc.dataconverter.parsers.bmc.model.xml.NodeKey;
import com.bluehouseinc.io.utils.FileMapUtil;
import com.bluehouseinc.io.utils.FileMapUtil.FileParts;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FTPUtil {

	AccountParser parser;

	public FTPUtil(AccountParser parser) {
		this.parser = parser;
	}

	public List<CsvFtpJob> setupFTPJob(BMCFileTransferJob src, TidalDataModel dm) {
		List<CsvFtpJob> jobs = new ArrayList<>();

		if (src.getName().equals("FTP_PUT_ALLIANZ_NNOTES_FG")) {
			src.getName();
		}

		final AtomicInteger count = new AtomicInteger(0);
		CsvFtpJob priorjob = null;

		int cmdtotal = src.getFtpObject().getFtpCommands().size();

		for (FtpCommand command : src.getFtpObject().getFtpCommands()) {
			int cnt = count.incrementAndGet();
			CsvFtpJob dest = new CsvFtpJob();

			if (cmdtotal > 1) {
				dest.setName(src.getName() + "-XXXXStep-" + cnt);
			} else {
				dest.setName(src.getName());
			}

			setupFTPJobDetails(dest, command, src, dm);
			// setFTPOperationPath(dest, command, src);
			// setFTPProtocol(dest, command, src);

			if (StringUtils.isBlank(dest.getFileName())) {
				dest.setFileName("-----ERROR----");
			}

			if (cnt == 1) {
				priorjob = dest; // First one so just set to prior
			} else {
				dm.addJobDependencyForJobCompletedNormal(dest, priorjob, null);
				priorjob = dest; // Set after we register.
			}

			jobs.add(dest);
		}

		return jobs;
	}

	private static void setFTPProtocol(CsvFtpJob dest, FtpCommand command, BMCFileTransferJob src) {
		String contype = src.getFtpObject().getRemoteConnectType() == null ? "ftp" : src.getFtpObject().getRemoteConnectType().toString();

		if (contype.equals("FTP-SSL") || contype.equals("FTPSSL")) {
			dest.setFtpProtocol(CsvFtpJob.FtpProtocol.sftp);
			// dest.setEncryptioncypherselection("3DES,Blowfish,DES,AES,AES192-CBC,AES128-CBC,AES256-CBC,AES192-CTR,AES128-CTR,AES256-CTR");
			dest.setUserPasswordAuthetication(true);
		} else {
			contype = contype.toLowerCase();
			CsvFtpJob.FtpProtocol type = CsvFtpJob.FtpProtocol.valueOf(contype);

			if (type == CsvFtpJob.FtpProtocol.local) {
				// We have an issue with this job
				// TODO: Figure this out.
				type = CsvFtpJob.FtpProtocol.ftp;
			}

			dest.setFtpProtocol(type);
			dest.setUserPasswordAuthetication(true);
		}
	}

	private static void setFTPOperationPath(CsvFtpJob dest, FtpCommand command, BMCFileTransferJob src) {

		// File names are a challenge in BMC as they do something with get vs mget , put
		// vs mput. Meaning they allow wild cards

		String localpath = command.getLocalPath().replace("\\", "/");
		String remotepath = command.getRemotePath().replace("\\", "/");

		FileParts localparts = FileMapUtil.splitFileNameIntoParts(localpath);
		FileParts reportparts = FileMapUtil.splitFileNameIntoParts(remotepath);

		if (command.getUpload().equals("0") || command.getUpload().equals("3")) {
			if (command.isMultiple()) {
				dest.setFtpOperation(CsvFtpJob.FTPOperation.MGET);
			} else {
				dest.setFtpOperation(CsvFtpJob.FTPOperation.GET);
			}
		} else {
			if (command.isMultiple()) {
				dest.setFtpOperation(CsvFtpJob.FTPOperation.MPUT);
			} else {
				dest.setFtpOperation(CsvFtpJob.FTPOperation.PUT);
			}
		}

		// if (command.getUpload().equals("0") || command.getUpload().equals("3")) {
		// dest.setFtpOperation(CsvFtpJob.FTPOperation.GET);
		// if (remotepath.contains("*")) {
		// dest.setFtpOperation(CsvFtpJob.FTPOperation.MGET);
		// }
		// if (reportparts.getName().equals("*") &&
		// reportparts.getExtention().equals("*")) {
		// dest.setFileName("*.*");
		//
		// } else {
		// dest.setFileName(reportparts.getName() + reportparts.getSeparator() +
		// reportparts.getExtention());
		// }
		// } else {
		// dest.setFtpOperation(CsvFtpJob.FTPOperation.PUT);
		// if (remotepath.contains("*")) {
		// dest.setFtpOperation(CsvFtpJob.FTPOperation.MPUT);
		// }
		// if (localparts.getName().equals("*") &&
		// localparts.getExtention().equals("*")) {
		// dest.setFileName("*.*");
		// } else {
		// dest.setFileName(localparts.getName() + localparts.getSeparator() +
		// localparts.getExtention());
		// }
		// }

		if (reportparts.getName().equals("*") && reportparts.getExtention().equals("*")) {
			dest.setFileName("*.*");

		} else {
			dest.setFileName(reportparts.getName() + reportparts.getSeparator() + reportparts.getExtention());
		}

		// No real way to deal with variables so if we have or end in a var them just
		// use raw values.
		if (localpath.endsWith(">")) {
			dest.setLocalPath(localpath);
		} else {
			dest.setLocalPath(localparts.getPath());
		}

		if (remotepath.endsWith(">")) {
			dest.setRemotePath(remotepath);
		} else {
			dest.setRemotePath(reportparts.getPath());
		}

		// dest.setLocalPath(BMCDataUtil.replacSysDateVariables(dest.getLocalPath(),
		// src.getSetVarData(), ""));
		// dest.setRemotePath(BMCDataUtil.replacSysDateVariables(dest.getRemotePath(),
		// src.getSetVarData(), ""));
		// dest.setFileName(BMCDataUtil.replacSysDateVariables(dest.getFileName(),
		// src.getSetVarData(), ""));

	}

	public void setupJob(CsvFtpJob dest, FtpCommand command, BMCFileTransferJob src) {

		// We have a real hard time figuring out how to know between get and put types..

		// GET
		// <VARIABLE NAME="%%FTP-LPASSIVE" VALUE="2" />
		// <VARIABLE NAME="%%FTP-RPASSIVE" VALUE="2" />
		// <VARIABLE NAME="%%FTP-UPLOAD1" VALUE="3" />

		// PUT
		// <VARIABLE NAME="%%FTP-LPASSIVE" VALUE="2" />
		// <VARIABLE NAME="%%FTP-RPASSIVE" VALUE="0" />
		// <VARIABLE NAME="%%FTP-UPLOAD1" VALUE="2" />

		// MGET
		// <VARIABLE NAME="%%FTP-LPASSIVE" VALUE="2" />
		// <VARIABLE NAME="%%FTP-RPASSIVE" VALUE="0" />
		// <VARIABLE NAME="%%FTP-UPLOAD1" VALUE="3" />

		// MPUT
		// <VARIABLE NAME="%%FTP-LPASSIVE" VALUE="2" />
		// <VARIABLE NAME="%%FTP-RPASSIVE" VALUE="0" />
		// <VARIABLE NAME="%%FTP-UPLOAD1" VALUE="2" />

		// Have to assume that the upload type = X determines put or get while <VARIABLE
		// NAME="%%FTP-TRANSFER_ALL1" VALUE="1" /> is the M type (MPUT / MGET)
		// Soooooo.

		int up = Integer.valueOf(command.getUpload());
		CsvFtpJob.FTPOperation operation = null;

		command.setTransferAll("true"); // for all M versions as they work for standard too.

		if (up == 3) {
			if (command.isMultiple()) {
				operation = CsvFtpJob.FTPOperation.MGET;
			} else {
				operation = CsvFtpJob.FTPOperation.GET;
			}
		} else if (up == 2) {
			if (command.isMultiple()) {
				operation = CsvFtpJob.FTPOperation.MPUT;
			} else {
				operation = CsvFtpJob.FTPOperation.PUT;
			}
		} else {
			throw new RuntimeException("Unknown FTP Upload type[" + up + "]");
		}

		dest.setFtpOperation(operation); // Set our operation type... I know we will hit many more types to code for.

		// Not sure if R and L mean local and remote or left and right.. but either way
		// there we have FTP, SFTP, and LOCAL.. LOCAL is local file system. and for
		// Genex.. seems all the right or remote is local.
		// So my issue is if R is local always then this is odd. so I guess we pick
		// which side is local or do we expect that we can one both sides be remote?

		// for now we are going to code for the right side to be local per genex and
		// fail if we encounter anything else.

		String rightsideconnectiontype = src.getFtpObject().getRemoteConnectType() == null ? "ftp" : src.getFtpObject().getRemoteConnectType().toString();
		String leftsideconnectiontype = src.getFtpObject().getLocalConnectType() == null ? "ftp" : src.getFtpObject().getLocalConnectType().toString();

		String localpath = null;
		String remotepath = null;

		String filename = null;

		// one of the two must be local.

		if (!"local".equalsIgnoreCase(rightsideconnectiontype) && !"local".equalsIgnoreCase(leftsideconnectiontype)) {
			// Must have one that is local.
			throw new RuntimeException("Connection type of one must be local.");
		} else {

			// so we make it here , one is local..
			String csvcontype = "ftp";
			CsvFtpJob.FtpProtocol type = CsvFtpJob.FtpProtocol.ftp;

			if ("local".equalsIgnoreCase(rightsideconnectiontype)) {
				// We are not the local data.
				csvcontype = rightsideconnectiontype.toLowerCase();
				remotepath = command.getRemotePath().replace("\\", "/"); // Right side
				localpath = command.getLocalPath().replace("\\", "/"); // Left side

			} else {
				// Must be the left side then.
				csvcontype = leftsideconnectiontype.toLowerCase();
				remotepath = command.getLocalPath().replace("\\", "/"); // left side
				localpath = command.getRemotePath().replace("\\", "/"); // Right side

			}

			if (csvcontype.equals("ftp-ssl") || csvcontype.equals("ftpssl")) {
				type = CsvFtpJob.FtpProtocol.sftp;
			} else {
				type = CsvFtpJob.FtpProtocol.valueOf(csvcontype);
			}

			dest.setFtpProtocol(type);
			dest.setUserPasswordAuthetication(true);

			// Now to deal with the path information.. In short we can have actual file
			// paths or we can have variables.

			FileParts remoteparts = FileMapUtil.splitFileNameIntoParts(remotepath);
			FileParts localparts = FileMapUtil.splitFileNameIntoParts(localpath);

			if (remotepath.contains("/")) {
				filename = remoteparts.getName() + remoteparts.getSeparator() + remoteparts.getExtention();

				if (remoteparts.getName().equals("*") && remoteparts.getExtention().equals("*")) {
					filename = "*.*";
				}
			}

			if (localpath.contains("/")) {
				filename = localparts.getName() + localparts.getSeparator() + localparts.getExtention();

				if (remoteparts.getName().equals("*") && remoteparts.getExtention().equals("*")) {
					filename = "*.*";
				}
			}

		}

	}

	/*
	 * This is the directional stuff , so my best guess is Left and Right, not local
	 * / remote that I have previously coded names for. <VARIABLE
	 * NAME="%%FTP-UPLOAD1" VALUE="0" /> 0 = Right to Left 2 = Right to Left – File
	 * stability check 1 = Left to Right 3 = Left to Right – File Stablity Check 4 =
	 * Watch only
	 */
	public enum UploadDirection {

		RTL(0), LTR(1), RTLS(2), LTRS(3), WO(4);

		public final int typeid;

		private UploadDirection(int typeid) {
			this.typeid = typeid;
		}

		public static UploadDirection valueOfInt(int typeid) {
			return EnumSet.allOf(UploadDirection.class).stream().filter(e -> e.typeid == typeid).findFirst()
					.orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", typeid)));
			// return Arrays.stream(values()).filter(e -> e.typeid ==
			// typeid).findFirst().orElseThrow(() -> new
			// IllegalStateException(String.format("Unsupported type %s.", typeid)));
		}
	}

	private void setupFTPJobDetails(CsvFtpJob dest, FtpCommand command, BMCFileTransferJob src, TidalDataModel dm) {

		UploadDirection direction = UploadDirection.valueOfInt(Integer.valueOf(command.getUpload().trim()));

		String rstype = src.getFtpObject().getRemoteConnectType() == null ? "ftp" : src.getFtpObject().getRemoteConnectType().toString();
		String lstype = src.getFtpObject().getLocalConnectType() == null ? "ftp" : src.getFtpObject().getLocalConnectType().toString();

		CsvFtpJob.FTPOperation oper = null; // We dont know yet.
		CsvFtpJob.FtpProtocol protocol = null; // We dont know just yet.BlueH0use

		String host = null;
		String remoteuser = null;

		dest.setAnonymous(false);

		if (command.getType().equals("A")) {
			dest.setAsciiformat(true);
		} else {
			dest.setAsciiformat(false);
		}

		/*
		 * So upload = the direction left to right or right to left of if L is local and
		 * R is FTP, then the relative get vs put is based on which is local. We can't
		 * support FTP to FTP so we need to error our fast on that cond for now.
		 */

		if (!"local".equalsIgnoreCase(rstype) && !"local".equalsIgnoreCase(lstype)) {
			// Must have one that is local.
			// throw new RuntimeException("Connection type of one must be local
			// for["+src.getFullPath()+"].");

			// Just fake it.. If we are Right to Left then assume right is local, or the
			// direction is local
			if (direction == UploadDirection.LTR || direction == UploadDirection.LTRS) {
				lstype = "local"; // Left to right so left if local
			} else {
				rstype = "local"; // Right to left so right is local.
			}

		}

		String csvcontype = null;

		// If my right side is local then we must assume our left side is our FTP side
		if ("local".equalsIgnoreCase(rstype)) {
			csvcontype = lstype.toLowerCase(); // Left is FTP type, not right side.
			// RIght is not local, so assume my right side is my FTP type (for now)
		} else {
			csvcontype = rstype.toLowerCase(); // Right side if the FTP type side.
		}

		// Only one supporting key / pair for passsword
		String nodeid = src.getNodeName().toLowerCase();
		String account = src.getFtpObject().getAccount();
		NodeKey acctkey = new NodeKey(nodeid, account);
		this.parser.loadAccount(acctkey); // Load our Account Data

		// We have a couple FTPS formats in terms of the values between versions of BMC
		// so lets just catch them all.
		if (csvcontype.equals("ftp-ssl") || csvcontype.equals("ftpssl") || csvcontype.equals("sftp")) {
			protocol = CsvFtpJob.FtpProtocol.sftp;

			if (acctkey.getAccount() == null) {
				log.error("Unable to locate account data for nodeid[" + nodeid + "] and account name[" + account + "]");
			} else {
				// 100% hack.. We know in TIDAL there is only one setting so just find it, its
				// either of the host entries.
				if (acctkey.getAccount().getLeftHost().getLogicalKeyName() != null) {
					doSetKeyFileData(acctkey.getAccount().getLeftHost(), dest);
				} else if (acctkey.getAccount().getRightHost().getLogicalKeyName() != null) {
					doSetKeyFileData(acctkey.getAccount().getRightHost(), dest);
				}

				// We need to set up the port for all FTP jobs too as this is missing from our
				// data.

			}
		} else {
			protocol = CsvFtpJob.FtpProtocol.valueOf(csvcontype);
			dest.setUserPasswordAuthetication(true); // Need to determine if this is always needed
		}

		dest.setFtpProtocol(protocol); // Set our protocol we are using.

		String leftpath = StringUtils.getOr(command.getLocalPath(), "").replace("\\", "/");
		String rightpath = StringUtils.getOr(command.getRemotePath(), "").replace("\\", "/");
		FileParts leftparts = FileMapUtil.splitFileNameIntoParts(leftpath);
		FileParts rightparts = FileMapUtil.splitFileNameIntoParts(rightpath);

		switch (direction) {
		case LTR:
		case LTRS:
			if (lstype.equalsIgnoreCase("local")) {
				// Left is ftp or one of the FTP types
				oper = CsvFtpJob.FTPOperation.PUT; // RIGHT is local and left is FTP so we are putting
				host = src.getFtpObject().getRemoteHost(); // Right side is our remote host
				remoteuser = src.getFtpObject().getRemoteUser(); // Right side is our remote user

				dest.setRemotePath(leftpath);

				String localdir = rightparts.getPath();
				dest.setLocalPath(localdir);

				String filename = rightparts.getName() + rightparts.getSeparator() + rightparts.getExtention();
				dest.setFileName(filename);

			} else {
				oper = CsvFtpJob.FTPOperation.GET; // RIGHT is FTP and left is our local, so we are getting

				host = src.getFtpObject().getLocalHost(); // Left side is our remote host
				remoteuser = src.getFtpObject().getLocalUser(); // Left side is our remote user

				dest.setLocalPath(rightpath);

				String remotedir = leftparts.getPath();
				dest.setRemotePath(remotedir);

				String filename = leftparts.getName() + leftparts.getSeparator() + leftparts.getExtention();
				dest.setFileName(filename);

			}
			break;
		case RTL:
		case RTLS:
			if (rstype.equalsIgnoreCase("local")) {
				// Left is ftp or one of the FTP types
				oper = CsvFtpJob.FTPOperation.PUT; // RIGHT is local and left is FTP so we are putting

				host = src.getFtpObject().getLocalHost(); // Left side is our remote host
				remoteuser = src.getFtpObject().getLocalUser(); // Left side is our remote user

				dest.setRemotePath(leftpath);

				String localdir = rightparts.getPath();
				dest.setLocalPath(localdir);

				String filename = rightparts.getName() + rightparts.getSeparator() + rightparts.getExtention();
				dest.setFileName(filename);

			} else {
				oper = CsvFtpJob.FTPOperation.GET; // RIGHT is FTP and left is our local, so we are getting

				host = src.getFtpObject().getRemoteHost(); // Right side is our remote host
				remoteuser = src.getFtpObject().getRemoteUser(); // Right side is our remote user

				dest.setRemotePath(rightpath);

				String localdir = leftparts.getPath();
				dest.setLocalPath(localdir);

				String filename = leftparts.getName() + leftparts.getSeparator() + leftparts.getExtention();
				dest.setFileName(filename);

			}
			break;
		default:
			//throw new RuntimeException("Unsupported Direction Detected");
			log.info("Unsupported Direction Detected");

		}

		// So now we know our operation type but is it Mget or Mput?
		// FIXME: The interally looped variables prevent from checking for wild cards so
		// we are going to set
		// all jobs to M versions as that will also work for files names without
		// wildcards
		if (oper == FTPOperation.GET) {
			// if (command.isMultiple() || dest.getFileName().contains("*")) {
			oper = FTPOperation.MGET;
			// }
		}

		if (oper == FTPOperation.PUT) {
			// if (command.isMultiple() || dest.getFileName().contains("*")) {
			oper = FTPOperation.MPUT;
			// }
		}

		if (StringUtils.isBlank(host)) {
			host = "-----ERROR-HOST-MISSING---";
		}

		dest.setFtphost(host);
		dest.setFtpOperation(oper); // Set our operation type... I know we will hit many more types to code for

		if (acctkey.getAccount() == null) {
			log.error("Unable to locate account data for nodeid[" + nodeid + "] and account name[" + account + "]");
		} else {
			if(acctkey.getAccount().getLeftHost().getPort()!=0 && acctkey.getAccount().getLeftHost().getPort()!=21) {
				doAppendPort(acctkey.getAccount().getLeftHost(), dest);
			}else if(acctkey.getAccount().getRightHost().getPort()!=0 && acctkey.getAccount().getRightHost().getPort()!=21) {
				doAppendPort(acctkey.getAccount().getLeftHost(), dest);
			}
		}

		if (StringUtils.isBlank(remoteuser)) {
			remoteuser = src.getFtpObject().getAccount();
		}

		CsvRuntimeUser rtu = new CsvRuntimeUser(remoteuser);
		rtu.setPasswordForFtpOrAS400("123");
		dm.addRunTimeUserToJobOrGroup(dest, rtu);
		dest.setRemoteuser(rtu);

	}

	private void doSetKeyFileData(Host host, CsvFtpJob dest) {

		if (host == null) {
			return;
		}

		String keydir = host.getLogicalKeyName();
		String keypwd = host.getPassphrase();

		if (keydir != null) {
			dest.setPrivateKeyFileLocation(keydir);
			dest.setPrivateKeyFilePassPhrase(keypwd);
			dest.setUserPasswordAuthetication(false);
		} else {
			dest.setUserPasswordAuthetication(true); // Need to determine if this is always needed
		}
	}

	private void doAppendPort(Host host, CsvFtpJob dest) {
		String hostname = dest.getFtphost();

		if (host == null) {
			return;
		}

		int port = host.getPort();

		// append port to the host name
		hostname = hostname + ":" + port;

		dest.setFtphost(hostname);
	}
}
