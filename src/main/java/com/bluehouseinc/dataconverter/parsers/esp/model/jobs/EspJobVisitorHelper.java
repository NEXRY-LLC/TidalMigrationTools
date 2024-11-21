package com.bluehouseinc.dataconverter.parsers.esp.model.jobs;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspDataModel;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.CcCheck;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspAppEndData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspExternalApplicationData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspLIEData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspLISData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspLinkProcessData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAgentMonitorJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAixJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAs400Job;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspDStrigJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspFileTriggerJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspFtpJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspLinuxJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSAPBwpcJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSapEventJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSapJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSecureCopyJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspServiceMonitorJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSftpJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspTaskProcessJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspTextMonJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspUnixJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspWindowsJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspZosJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspEnvVarStatement;
import com.bluehouseinc.tidal.utils.StringUtils;

import io.vavr.Function2;

public class EspJobVisitorHelper {

	EspDataModel datamodel;

	EspJobVisitorHelper(EspDataModel datamodel) {
		this.datamodel = datamodel;
	}

	Function2<String, String, Boolean> visitJob(EspAgentMonitorJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "MSGQLEN":
				job.setMsgQLen(Integer.parseInt(statementParameters));
				break;
			case "STATINTV":
				job.setStatIntV(Integer.parseInt(statementParameters));
				break;
			default:
				// no statement hit
				return false;
			}

			return true;
		};
	}

	/**
	 * Return the {@link Function2} for our {@link EspAixJob}
	 *
	 * @param espAixJob
	 * @return
	 */
	Function2<String, String, Boolean> visitJob(EspAixJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "SCRIPTNAME":
			case "COMMAND":
			case "CMDNAME":
				job.setCommand(statementParameters); // later convert it into TIDAL's Command parameters
				break;
			case "ARGS":
				job.setParams(statementParameters);
				break;
			default:
				return false;
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(final EspAs400Job job) {

		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "AS400FILE":
				job.setAs400File(statementParameters);
				break;
			case "CLPNAME":
				job.setClpName(statementParameters);
				break;
			case "COMMAND":
				job.setCommand(statementParameters);
				break;
			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspAs400Job.EspAs400JobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspDStrigJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "DSNAME":
				job.setDsName(statementParameters);
				break;

			default:
				// no statement hit
				return false;
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspFileTriggerJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "FILENAME":
				// FIXME: could be probably set to localFileName ORR remoteName -> check with
				// Brian and client!
				job.setFileName(statementParameters);
				break;
			case "JOBCLASS":
				job.setJobClass(statementParameters);
				break;

			default:
				// no statement hit
				return false;
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspFtpJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "FTPFORMAT":
				job.setFtpFormat(statementParameters);
				break;
			case "REMOTEFILENAME":
				job.setRemoteFileName(statementParameters);
				break;
			case "SERVERPORT":
				job.setServerPort(statementParameters);
				break;
			case "LOCALFILENAME":
				job.setLocalFileName(statementParameters);
				break;
			case "TRANSFERDIRECTION":
				job.setTransferDirection(EspFtpJob.TransferDirection.valueOf(statementParameters));
				break;

			default:
				// no statement hit
				return false;
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspLinkProcessData job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {

			case "CCCHK":
				break;
			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspLinkProcessData.EspLISOptionalStatements.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		};
	}

	/**
	 * Return the {@link Function2} for our {@link EspAixJob}
	 *
	 * @param espAixJob
	 * @return
	 */
	Function2<String, String, Boolean> visitJob(EspLinuxJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "ABANDON":
				job.setAbandon(statementParameters);
				break;
			case "ARGS":
				job.setParams(statementParameters);
				break;
			case "CMDNAME":
			case "SCRIPTNAME":
			case "scriptname":
			case "COMMAND":
				job.setCommand(statementParameters);
				break;
			case "user":
				break;
			default:
				// no statement hit
				return false;
			}

			return true;
		};
	}

	/**
	 * Return the {@link Function2} for our {@link EspAixJob}
	 *
	 * @param espAixJob
	 * @return
	 */
	Function2<String, String, Boolean> visitJob(EspZosJob job) {

		String command = datamodel.getConfigeProvider().getZosLibPath();

		// Per customer , any job name with a dot in it , the dot and all data to the right is not used.
		// Display only
		String cliname = job.getName();
		if (cliname.contains(".")) {
			cliname = cliname.substring(0, cliname.indexOf("."));
		}
		command = command + "(" + cliname + ")";

		job.setCommandLine(command);

		String runtime = datamodel.getConfigeProvider().getZosLibDefaultRuntimeUser();
		job.setUser(runtime);

		String agent = datamodel.getConfigeProvider().getZosLibDefaultAgent();
		job.setAgent(agent);

		return (statementType, statementParameters) -> {

			if (statementType.contains("ESP_RECIPIENT_") || statementType.contains("ESP_MESSAGE_") || statementType.contains("ESP_HEADER") || statementType.contains("REM_")) {
				return true; // Just say yes, not working with this data.
			}
			switch (statementType) {
			case "DEQUEUE":
				job.setDeQueue(statementParameters);
				break;
			case "SEND":
				job.setSend(statementParameters);
				break;
			case "MEMBER":
				job.setMember(statementParameters);
				break;
			case "DATASET":
				job.setDataSet(statementParameters);
				break;
			case "ESPNOMSG":
				job.setEspNoMsg(statementParameters);
				break;
			case "ABANDON":
				job.setAbandon(statementParameters);
				break;
			case "ECHO":
				job.getEchos().add(statementParameters);
				break;
			case "encparm":
			case "ENCPARM":
				job.setEncParam(statementParameters);
				break;
			case "CCCHK":
				job.getCcchks().add(extractCheck(statementParameters, job));
				break;
			case "ESP_SUBJECT":
			case "ESP_BG_COLOR":
			case "GENTIME":
			case "REEXEC":
				break;
			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspZosJob.EspZoSOptionalStatements.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		};
	}

	private CcCheck extractCheck(String data, EspZosJob job) {
		CcCheck check = new CcCheck();

		String regexstep = "^STEP\\((.+)\\) PROC\\((.+)\\) RC\\((.+)\\) .*";
		String regexrange = "^RC\\((\\d+):(\\d+)\\).*";
		String regexsingle = "^RC\\((\\d+)\\).*";

		if (data.contains("OK CONTINUE")) {
			check.setOkContinue(true);
		} else {
			check.setOkContinue(false);
		}

		if (RegexHelper.matchesRegexPattern(data, regexstep)) {
			// CCCHK STEP(STEP20) PROC(STEP01) RC(1) OK CONTINUE

			String stepname = RegexHelper.extractNthMatch(data, regexstep, 0);
			String procename = RegexHelper.extractNthMatch(data, regexstep, 1);
			String returncode = RegexHelper.extractNthMatch(data, regexstep, 2);

			check.setProcessStepName(stepname);
			check.setProccessName(procename);
			check.setProcessReturnCode(Integer.valueOf(returncode));
			check.setStepProcessCheck(true);
		} else if (RegexHelper.matchesRegexPattern(data, regexrange)) {
			// CCCHK RC(1:4095) FAIL CONTINUE
			String rangestart = RegexHelper.extractNthMatch(data, regexrange, 0);
			String rangeendend = RegexHelper.extractNthMatch(data, regexrange, 1);
			check.setRangeStartCode((Integer.valueOf(rangestart)));
			check.setRangeEndCode(Integer.valueOf(rangeendend));
			check.setRangeCheck(true);
		} else if (RegexHelper.matchesRegexPattern(data, regexsingle)) {
			// CCCHK RC(1) OK CONTINUE
			// singleReturnCode
			String singlecode = RegexHelper.extractNthMatch(data, regexsingle, 0);
			check.setSingleReturnCode(Integer.valueOf(singlecode));
			check.setSingleCheck(true);
		} else {
			// Not handled
			job.setComplexCcCheck(true);
		}

		return check;
	}
	// EspSAPBwpcJob

	Function2<String, String, Boolean> visitJob(EspSAPBwpcJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "CHAIN":
				job.setChain(statementParameters);
				break;

			default:
				// no statement hit
				return false;
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspSapEventJob job) {

		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "EVENT":
				job.setEvent(statementParameters);
				break;

			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspSapEventJob.EspSapEventJobOptionalStatement.valueOf(statementType), statementParameters);

				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(final EspSapJob job) {
		

		return (statementType, statementParameters) -> {
			String name = job.getName();
			if (name.contains("JDAOP_ZOP_RE")) {
				name.getBytes();
			}
			switch (statementType) {
			case "ABAPNAME":
				if (StringUtils.isBlank(job.getAbapName())) {
					job.setAbapName(statementParameters);
				} else {
					job.setMultiplePrograms(true);
				}
				break;
			case "SAPUSER":
				job.setSapUser(statementParameters);
				break;
			case "STEPUSER":
				job.setSapStepUser(statementParameters);
				break;
			case "SAPJOBCLASS":
				job.setSapJobClass(statementParameters);
				break;
			case "SAPJOBNAME":
				job.setSapJobName(statementParameters);
				break;
			case "VARIANT":
				if (StringUtils.isBlank(job.getVariant())) {
					job.setVariant(statementParameters);
				} else {
					job.setMultiplePrograms(true);
				}
				break;
			case "STARTMODE":
				job.setStartMode(statementParameters);
				break;
			case "COLUMNS":
				job.setPrintColumns(Integer.valueOf(statementParameters));
				break;
			case "RECIPIENT":
				job.setPrintRecipient(statementParameters);
				break;
			case "PRINTDEPARTMENT":
				job.setPrintDept(statementParameters);
				break;
			case "EXPIRATION":
				job.setPrintExpire(Integer.valueOf(statementParameters));
				break;
			case "LINES":
				job.setPrintRows(Integer.valueOf(statementParameters));
				break;
			case "PRINTFORMAT":
				job.setPrintFormat(statementParameters);
				break;
			case "PRINTSPOOLNAME":
				job.setPrintSpoolName(statementParameters);
				break;
			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspSapJob.EspSapJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspSecureCopyJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "REMOTEDIR":
				job.setRemoteDir(statementParameters);
				break;
			case "REMOTENAME":
				job.setRemoteName(statementParameters);
				break;
			case "SERVERADDR":
				job.setServerAddr(statementParameters);
				break;
			case "TRANSFERDIRECTION":
				job.setTransferDirection(EspSecureCopyJob.TransferDirection.valueOf(statementParameters));
				break;

			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspSecureCopyJob.EspSecureCopyJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspServiceMonitorJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "SERVICENAME":
				job.setServiceName(statementParameters);
				break;
			case "STATUS":
				job.setStatus(statementParameters);
				break;

			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspServiceMonitorJob.EspServiceMonitorJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspSftpJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "REMOTEDIR":
				job.setRemoteDir(statementParameters);
				break;
			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspSftpJob.EspSftpJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspTextMonJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "SEARCHRANGE":
				job.setSearchRange(statementParameters);
				break;
			case "TEXTFILE":
				job.setTextFile(statementParameters);
				break;
			case "TEXTSTRING":
				job.setTextString(statementParameters);
				break;

			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspTextMonJob.EspTextMonJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}
			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspUnixJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "scriptname":
			case "SCRIPTNAME":
			case "CMDNAME":
				job.setCommand(statementParameters);
				break;
			case "args":// necessary since there is one defined in cfg/bfusa/ESP/Test/BFTARCHI and cfg/bfusa/ESP/Test/BFTARCHO file
			case "ARGS":
				job.setParams(statementParameters);
				break;
			default:
				// no statement hit
				return false;

			}
			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspWindowsJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "CMDNAME":
				job.setCommand(statementParameters);
				break;
			case "ARGS":
				job.setParams(statementParameters);
				break;
			case "ENVAR":
				job.setEnvironmentVariable(extractEnvVarStatement(statementParameters));
				break;
			case "CCCHK":
				break;
			default:
				return false;
			}
			return true;
		};
	}

	static EspEnvVarStatement extractEnvVarStatement(String statementParameters) {
		String[] statementParametersElements = statementParameters.split("=");
		String environmentVariableName = statementParametersElements[0];
		String environmentVariableValue = statementParametersElements[1];
		return new EspEnvVarStatement(environmentVariableName, environmentVariableValue);
	}

	Function2<String, String, Boolean> visitJob(EspAppEndData job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {

			default:
				try {
					job.getOptionalStatements().putIfAbsent(EspAppEndData.EspOptionalStatements.valueOf(statementType), statementParameters);
				} catch (Exception e) {

					return false;
				}
			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspLIEData job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "ARGS":
			case "CMDNAME":
			case "COMMAND":
			case "MEMBER":
				break;
			default:
				// no statement hit
				return false;

			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspLISData job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "ARGS":
			case "COMMAND":
				break;
			default:
				// no statement hit
				return false;

			}

			return true;
		};
	}

	Function2<String, String, Boolean> visitJob(EspExternalApplicationData job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {

			default:
				// no statement hit
				return false;

			}

		};
	}

	Function2<String, String, Boolean> visitJob(EspTaskProcessJob job) {
		return (statementType, statementParameters) -> {
			switch (statementType) {
			case "SEND":
				break;
			default:
				// no statement hit
				return false;

			}
			return true;
		};
	}
}
