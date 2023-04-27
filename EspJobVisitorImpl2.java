package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspDataModel;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitor;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspAfterStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspEnvVarStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspExitCodeStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspJobResourceStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspNoRunStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspNotWithStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspPrereqStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspReleaseStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspRunStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspSetVarStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspStatementType;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobDependencyTerminationStatus;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

import io.vavr.Function2;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class EspJobVisitorImpl2 implements EspJobVisitor {
	EspDataModel model;

	public EspJobVisitorImpl2(EspDataModel model) {
		this.model = model;
	}

	private String extractVariableValue(String criteria, EspJobGroup espJobGroup) {
		// First check does jobGroup contain any variable
		if (espJobGroup.getVariables() == null)
			return null;
		Set<String> variableNames = espJobGroup.getVariables().keySet();
		String key = variableNames.stream().filter(criteria::equals).findFirst().orElse(null);
		return key != null ? espJobGroup.getVariables().get(key) : null;
	}

	private String replaceInlineVariablesWithValues(String statementType, String statementParameters, EspJobGroup espJobGroup) {
		if (statementParameters.contains("WOBDATA")) {
			String varValue = extractVariableValue(statementType,espJobGroup);
			if (varValue != null) {
				return varValue;
			}

			return statementParameters;
		} else {
			String updatedStatement = statementParameters;
			List<String> statementParametersElements = Arrays.stream(statementParameters.split(" ")).filter(s -> !StringUtils.isBlank(s) && s.contains("!")).collect(Collectors.toList());

			for (String element : statementParametersElements) {
				String varKey = element.replace("!", "");
				String varValue = extractVariableValue(varKey,espJobGroup);
				if (varValue != null && !varValue.equals("''")) {
					updatedStatement = updatedStatement.replace(element, varValue);

					if (varValue.contains("!")) {
						String mappedValue = model.getVariableProcessor().fromString(varValue);
						if (mappedValue != null)
							updatedStatement = updatedStatement.replace(varValue, mappedValue);
					}
				} else {
					String mappedValue = model.getVariableProcessor().fromString(element);
					if (mappedValue != null)
						updatedStatement = updatedStatement.replace(element, mappedValue);
				}

			}

			return updatedStatement;
		}
	}

	// Function2 is dynamic function that accepts 2 params and returns a boolean value
	// whether a second invoked switch hit a statement or not
	private void visitCommon(EspAbstractJob job, List<String> lines, Function2<String, String, Boolean> lambdaFunction) {
		List<String> notifications = new ArrayList<>();
		List<EspReleaseStatement> releaseJobDependencies = new ArrayList<>();
		List<EspJobResourceStatement> jobResources = new ArrayList<>();
		List<EspAfterStatement> afterStatements = new ArrayList<>();
		List<EspNotWithStatement> notWithStatements = new ArrayList<>();
		List<EspExitCodeStatement> exitCodeStatements = new ArrayList<>();
		List<EspRunStatement> runStatements = new ArrayList<>();
		List<EspNoRunStatement> noRunStatements = new ArrayList<>();
		List<String> noteStrings = new ArrayList<>();

		for (String line : lines) {
			if (line.startsWith("/*")) {
				noteStrings.add(line.substring(line.indexOf("/*") + 2));
				continue;
			}

			String[] statementParts;
			// line may contain following values, values are notated on right-side of <=> operator:
			// line<=>STMPTM= WOBDATA('PARMSET.!ESPAPPL','STMPTM')
			// line<=>ARGS --env=stg --service STATEMENT_XML_FILE_GENERATION--host ...
			if (line.contains("WOBDATA")) {
				statementParts = line.split("=", 2); // This will result: statementParts[0]=ARGS --env
			} else {
				statementParts = line.split(" ", 2);
			}
			String statementType = statementParts[0];
			String statementParameters;
			if (statementParts.length < 2) {
				continue;
			} else {
				statementParameters = statementParts[1];
				if (statementParameters.contains("!")) {
					statementParameters = replaceInlineVariablesWithValues(statementType, statementParameters, (EspJobGroup) job.getParent());
				}
			}

			switch (statementType) {
			case "AFTER":
				afterStatements.add(extractAfterStatement(statementParameters));
				continue;
			case "EXITCODE":
				exitCodeStatements.add(extractExitCodeStatement(statementParameters));
				continue;
			case "NOTIFY":
				notifications.add(statementParameters);
				continue;
			case "NOTWITH":
				// TODO-4: Check how scope of NOTWITH statement referencing to ONLY Test or also Prod directory/environment!!!
				notWithStatements.add(extractNotWithStatement(statementParameters));
				continue;
			case "NORUN":
				noRunStatements.add(new EspNoRunStatement(statementParameters));
				continue;
			case "RELEASE":
				releaseJobDependencies.add(extractReleaseJobDependencies(statementParameters));
				continue;
			case "RESOURCE":
				jobResources.add(extractJobResource(statementParameters));
				continue;
			case "RUN":
				runStatements.add(extractEspRunStatement(statementParameters));
				continue;
			default:
				// call lambda
				boolean hasHitStatement = lambdaFunction.apply(statementType, statementParameters);
				if (hasHitStatement)
					continue;
			}

			fillInDefaultProperties(job, statementType, statementParameters);
		}

		if (!afterStatements.isEmpty()) {
			job.setEspAfterStatements(afterStatements);
		}
		if (!exitCodeStatements.isEmpty()) {
			job.setExitCodeStatements(exitCodeStatements);
		}
		if (!notifications.isEmpty()) {
			job.setNotifyList(notifications);
		}
		if (!noRunStatements.isEmpty()) {
			job.setEspNoRunStatements(noRunStatements);
		}
		if (!runStatements.isEmpty()) {
			job.setEspRunStatements(runStatements);
		}
		if (!notWithStatements.isEmpty()) {
			job.setEspNotWithStatements(notWithStatements);
		}
		if (!releaseJobDependencies.isEmpty()) {
			job.setEspReleasedJobDependencies(releaseJobDependencies);
		}
		if (!jobResources.isEmpty()) {
			job.setResources(jobResources);
		}
		if (!noteStrings.isEmpty()) {
			job.getNoteData().addAll(noteStrings);
		}
	}

	@Override
	public void visit(EspAgentMonitorJob espAgentMonitorJob, List<String> lines) {
		this.visitCommon(espAgentMonitorJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "MSGQLEN":
				espAgentMonitorJob.setMsgQLen(Integer.parseInt(statementParameters));
				break;
			case "STATINTV":
				espAgentMonitorJob.setStatIntV(Integer.parseInt(statementParameters));
				break;
			default:
				// no statement hit
				return false;
			}

			return true;
		});
	}

	@Override
	public void visit(EspAixJob espAixJob, List<String> lines) {
		this.visitCommon(espAixJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "CMDNAME":
				espAixJob.setCmdName(statementParameters);
				break;
			case "COMMAND":
				espAixJob.setCommand(statementParameters); // later convert it into TIDAL's Command parameters
				break;
			case "STMPTM":
				espAixJob.setStmptm(statementParameters);
				break;
			case "SCRIPTNAME":
				espAixJob.setScriptName(statementParameters);
				break;
			case "ARGS":
				espAixJob.setArgs(statementParameters);
				break;
			default:
				// no statement hit
				return false;
			}

			return true;
		});
	}

	@Override
	public void visit(EspAs400Job espAs400Job, List<String> lines) {
		Map<EspAs400Job.EspAs400JobOptionalStatement, String> optionalStatements = new HashMap<>();

		this.visitCommon(espAs400Job, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "AS400FILE":
				espAs400Job.setAs400File(statementParameters);
				break;
			case "CLPNAME":
				espAs400Job.setClpName(statementParameters);
				break;
			case "COMMAND":
				espAs400Job.setCommand(statementParameters);
				break;
			default:
				try {
					optionalStatements.putIfAbsent(EspAs400Job.EspAs400JobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		if (!optionalStatements.isEmpty()) {
			espAs400Job.setOptionalStatements(optionalStatements);
		}
	}

	@Override
	public void visit(EspSAPBwpcJob espBwpcJob, List<String> lines) {
		this.visitCommon(espBwpcJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "CHAIN":
				espBwpcJob.setChain(statementParameters);
				break;

			default:
				// no statement hit
				return false;
			}

			return true;
		});
	}

	@Override
	public void visit(EspDataObjectJob espDataObjectJob, List<String> lines) {
		List<EspSetVarStatement> variableList = new ArrayList<>();

		this.visitCommon(espDataObjectJob, lines, (statementType, statementParameters) -> {

			return true;
		});

		for (String line : lines) {

			if (line.contains("=")) {
				String[] statementParts = line.split(" ", 2);
				String statementParameters = statementParts[1]; // e.g. TMSTMP='!ESPSHH.:!ESPSMN.,!ESPSMM./!ESPSDD./!ESPSYY'
				variableList.add(extractVariable(statementParameters));
			}
		}

		if (!variableList.isEmpty()) {
			espDataObjectJob.setVariables(variableList);
		}

	}

	private EspSetVarStatement extractVariable(String statementParameters) {
		// E.g. statementParameters<=>TMSTMP='!ESPSHH.:!ESPSMN.,!ESPSMM./!ESPSDD./!ESPSYY'
		String[] variableParStrings = statementParameters.split("=");
		return new EspSetVarStatement(variableParStrings[0], variableParStrings[1]);
	}

	@Override
	public void visit(EspDStrigJob espDStrigJob, List<String> lines) {
		this.visitCommon(espDStrigJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "DSNAME":
				espDStrigJob.setDsName(statementParameters);
				break;

			default:
				// no statement hit
				return false;
			}

			return true;
		});
	}

	@Override
	public void visit(EspFileTriggerJob espFileTriggerJob, List<String> lines) {
		this.visitCommon(espFileTriggerJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "FILENAME":
				// FIXME: could be probably set to localFileName ORR remoteName -> check with
				// Brian and client!
				espFileTriggerJob.setFileName(statementParameters);
				break;
			case "JOBCLASS":
				espFileTriggerJob.setJobClass(statementParameters);
				break;

			default:
				// no statement hit
				return false;
			}

			return true;
		});
	}

	@Override
	public void visit(EspFtpJob espFtpJob, List<String> lines) {
		this.visitCommon(espFtpJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "FTPFORMAT":
				espFtpJob.setFtpFormat(statementParameters);
				break;
			case "REMOTEFILENAME":
				espFtpJob.setRemoteFileName(statementParameters);
				break;
			case "SERVERPORT":
				espFtpJob.setServerPort(statementParameters);
				break;
			case "LOCALFILENAME":
				espFtpJob.setLocalFileName(statementParameters);
				break;
			case "TRANSFERDIRECTION":
				espFtpJob.setTransferDirection(EspFtpJob.TransferDirection.valueOf(statementParameters));
				break;

			default:
				// no statement hit
				return false;
			}

			return true;
		});
	}

	@Override
	public void visit(EspLinuxJob espLinuxJob, List<String> lines) {
		this.visitCommon(espLinuxJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "ARGS":
				espLinuxJob.setArgs(statementParameters);
				break;
			case "COMMAND":
				espLinuxJob.setCommand(statementParameters);
				break;
			case "ENVAR":
				espLinuxJob.setEnVar(statementParameters);
				break;
			case "JOBCLASS":
				espLinuxJob.setJobClass(statementParameters);
				break;
			case "PROCESS_PRIORITY":
				espLinuxJob.setProcessPriority(statementParameters);
				break;
			case "SHELL":
				espLinuxJob.setShell(statementParameters);
				break;
			case "SCRIPTNAME":
				espLinuxJob.setScriptName(statementParameters);
				break;
			default:
				// no statement hit
				return false;
			}

			return true;
		});

	}

	@Override
	public void visit(EspZosJob espPlainJob, List<String> lines) {
		Map<EspStatementType, String> standardStatements = new HashMap<>();

		this.visitCommon(espPlainJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "ESPNOMSG":
				espPlainJob.setEspNoMsg(statementParameters);
				break;
			case "CCCHK":
				if (espPlainJob.getCcchk() == null) {
					List<String> ccchk = new ArrayList<>();
					espPlainJob.setCcchk(ccchk);
				}
				// Each of these represent an exit code statement to continue if found. Not
				// coding this right now
				// Need to understand TIDAL side more.
				espPlainJob.getCcchk().add(statementParameters);
				break;
			default:
				try {
					standardStatements.putIfAbsent(EspStatementType.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		espPlainJob.setOptionalStatements(standardStatements);
	}

	@Override
	public void visit(EspSapEventJob espSapEventJob, List<String> lines) {
		Map<EspSapEventJob.EspSapEventJobOptionalStatement, String> optionalStatements = new HashMap<>();

		this.visitCommon(espSapEventJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "EVENT":
				espSapEventJob.setEvent(statementParameters);
				break;

			default:
				try {
					optionalStatements.putIfAbsent(EspSapEventJob.EspSapEventJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		espSapEventJob.setOptionalStatements(optionalStatements);
	}

	@Override
	public void visit(EspSapJob espSapJob, List<String> lines) {
		Map<EspSapJob.EspSapJobOptionalStatement, String> optionalStatements = new HashMap<>();

		this.visitCommon(espSapJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "ABAPNAME":
				espSapJob.setAbapName(statementParameters);
				break;

			default:
				try {
					optionalStatements.putIfAbsent(EspSapJob.EspSapJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		espSapJob.setOptionalStatements(optionalStatements);
	}

	@Override
	public void visit(EspSecureCopyJob espSecureCopyJob, List<String> lines) {
		Map<EspSecureCopyJob.EspSecureCopyJobOptionalStatement, String> optionalStatements = new HashMap<>();

		this.visitCommon(espSecureCopyJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "REMOTEDIR":
				espSecureCopyJob.setRemoteDir(statementParameters);
				break;
			case "REMOTENAME":
				espSecureCopyJob.setRemoteName(statementParameters);
				break;
			case "SERVERADDR":
				espSecureCopyJob.setServerAddr(statementParameters);
				break;
			case "TRANSFERDIRECTION":
				espSecureCopyJob.setTransferDirection(EspSecureCopyJob.TransferDirection.valueOf(statementParameters));
				break;

			default:
				try {
					optionalStatements.putIfAbsent(EspSecureCopyJob.EspSecureCopyJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		espSecureCopyJob.setOptionalStatements(optionalStatements);
	}

	@Override
	public void visit(EspServiceMonitorJob espServiceMonitorJob, List<String> lines) {
		Map<EspServiceMonitorJob.EspServiceMonitorJobOptionalStatement, String> optionalStatements = new HashMap<>();

		this.visitCommon(espServiceMonitorJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "SERVICENAME":
				espServiceMonitorJob.setServiceName(statementParameters);
				break;
			case "STATUS":
				espServiceMonitorJob.setStatus(statementParameters);
				break;

			default:
				try {
					optionalStatements.putIfAbsent(EspServiceMonitorJob.EspServiceMonitorJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		espServiceMonitorJob.setOptionalStatements(optionalStatements);
	}

	@Override
	public void visit(EspSftpJob espSftpJob, List<String> lines) {
		Map<EspSftpJob.EspSftpJobOptionalStatement, String> optionalStatements = new HashMap<>();

		this.visitCommon(espSftpJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "REMOTEDIR":
				espSftpJob.setRemoteDir(statementParameters);
				break;
			default:
				try {
					optionalStatements.putIfAbsent(EspSftpJob.EspSftpJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		espSftpJob.setOptionalStatements(optionalStatements);
	}

	@Override
	public void visit(EspTextMonJob espTextMonJob, List<String> lines) {
		Map<EspTextMonJob.EspTextMonJobOptionalStatement, String> optionalStatements = new HashMap<>();

		this.visitCommon(espTextMonJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "SEARCHRANGE":
				espTextMonJob.setSearchRange(statementParameters);
				break;
			case "TEXTFILE":
				espTextMonJob.setTextFile(statementParameters);
				break;
			case "TEXTSTRING":
				espTextMonJob.setTextString(statementParameters);
				break;

			default:
				try {
					optionalStatements.putIfAbsent(EspTextMonJob.EspTextMonJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		espTextMonJob.setOptionalStatements(optionalStatements);
	}

	@Override
	public void visit(EspUnixJob espUnixJob, List<String> lines) {
		Map<EspUnixJob.EspUnixJobOptionalStatement, String> optionalStatements = new HashMap<>();

		this.visitCommon(espUnixJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "SCRIPTNAME":
				espUnixJob.setScriptName(statementParameters);
				break;
			case "args":// necessary since there is one defined in cfg/bfusa/ESP/Test/BFTARCHI and cfg/bfusa/ESP/Test/BFTARCHO file
			case "ARGS":
				espUnixJob.setArgs(statementParameters);
				break;
			default:
				try {
					optionalStatements.putIfAbsent(EspUnixJob.EspUnixJobOptionalStatement.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		espUnixJob.setOptionalStatements(optionalStatements);
	}

	@Override
	public void visit(EspWindowsJob espWindowsJob, List<String> lines) {
		Map<EspWindowsJob.EspWindowsJobOptionalStatements, String> optionalStatements = new HashMap<>();

		this.visitCommon(espWindowsJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {
			case "CMDNAME":
				espWindowsJob.setCmdName(statementParameters);
				break;
			case "ENVAR":
				espWindowsJob.setEnvironmentVariable(extractEnvVarStatement(statementParameters));
				break;
			case "ARGS":
				espWindowsJob.setArgs(statementParameters);
				break;
			case "TRANSFERDIRECTION":
				espWindowsJob.setTransferDirection(EspWindowsJob.TransferDirection.valueOf(statementParameters));
				break;
			default:
				try {
					optionalStatements.putIfAbsent(EspWindowsJob.EspWindowsJobOptionalStatements.valueOf(statementType), statementParameters);
				} catch (Exception e) {
					// no statement hit
					return false;
				}
			}

			return true;
		});

		espWindowsJob.setOptionalStatements(optionalStatements);
	}

	// ======================================= Helper methods =======================================

	private EspEnvVarStatement extractEnvVarStatement(String statementParameters) {
		String[] statementParametersElements = statementParameters.split("=");
		String environmentVariableName = statementParametersElements[0];
		String environmentVariableValue = statementParametersElements[1];
		return new EspEnvVarStatement(environmentVariableName, environmentVariableValue);
	}

	private EspJobResourceStatement extractJobResource(String jobResource) {
		String[] resourceParams = jobResource.substring(jobResource.indexOf("(") + 1, jobResource.indexOf(")")).split(",");
		int limit = Integer.parseInt(resourceParams[0]);
		String resourceName = resourceParams[1];

		return new EspJobResourceStatement(limit, resourceName);
	}

	private void fillInDefaultProperties(EspAbstractJob espJob, String statementType, String statementParameters) {

		try {
			switch (EspStatementType.valueOf(statementType)) {
			case AGENT:
				espJob.setAgent(statementParameters);
				break;
			case DELAYSUB:
				espJob.setDelaySubmission(statementParameters);
				break;
			case DUEOUT:
				espJob.setDueout(statementParameters);
				break;
			case EARLYSUB:
				espJob.setEarlySubmission(statementParameters);
				break;
			case OPTIONS:
				espJob.setOptions(statementParameters);
				break;
			case PID:
				espJob.setPid(statementParameters);
				break;
			case PREREQ:
				espJob.setPrerequisite(extractPrereqJobDependencies(statementParameters));
				break;
			case RELDELAY:
				espJob.setRelDelay(Integer.parseInt(statementParameters));
				break;
			case STARTMODE:
				espJob.setStartMode(statementParameters);
				break;
			case TAG:
				espJob.setTag(statementParameters);
				break;
			case USER:
				espJob.setUser(statementParameters);
				break;
			default:
				// Must throw so prevent inpartial data.
				throw new TidalException("NOT yet supported statementType={" + statementType + "} for Job[" + espJob.getFullPath() + "]");

			}

		} catch (Exception e) {
			throw new TidalException("Error in Statement Type{" + statementType + "} for Job[" + espJob.getFullPath() + "]");

		}
	}

	private EspReleaseStatement extractReleaseJobDependencies(String statementParameters) {
		// Examples in ESP syntax are given below to know what to parse:
		// RELEASE ADD(EBLXNXS1_FT) COND(RC(0))
		// following two statements are located in cfg/bfusa/ESP/Prod/SWSAPTST file
		// RELEASE ADD(X_TEST_CYBERMATION_BFT030) COND(NOT RC(0))
		// RELEASE ADD(LIE.!ESPAPPL)
		// jobs/jobNames containing LIE/LIS (dummy jobs)
		EspReleaseStatement espReleaseStatement = new EspReleaseStatement();
		String joinedDependencies = statementParameters.substring(statementParameters.indexOf("(") + 1, statementParameters.indexOf(")"));
		List<String> jobNames = Arrays.asList(joinedDependencies.split(","));
		espReleaseStatement.setReleaseJobs(jobNames);

		String[] parameters = statementParameters.split(" ");
		if (parameters.length > 2) {
			String cond = parameters[2];
			String condRCValue = cond.substring(cond.indexOf("(") + 1, cond.indexOf(")") + 1);
			espReleaseStatement.setCondition(condRCValue);
		}

		return espReleaseStatement;
	}

	private EspPrereqStatement extractPrereqJobDependencies(String statementParameters) {
		String fullName = statementParameters.substring(statementParameters.indexOf("(") + 1, statementParameters.indexOf(")"));
		if (statementParameters.contains("(A)")) { // For example: PREREQ (ZSDSAR09_BANDAG_SALES_ORDERS_CD(A))
			return new EspPrereqStatement(fullName.substring(0, fullName.indexOf("(")), EspJobDependencyTerminationStatus.A);
		}
		return new EspPrereqStatement(fullName, EspJobDependencyTerminationStatus.N);
	}

	private EspAfterStatement extractAfterStatement(String statementParameters) {
		String jobName = statementParameters.substring(statementParameters.indexOf("(") + 1, statementParameters.lastIndexOf(")"));
		if (jobName.contains("(A)")) {
			return new EspAfterStatement(jobName.substring(0, jobName.indexOf("(")), EspJobDependencyTerminationStatus.A);
		}
		return new EspAfterStatement(jobName, EspJobDependencyTerminationStatus.N);
	}

	private EspNotWithStatement extractNotWithStatement(String statementParameters) {
		if (statementParameters.contains("(") && statementParameters.contains(".")) {
			return new EspNotWithStatement(statementParameters.substring(statementParameters.indexOf("(") + 1, statementParameters.indexOf(".")));
		}
		return new EspNotWithStatement(statementParameters);
	}

	private EspRunStatement extractEspRunStatement(String statementParameters) {
		EspRunStatement runStatement = new EspRunStatement();
		if (statementParameters.contains("REF")) {
			runStatement.setJobName(statementParameters.substring(statementParameters.indexOf("REF ") + 1));
			runStatement.setCriteria(null);
		} else {
			runStatement.setJobName(null);
			runStatement.setCriteria(statementParameters);
		}

		return runStatement;
	}

	private EspExitCodeStatement extractExitCodeStatement(String statementParameters) {
		// EXITCODE 2 SUCCESS
		// EXITCODE 3-9099 FAILURE
		String[] statementParametersParts = statementParameters.split(" ");
		String range = statementParametersParts[0];
		if (statementParametersParts[0].equalsIgnoreCase("FAILURE") || statementParametersParts[0].equalsIgnoreCase("fail")) {
			return new EspExitCodeStatement(range, EspExitCodeStatement.EspExitCodeStatementJobCompletionStatus.FAILURE);
		}
		return new EspExitCodeStatement(range, EspExitCodeStatement.EspExitCodeStatementJobCompletionStatus.SUCCESS);
	}

	@Override
	public void visit(EspGroupLIEData espApplEndJob, List<String> lines) {
		this.visitCommon(espApplEndJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {

			default:
				return false;
			}

		});
	}

	@Override
	public void visit(EspGroupLISData espApplEndJob, List<String> lines) {
		this.visitCommon(espApplEndJob, lines, (statementType, statementParameters) -> {
			switch (statementType) {

			default:
				return false;
			}

		});
	}

}
