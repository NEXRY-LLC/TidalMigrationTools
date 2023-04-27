package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;
import lombok.EqualsAndHashCode;

/*
 *
 */
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class BMCOS400Job extends BaseBMCJobOrFolder {

	List<SetVarData> unusedVarData = new ArrayList<>();

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.OS400;
	}

	/*
	 * <VARIABLE NAME="%%OS400-JOB_NAME" VALUE="PRODUSRCN" />
	 */
	private String jobName;

	/*
	 * <VARIABLE NAME="%%OS400-JOB_OWNER" VALUE="PRODUSRCN" /> <VARIABLE
	 * NAME="%%OS400-JOB_AUTHOR" VALUE="CSECOFR" />
	 */

	public String jobOwner;

	public String jobAuthor;

	/*
	 * <VARIABLE NAME="%%ALERT_DEVICE_NAME" VALUE="*RBTDFT" />
	 */

	public String alertDeviceName;

	/*
	 * <VARIABLE NAME="%%ALERT_DEVICE_NAME" VALUE="*RBTDFT" /> <VARIABLE
	 * NAME="%%LIBMEMSYM" VALUE="/tmp/edi/LIBMEMSYM/ENV_STANDARD" />
	 */
	public String systemLibraryList;

	/*
	 * <VARIABLE NAME="%%OS400-CMDLINE1"
	 * VALUE="SBMJOB CMD(CALL PGM(MB00097CL)) JOB(MB00097CL) JOBQ(QCTL)" />
	 * <VARIABLE NAME="%%OS400-CMDLINE2" VALUE="DLYJOB DLY(300)" /> <VARIABLE
	 * NAME="%%OS400-CMDLINE3" VALUE="ENDSBS SBS(QZRDSSRV) OPTION(*IMMED)" />
	 */
	private List<OSCommand> commands = new ArrayList<>();
	private List<String> params = new ArrayList<>();
	private List<OSCommand> postCommands = new ArrayList<>();
	private List<String> ldaData = new ArrayList<>();

	public String memoryName;
	public String memoryLibrary;

	// <VARIABLE NAME="%%OS400-JOBD" VALUE="QGPL/QBATCH" />
	public String jobDescription;
	public String jobDescriptionLibrary;

	// <VARIABLE NAME="%%OS400-OUTQ" VALUE="QGPL/QPRINT" />

	public String outQueueName;
	public String outQueueLibrary;

	// <VARIABLE NAME="%%OS400-JOBQ" VALUE="*LIBL/QTXTSRCH" />
	public String jobQueue;
	public String jobQueueLibrary;

	// <VARIABLE NAME="%%OS400-INQMSGRPY" VALUE="*RQD" />
	public String inqueryMessageReply;

	private ObjectTypes objectType;

	// <VARIABLE NAME="%%OS400-AEV_LEN" VALUE="4000" />
	public String aevLenght;

	// SCRIPT_FILE_IGNERR
	public String scriptFileIgnoreError;

	// SetVarData(name=%%OS400-DATE, value=*JOBD)
	public String date;

	// SetVarData(name=%%OS400-MSGQ, value=*LIBL/SRHODY)
	public String messageQueue;
	public String messageQueueLibrary;

	// SetVarData(name=%%OS400-LOG, value=4 00 *SECLVL)
	public String messageLogLevel;
	public String messageLogSeverity;
	public String messageLogText;

	// SetVarData(name=%%OS400-SCRIPT_FILE_LOGINFMSG, value=N)
	public String scriptFileLogInformationMessage;

	// SetVarData(name=%%OS400-SKIP_VALIDITY, value=Y)
	public String skipValidity;

	// SetVarData(name=%%OS400-JOBPTY, value=3)
	public String jobPriorityOnQueue;

	// SetVarData(name=%%OS400-CURLIB, value=ROBOTLIB)
	public String currentLibrary;

	// SetVarData(name=%%OS400-RUNPTY, value=10)
	public String runPriority;

	// SetVarData(name=%%OS400-HOLD, value=*NO)
	public String holdOnJobQueue;

	// SetVarData(name=%%OS400-ASPGRP, value=*JOBD)
	public String initalASPGroup;

	// SetVarData(name=%%OS400-LOGCLPGM, value=*YES)
	public String logCLProgramCommands;

	// SetVarData(name=%%SYSAUDDATE, value=%%MONTH.%%MYDAY.%%YEAR)
	public String systemAudDate;
	// SetVarData(name=%%MYDAY, value=%%DAY %%MINUS 1)
	public String mydayVar;

	// %%OS400-PRTTXT
	public String printText;

	// %%OS400-INLLIBL1 => CCUSROBJ1

	public String initidalLibrary;

	// %%OS400-JOBMSGQF
	public String fullAction;

	// %%OS400-JOBMSGQMX => *JOBD
	public String jobMessageQueueMaxSize;

	/**
	 * Works for standard command and PGM calls as they are all single commands. The
	 * Multi Command is still open in terms of how to run in TIDAL
	 *
	 * @return
	 */
	// public String getPge1Command() {
	// String command = "";
	// // CALL PGM(PLG0810) PARM(GEOIMPORT 'C' ' ')
	// /*
	// * String joinedFirstNames =
	// * list.stream().map(Person::getFirstName).collect(Collectors.joining(", "));
	// */
	// if (this.objectType == ObjectTypes.CMDLINE) {
	// command = String.join(" ", this.commands); // Should only ever be one for CMDLINE
	// if (!this.params.isEmpty()) {
	// String data = String.join(" ", this.params);
	// command = command + " PARM(" + data + ")";
	// }
	// } else if (this.objectType == ObjectTypes.PGM) { // MEM_NAME is the call.
	// // command = String.join(" ", this.commands); // Should only ever be one for
	// // CMDLINE
	// command = "CALL PGM(" + this.memoryName + ")";// This is what PGM needs to call.
	// if (!this.params.isEmpty()) {
	// String data = String.join(" ", this.params);
	// command = command + " PARM(" + data + ")";
	// }
	// } else if (this.objectType == ObjectTypes.MULTICMD) {
	// // No idea if this will work but likly no.. and need to make one job into multiple jobs. Will test.
	// command = String.join(" ", this.commands);//
	// if (!this.params.isEmpty()) {
	// String data = String.join(" ", this.params);
	// command = command + " PARM(" + data + ")";
	// }
	// }
	//
	// return command;
	// }

	/**
	 * Any Var data not used will get put into this String.
	 */
	@Override
	public String getPlaceHolderData() {
		StringBuilder b = new StringBuilder();

		this.unusedVarData.stream().forEach(data -> {

			b.append(data.getNAME());
			b.append("=");
			b.append(data.getVALUE());
			b.append("\n");
		});

		return b.toString();
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData d) {

		if (this.getName().equals("QAUDITSAV")) {
			// System.out.println();
		}

		if (d.getNAME().equals("%%OS400-JOB_OWNER")) {
			this.jobOwner = d.getVALUE();
		} else if (d.getNAME().equals("%%OS400-JOB_AUTHOR")) {
			this.jobAuthor = d.getVALUE();
		} else if (d.getNAME().equals("%%ALERT_DEVICE_NAME")) {
			this.alertDeviceName = d.getVALUE();
		} else if (d.getNAME().equals("%%LIBMEMSYM")) {
			// <VARIABLE NAME="%%LIBMEMSYM" VALUE="\\Bmc-prod-cm01\libmemsym" />
			// Eat for now, not sure what this is for in TIDAL
			// this.systemLibraryList = d.getVALUE();
		} else if (d.getNAME().equals("%%OS400-MEM_NAME")) {
			this.memoryName = d.getVALUE();
		} else if (d.getNAME().equals("%%OS400-MEM_LIB")) {
			this.memoryLibrary = d.getVALUE();
		} else if (d.getNAME().equals("%%OS400-JOB_NAME")) {
			this.jobName = d.getVALUE();
		} else if (d.getNAME().equals("%%OS400-OBJTYP")) {
			// Just to cleanup they all start with * and need to map to our types to force
			// failure on new types
			String data = d.getVALUE().replace("*", "");
			this.objectType = ObjectTypes.valueOf(data);
		} else if (d.getNAME().equals("%%OS400-JOBD")) {
			if (d.getVALUE().contains("/")) {
				String data[] = d.getVALUE().split("/");
				this.jobDescription = data[1];
				this.jobDescriptionLibrary = data[0];
			} else {
				this.jobDescription = d.getVALUE();
			}
		} else if (d.getNAME().equals("%%OS400-JOBQ")) {
			if (d.getVALUE().contains("/")) {
				String data[] = d.getVALUE().split("/");
				this.jobQueue = data[1];
				this.jobQueueLibrary = data[0];
			} else {
				this.jobQueue = d.getVALUE();
			}
		} else if (d.getNAME().equals("%%OS400-OUTQ")) {
			if (d.getVALUE().contains("/")) {
				String data[] = d.getVALUE().split("/");
				this.outQueueName = data[1];
				this.outQueueLibrary = data[0];
			} else {
				this.outQueueName = d.getVALUE();
			}
		} else if (d.getNAME().equals("%%OS400-MSGQ")) {
			if (d.getVALUE().contains("/")) {
				String data[] = d.getVALUE().split("/");
				this.messageQueue = data[1];
				this.messageQueueLibrary = data[0];
			} else {
				this.messageQueue = d.getVALUE();
			}
		} else if (d.getNAME().equals("%%OS400-INQMSGRPY")) {
			this.inqueryMessageReply = d.getVALUE();
		} else if (d.getNAME().equals("%%OS400-AEV_LEN")) {
			this.aevLenght = d.getVALUE(); // No idea what this is for
		} else if (d.getNAME().equals("%%OS400-SCRIPT_FILE_IGNERR")) {
			this.scriptFileIgnoreError = d.getVALUE(); // No idea what this is for
		} else if (d.getNAME().equals("%%OS400-DATE")) {
			this.date = d.getVALUE(); // No idea what this is for
		} else if (d.getNAME().equals("%%OS400-LOG")) {
			if (d.getVALUE() != null) {
				// SetVarData(name=%%OS400-LOG, value=4 00 *SECLVL)
				String vals[] = d.getVALUE().split(" ");
				this.messageLogLevel = vals[0];
				this.messageLogSeverity = vals[1];
				this.messageLogText = vals[2];
			}

		} else if (d.getNAME().equals("%%OS400-SCRIPT_FILE_LOGINFMSG")) {
			this.scriptFileLogInformationMessage = d.getVALUE();// No idea what this is for
		} else if (d.getNAME().equals("%%OS400-SKIP_VALIDITY")) {
			this.skipValidity = d.getVALUE(); // No idea what this is for
		} else if (d.getNAME().equals("%%OS400-JOBPTY")) {
			this.jobPriorityOnQueue = d.getVALUE(); // Page 1
		} else if (d.getNAME().equals("%%OS400-CURLIB")) {
			this.currentLibrary = d.getVALUE();
		} else if (d.getNAME().equals("%%OS400-RUNPTY")) {
			this.runPriority = d.getVALUE(); // No idea what this is for
		} else if (d.getNAME().startsWith("%%OS400-LDA")) { // FIXME: TIDAL DOES NOT SUPPORT THIS YET
			this.ldaData.add(d.getVALUE());
		} else if (d.getNAME().equals("%%OS400-HOLD")) {
			this.holdOnJobQueue = d.getVALUE(); // Page 3
		} else if (d.getNAME().equals("%%OS400-ASPGRP")) {
			this.initalASPGroup = d.getVALUE(); // Page 4
		} else if (d.getNAME().equals("%%OS400-LOGCLPGM")) {
			this.logCLProgramCommands = d.getVALUE(); // Page 3
		} else if (d.getNAME().equals("%%SYSAUDDATE")) {
			this.systemAudDate = d.getVALUE(); // Page 3
		} else if (d.getNAME().equals("%%MYDAY")) {
			this.mydayVar = d.getVALUE(); // Page 3
		} else if (d.getNAME().startsWith("%%OS400-PARM")) {
			this.params.add(d.getVALUE());
		} else if (d.getNAME().equals("%%OS400-PRTTXT")) {
			this.printText = d.getVALUE(); // Page 3
		} else if (d.getNAME().equals("%%OS400-INLLIBL1")) {
			this.initidalLibrary = d.getVALUE(); // Page 3
		} else if (d.getNAME().equals("%%OS400-JOBMSGQFL")) {
			this.fullAction = d.getVALUE(); // Page 3
		} else if (d.getNAME().equals("%%OS400-JOBMSGQMX")) {
			this.jobMessageQueueMaxSize = d.getVALUE(); // Page 3
		} else if (d.getNAME().startsWith("%%OS400-CMDLINE")) {

			for (int i = 1; i <= 15; i++) { // Guesing 10 is our max
				String key = "%%OS400-CMDLINE" + Integer.toString(i);

				if (d.getNAME().equals(key)) {
					OSCommand osc = new OSCommand(d);
					// With Crate, there is bad data.. same command listed twice. odd.
					OSCommand existing = this.getCommands().stream().filter(f -> f.getName().equals(osc.getName())).findFirst().orElse(null);
					if (existing == null) {
						this.getCommands().add(osc);
					}
					break;
					// this.getCommands().add(osc);
				} else if (d.getNAME().startsWith(key + "_")) {
					// We don't care right now but we may in the future.
					OSCommand osc = this.getCommands().stream().filter(f -> f.getName().equals(key)).findFirst().orElse(null);
					if (osc != null) {
						osc.getUnSetData().add(d);

					}
				}

			}
			// this.commands.add(d.getVALUE());
		} else if (d.getNAME().startsWith("%%OS400-POSTCMD")) {

			for (int i = 1; i <= 15; i++) { // Guesing 10 is our max
				String key = "%%OS400-POSTCMD" + Integer.toString(i);

				if (d.getNAME().equals(key)) {
					OSCommand osc = new OSCommand(d);
					this.getPostCommands().add(osc);
				} else if (d.getNAME().startsWith(key + "_")) {
					// We don't care right now but we may in the future.
					OSCommand osc = this.getPostCommands().stream().filter(f -> f.getName().equals(key)).findFirst().orElse(null);
					if (osc != null) {
						osc.getUnSetData().add(d);

					}
				}

			}
		} else {
			unusedVarData.add(d); // What can this all be?
		}

		/*
		 * <VARIABLE NAME="%%OS400-CMDLINE1" VALUE="STRBKUBRM CTLGRP(NOTSAVEDD) SBMJOB(*NO)" />
		 * <VARIABLE NAME="%%OS400-CMDLINE2"
		 * VALUE="SAVMEDIBRM DEV(DEV_VTL01) MEDPCY(DEVDLYDD5) OPTION(*OBJ) ENDOPT(*UNLOAD)" />
		 * <VARIABLE NAME="%%OS400-CMDLINE3" VALUE="MOVMEDBRM SYSNAME(*LCL)" />
		 * <VARIABLE NAME="%%OS400-CMDLINE4"
		 * VALUE="STRMNTBRM EXPSETMED(*YES) RMVLOGE(*ALL *BEGIN 30) RTVVOLSTAT(*NO) PRTEXPMED(*NO) PRTVSNRPT(*NO) PRTBKUACT(*NO)"
		 * />
		 * <VARIABLE NAME="%%OS400-CMDLINE1_IGNERR" VALUE="Y" />
		 * <VARIABLE NAME="%%OS400-POSTCMD1"
		 * VALUE="OVRDBF FILE(PLPXPKUP) TOFILE(%%DTALIB/PLPXPKUP) MBR(*FIRST) OVRSCOPE(*JOB)" />
		 * <VARIABLE NAME="%%OS400-POSTCMD2" VALUE="CALL PLG0810 PARM(%%FTPNAME %%FTPSTATUS)" />
		 * <VARIABLE NAME="%%OS400-POSTCMD3" VALUE="DLTOVR FILE(PLPXPKUP) LVL(*JOB)" />
		 */
		// %%OS400-JOBMSGQMX
	}

	@Override
	public String getVarPrefix() {
		return "%%OS400-";
	}

	@Data
	public class OSCommand {

		List<SetVarData> unSetData = new ArrayList<>();
		String name;
		String value;
		SetVarData data;

		public OSCommand(SetVarData data) {
			this.data = data;
			this.name = data.getNAME();
			this.value = data.getVALUE();
		}

	}

	public enum ObjectTypes {
		MULTICMD, PGM, CMDLINE
	}
}
