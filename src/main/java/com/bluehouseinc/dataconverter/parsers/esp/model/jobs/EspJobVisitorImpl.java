package com.bluehouseinc.dataconverter.parsers.esp.model.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspDataModel;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspAppEndData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspDataObjectJob;
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
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspJobGroup;
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
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspAfterStatement;
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
public class EspJobVisitorImpl implements EspJobVisitor {
	EspDataModel model;
	EspJobVisitorHelper helper;
	private static final String IF_ELSE_ACTION_STATEMENT = "IF\\s(.*?)\\sTHEN\\s(\\S+)\\s(.*)";

	public EspJobVisitorImpl(EspDataModel model) {
		this.model = model;
		this.helper = new EspJobVisitorHelper(model);
	}

	private boolean containsIfLogic(String line) {
		if (line.trim().startsWith("if") || line.trim().startsWith("IF") || line.trim().startsWith("THEN") || line.trim().startsWith("ELSE")) {
			return true;
		}
		return false;
	}

	@Override
	public <E extends EspAbstractJob> void doProcess(final E job, final List<String> lines) {

		List<String> cleaned = new ArrayList<>();

		lines.forEach(f -> {
			if (containsIfLogic(f)) {

				if (RegexHelper.matchesRegexPattern(f, IF_ELSE_ACTION_STATEMENT)) {
					String action = RegexHelper.extractNthMatch(f, IF_ELSE_ACTION_STATEMENT, 1);
					String rundal = RegexHelper.extractNthMatch(f, IF_ELSE_ACTION_STATEMENT, 2);
					cleaned.add(action + " " + rundal);
				} else {
					job.setContainsIfLogic(true);
				}
			} else {
				cleaned.add(f);
			}
		});

		lines.clear();
		lines.addAll(cleaned);

		if (job.isContainsIfLogic()) {
			log.debug("EspJobVisitorImpl doProcess Job{} contains if logic", job.getFullPath());
		}

		if (job instanceof EspAixJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspAixJob) job));
		} else if (job instanceof EspLinuxJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspLinuxJob) job));
		} else if (job instanceof EspAgentMonitorJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspAgentMonitorJob) job));
		} else if (job instanceof EspAs400Job) {
			this.visitCommon(job, lines, this.helper.visitJob((EspAs400Job) job));
		} else if (job instanceof EspDataObjectJob) {
			this.visitCommon((EspDataObjectJob) job, lines);
		} else if (job instanceof EspDStrigJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspDStrigJob) job));
		} else if (job instanceof EspFileTriggerJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspFileTriggerJob) job));
		} else if (job instanceof EspFtpJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspFtpJob) job));
		} else if (job instanceof EspLinkProcessData) {
			this.visitCommon(job, lines, this.helper.visitJob((EspLinkProcessData) job));
		} else if (job instanceof EspSAPBwpcJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspSAPBwpcJob) job));
		} else if (job instanceof EspSapEventJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspSapEventJob) job));
		} else if (job instanceof EspSapJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspSapJob) job));
		} else if (job instanceof EspSecureCopyJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspSecureCopyJob) job));
		} else if (job instanceof EspServiceMonitorJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspServiceMonitorJob) job));
		} else if (job instanceof EspSftpJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspSftpJob) job));
		} else if (job instanceof EspTextMonJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspTextMonJob) job));
		} else if (job instanceof EspUnixJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspUnixJob) job));
		} else if (job instanceof EspWindowsJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspWindowsJob) job));
		} else if (job instanceof EspZosJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspZosJob) job));
		} else if (job instanceof EspAppEndData) {
			this.visitCommon(job, lines, this.helper.visitJob((EspAppEndData) job));
		} else if (job instanceof EspLIEData) {
			this.visitCommon(job, lines, this.helper.visitJob((EspLIEData) job));
		} else if (job instanceof EspLISData) {
			this.visitCommon(job, lines, this.helper.visitJob((EspLISData) job));
		} else if (job instanceof EspExternalApplicationData) {
			this.visitCommon(job, lines, this.helper.visitJob((EspExternalApplicationData) job));
		} else if (job instanceof EspTaskProcessJob) {
			this.visitCommon(job, lines, this.helper.visitJob((EspTaskProcessJob) job));
		} else {
			throw new TidalException("Unknown Job Type[" + job.getClass().getSimpleName() + "] Job[" + job.getFullPath() + "]");
		}
	}

	/**
	 * Special Method for data object, this is not a job
	 *
	 * @param espDataObjectJob
	 * @param lines
	 */
	public void visitCommon(EspDataObjectJob espDataObjectJob, List<String> lines) {
		List<EspSetVarStatement> variableList = new ArrayList<>();

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

		this.visitCommon(espDataObjectJob, lines, (statementType, statementParameters) -> {
			// No data, just return true to prevent downstream processing.
			return true;
		});

	}

	// Function2 is dynamic function that accepts 2 params and returns a boolean value
	// whether a second invoked switch hit a statement or not
	private void visitCommon(EspAbstractJob job, List<String> lines, Function2<String, String, Boolean> lambdaFunction) {

		for (String line : lines) {
			if (line.startsWith("/*")) {
				// job.getNoteData().add(line.substring(line.indexOf("/*") + 2));
				continue;
			}

			// Replace with our parent blindly
			if (line.contains("!ESPAPPL")) {
				if (job.getParent() != null) {
					line = line.replace("!ESPAPPL", job.getParent().getName());
				}
			}

			if (job.getName().contains("ZFI_BYCUSTOMER_PC")) {
				job.getName();
			}
			String[] statementParts;
			// line may contain following values, values are notated on right-side of <=> operator:
			// line<=>STMPTM= WOBDATA('PARMSET.!ESPAPPL','STMPTM')
			// line<=>ARGS --env=stg --service STATEMENT_XML_FILE_GENERATION--host ...
			if (line.contains("WOBDATA")) {
				// statementParts = line.split("=", 2); // This will result: statementParts[0]=ARGS --env
				log.debug("Job[{}] variable detection, skiping, not supported {}", job.getFullPath(), line);
				continue;
			} else {
				statementParts = line.split(" ", 2);
			}
			String statementType = statementParts[0].trim();
			String statementParameters;
			if (statementParts.length < 2) {
				continue;
			} else {
				statementParameters = statementParts[1].trim();
				if (statementParameters.contains("!")) {
					statementParameters = replaceInlineVariablesWithValues(statementType, statementParameters, (EspJobGroup) job.getParent());
				}
			}

			// Let our job match and set the data, if its not specific to our job, then set to our common.
			// This needs to throw if no match so we know what data we are missing.
			boolean filledinbyjob = lambdaFunction.apply(statementType, statementParameters);

			if (!filledinbyjob) {
				switch (statementType) {
				case "AFTER":
					job.getStatementObject().getEspAfterStatements().add(extractAfterStatement(statementParameters));
					continue;
				case "EXITCODE":
					job.getStatementObject().getExitCodeStatements().add(extractExitCodeStatement(statementParameters));
					continue;
				case "NOTIFY":
					job.getNotifyList().add(statementParameters);
					continue;
				case "NOTWITH":
					job.getStatementObject().getEspNotWithStatements().add(extractNotWithStatement(statementParameters));
					continue;
				case "NORUN":
					job.getStatementObject().getEspNoRunStatements().add(new EspNoRunStatement(statementParameters));
					continue;
				case "RELEASE":
					job.getStatementObject().getEspReleasedJobDependencies().add(extractReleaseJobDependencies(statementParameters));
					continue;
				case "RESOURCE":
					job.getStatementObject().getResources().add(extractJobResource(statementParameters));
					continue;
				case "run":
				case "RUN":
					job.getStatementObject().getEspRunStatements().add(extractEspRunStatement(statementParameters));
					continue;
				default:
					setCommon(job, statementType, statementParameters);
					continue;
				}

			}
		}

	}

	// ======================================= Helper methods =======================================

	private EspSetVarStatement extractVariable(String statementParameters) {
		// E.g. statementParameters<=>TMSTMP='!ESPSHH.:!ESPSMN.,!ESPSMM./!ESPSDD./!ESPSYY'
		String[] variableParStrings = statementParameters.split("=");
		if (variableParStrings.length > 1) {
			return new EspSetVarStatement(variableParStrings[0], variableParStrings[1]);
		} else {
			return new EspSetVarStatement(variableParStrings[0], "");
		}
	}

	private EspJobResourceStatement extractJobResource(String jobResource) {
		String[] resourceParams = jobResource.substring(jobResource.indexOf("(") + 1, jobResource.indexOf(")")).split(",");
		int limit = Integer.parseInt(resourceParams[0]);
		String resourceName = resourceParams[1];

		return new EspJobResourceStatement(limit, resourceName);
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
		statementParameters = statementParameters.replace("(", "").replace(")", "");
		// if (statementParameters.contains("(") && statementParameters.contains(".")) {
		// return new EspNotWithStatement(statementParameters.substring(statementParameters.indexOf("(") + 1, statementParameters.indexOf(".")));
		// }
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

	// private EspIfStatement extractIfStatement(String statementParameters) {
	// EspIfStatement runStatement = null;
	// // new EspIfStatement();
	// if (statementParameters.contains("IF")) {
	// String data = statementParameters.substring(statementParameters.indexOf("IF ") + 1);
	// runStatement = new EspIfStatement(data);
	// }
	// return runStatement;
	// }

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

	private String extractVariableValue(String criteria, EspJobGroup espJobGroup) {
		// First check does jobGroup contain any variable

		if (espJobGroup != null) {
			if (espJobGroup.getVariables() == null) {
				return null;
			}
			Set<String> variableNames = espJobGroup.getVariables().keySet();
			String key = variableNames.stream().filter(criteria::equals).findFirst().orElse(null);
			return key != null ? espJobGroup.getVariables().get(key) : null;
		}

		return null;
	}

	private String replaceInlineVariablesWithValues(String statementType, String statementParameters, EspJobGroup espJobGroup) {
		if (statementParameters.contains("WOBDATA")) {
			String varValue = extractVariableValue(statementType, espJobGroup);
			if (varValue != null) {
				return varValue;
			}

			return statementParameters;
		} else {
			String updatedStatement = statementParameters;
			List<String> statementParametersElements = Arrays.stream(statementParameters.split(" ")).filter(s -> !StringUtils.isBlank(s) && s.contains("!")).collect(Collectors.toList());

			for (String element : statementParametersElements) {
				String varKey = element.replace("!", "");
				String varValue = extractVariableValue(varKey, espJobGroup);
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

	private void setCommon(EspAbstractJob espJob, String statementType, String statementParameters) {

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
				espJob.getStatementObject().setPrerequisite(extractPrereqJobDependencies(statementParameters));
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
			case INVOKE:
				espJob.setInvokeObject(statementParameters);
				break;
			case ABANDON:
				espJob.setAbandonSubmission(statementParameters); // TODO: Extract out the time
				break;
			case LATESUB:
				espJob.setLateSubmission(statementParameters); // TODO: Extract out the time
				break;
			case JOBATTR:
				espJob.setJobAttributes(statementParameters);
				break;
			default:
				// Must throw so prevent inpartial data.
				throw new TidalException("NOT yet supported statementType={" + statementType + "} for Job[" + espJob.getFullPath() + "]");

			}

		} catch (Exception e) {
			throw new TidalException("Error in Statement Type{" + statementType + "} for Job[" + espJob.getClass().getSimpleName() + "][" + espJob.getFullPath() + "]");

		}
	}
}
