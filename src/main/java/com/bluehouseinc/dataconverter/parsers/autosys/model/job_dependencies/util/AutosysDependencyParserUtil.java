package com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.AutosysDataModel;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.AutosysBaseDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types.AutosysExitCodeDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types.AutosysJobStatusDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types.AutosysVariableDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.expressions.ExpressionType;
import com.bluehouseinc.expressions.ExpressionUtil;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.model.dependency.job.DepLogic;
import com.bluehouseinc.tidal.api.model.dependency.job.DependentJobStatus;
import com.bluehouseinc.tidal.api.model.dependency.job.ExitCodeOperator;
import com.bluehouseinc.tidal.api.model.dependency.job.Operator;
import com.bluehouseinc.tidal.utils.DependencyBuilder;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

/*
	This class holds reference for current AUTOSYS job of its `condition` job property. It contains collections which are used to store references for
	adequate types (AutosysVariableDependency/AutosysExitCodeDependency/AutosysJobStatusDependency) of AUTOSYS dependencies. Also, this class processes
	String value of `condition` property by sanitizing variable,  exitCode and job status dependencies with placeholders and keeps those values in their
	adequate collections.
	input  = Autosys condition for current job,
	output1 = sanitized expression by replacing condition values with placeholders, along with their references collection

	Next steps (CONVERTER level) - for Single Job:
	1) Convert references collection to dependency collection (CvsDependencyJob objects) where key = i, where v(#1) s(#2), i = {1,2}
	2) Figure how to add them to the TIDAL Data Model (maybe use existing `addJobDependencyForJob` method or create method)

	Next steps (CONVERTER level) - for Multiple Jobs:
*/
@Log4j2
@Data
@Component
public class AutosysDependencyParserUtil {

	// Using `jobDependencyMap` to reference value for all AUTOSYS Dependency types.
	// Used later for look up of AUTOSYS jobs when performing conversion of
	// AUTOSYS jobs into TIDAL jobs
	private static final HashMap<Integer, Map<Integer, AutosysBaseDependency>> jobDependencyMapData = new HashMap<>();
	// This job is dependent on these other objects in order to run.

	private static final HashMap<Integer, Map<Integer, AutosysBaseDependency>> jobBoxSuccessMapData = new HashMap<>();
	// This group/box can only be success if these dependencies are satisfied.

	private final static String DEPENDENCY_STATUS_REGEX = "(s|success|n|notrunning|e|exitcode|v|value|f|failure|t|terminated|d|done)";

	// s(#1) & (e(#2) | d(#3))
	private final static String DEPENDENCY_REGEX = DEPENDENCY_STATUS_REGEX + "(\\((?>[^()]+|(\\?1))*\\))";
	private final static Pattern DEPENDENCY_PATTERN = Pattern.compile(DEPENDENCY_REGEX);

	private static HashMap<Integer, Map<Integer, AutosysBaseDependency>> getMapByJobType(AutosysAbstractJob job) {
		if (job.isGroup()) {
			return jobBoxSuccessMapData;
		}
		return jobDependencyMapData;
	}

	AutosysDependencyParserUtil() {
	}

	/**
	 * Main entry point to AutosysDataModel Process and registered the job with its
	 * expresios into the
	 *
	 * @param job
	 * @param conditionValue
	 */
	public static String doProcessExpresionData(AutosysAbstractJob job, String conditionValue) {

		if (StringUtils.isBlank(conditionValue)) {
			return null;
		}

		String cleanedExpresionData = sanitizeExpression(job, conditionValue);
		log.debug("sanitizedExpressionString=[{}] for job[{}]", cleanedExpresionData, job.getFullPath());

		if (StringUtils.isBlank(cleanedExpresionData)) {
			return null;
		}

		return cleanedExpresionData;
	}

	/*
	 * Following `sanitizeExpression` method converts value of `condition` job
	 * property in following steps:
	 *
	 * 1. s(jobName1) & (e(job_name2) <= 1542 | t(jobName3)) | v(variable_name) =
	 * "some_value" // original expression 2. s(jobName1) & (e(job_name2) <= 1542 |
	 * t(jobName2)) | v(#1) // result by using `sanitizeVariableExceptionals` method
	 * 3. s(jobName1) & (e(#1) | t(jobName2)) | v(#1) // result by using
	 * `sanitizeExitCodeExceptionals` method 4. s(#0) & (e(#1) | t(#2)) | v(#3) //
	 * result achieved after iterating through while-loop
	 *
	 * In last step 4. all index values are being set to keep correct order for
	 * AUTOSYS Dependency Types.
	 *
	 * Along the way each of these dependencies (s(#1), e(#2), etc.) is being stored
	 * into its adequate instance of AUTOSYS Dependency Type (i.e.
	 * AutosysBaseDependency ) by using `createAdequateDependencyType` method and
	 * stored in `currentJobDependencyMap` collection to keep track of real values
	 * (AutosysBaseDependency - value) by its index (key).
	 *
	 * Finally, at the end `currentJobDependencyMap` is set as value into
	 * `jobDependencyMap` collection as value and current AUTOSYS Job as key.
	 */
	private static String sanitizeExpression(AutosysAbstractJob job, String fullExpression) {

		if (job.getName().equals("MMM_QNXT_0180_05.ETS_IN_Script_for_Provider_Sync.FT00")) {
			job.getName();
		}

		String fullExpressionWithReplacedVariables = sanitizeVariableExceptionals(fullExpression, job);
		String fullExpressionWithReplacedVariablesAndExitCodeDeps = sanitizeExitCodeExceptionals(fullExpressionWithReplacedVariables, job);
		String finalExpressionFullyReplaced = sanitizeJobExpression(fullExpressionWithReplacedVariablesAndExitCodeDeps, job);

		if (fullExpression.contains("v(")) {
			// System.out.println("fullExpression=" + fullExpression);
			// System.out.println("finalExpressionFullyReplaced =" + finalExpressionFullyReplaced);
		}
		if (finalExpressionFullyReplaced.trim().equals("|") || finalExpressionFullyReplaced.trim().equals("&")) {
			// We had some type of issue, likely loop , AKA job A dep on Job A, not possible.
			return null;
		}

		if (finalExpressionFullyReplaced.trim().equals("(") || finalExpressionFullyReplaced.trim().equals(")")) {
			// We had some type of issue, likely loop , AKA job A dep on Job A, not possible.
			return null;
		}

		return finalExpressionFullyReplaced;
	}

	private static String sanitizeJobExpression(String expression, AutosysAbstractJob job) {
		final StringBuffer sb = new StringBuffer();
		Map<Integer, AutosysBaseDependency> currentJobDependencyMap = new HashMap<>();

		Matcher matcher = DEPENDENCY_PATTERN.matcher(expression);

		if (job.getName().equals("MMM_QNXT_0180_05.ETS_IN_Script_for_Provider_Sync.FT00")) {
			job.getName();
		}

		/*
		 * Following ExitCode and Variable Dependency's placeholder value with value of
		 * above declared `i` counter. E.g.: s(jobName1) & (e(#1) | t(jobName2)) | v(#1)
		 * ... into: s(#1) & (e(#2) | t(#3)) | v(#4)
		 */
		while (matcher.find()) {
			String depTypeString = matcher.group(1);
			/*
			 * `depJobNameRaw` variable needs to keep its parenthesis pair `()` so
			 * extractDependencyIndex method can properly extract its index value for
			 * further necessary processing. Therefore, its value is `(dependency_name)` ORR
			 * `(#some_number)`
			 */
			String depJobNameRaw = matcher.group(2);
			AutosysJobStatus jobStatus = AutosysJobStatus.getStatus(depTypeString);

			AutosysBaseDependency autosysBaseDependency = createAdequateDependencyType(jobStatus, depJobNameRaw, job);

			currentJobDependencyMap.put(autosysBaseDependency.getId(), autosysBaseDependency);
			// AutosysJobStatus foundAutosysJobStatus =
			// AutosysJobStatus.getStatus(depTypeString);
			// String replacement = foundAutosysJobStatus.getCode() + "(#" + i + ")";
			matcher.appendReplacement(sb, Integer.toString(autosysBaseDependency.getId()));

		}

		if (!currentJobDependencyMap.isEmpty()) {
			getMapByJobType(job).put(job.getId(), currentJobDependencyMap);
		}

		matcher.appendTail(sb);
		return sb.toString();

	}

	/**
	 * Replaces occurrences of variables dependencies with placeholder values. E.g.
	 * v(variable_dependency_name) = "some_value" => v(#1)*
	 *
	 * @param expression - whole String expression
	 * @return replaced String expression with placeholder value(s) for AUTOSYS
	 *         variable dependencies
	 */
	private static String sanitizeVariableExceptionals(final String expression, AutosysAbstractJob job) {
		final StringBuffer sb = new StringBuffer();
		final Pattern pattern = Pattern.compile(AutosysVariableDependency.VARIABLE_DEPENDENCY_REGEX);
		final Matcher matcher = pattern.matcher(expression);
		Map<Integer, AutosysBaseDependency> variableDependencyValueMap = new HashMap<>();
		// its implementation MUST be of LinkedList type to preserve the order because of placeholders

		while (matcher.find()) {

			String rawVariableName = matcher.group(2);
			// extracting dependency name nested in parentheses pair
			String variableName = rawVariableName.substring(rawVariableName.indexOf("(") + 1, rawVariableName.indexOf(")"));
			String value = matcher.group(3);

			AutosysVariableDependency autosysVariableDependency = new AutosysVariableDependency(variableName, value);
			variableDependencyValueMap.put(autosysVariableDependency.getId(), autosysVariableDependency);
			matcher.appendReplacement(sb, Integer.toString(autosysVariableDependency.getId()));

		}
		matcher.appendTail(sb);
		getMapByJobType(job).put(job.getId(), variableDependencyValueMap);
		return sb.toString();
	}

	/**
	 * Replaces occurrences of exit code dependencies with placeholder values. E.g.
	 * e(job_name) <= 1214 => e(#1)
	 *
	 * @param expression - whole String expression
	 * @return replaced String expression with placeholder value(s) for AUTOSYS exit
	 *         code dependencies
	 */
	private static String sanitizeExitCodeExceptionals(final String expression, AutosysAbstractJob job) {
		final StringBuffer sb = new StringBuffer();
		final Pattern pattern = Pattern.compile(AutosysExitCodeDependency.EXIT_CODE_DEPENDENCY_REGEX);
		final Matcher matcher = pattern.matcher(expression);

		// its implementation MUST be of LinkedList type to preserve the order because of placeholders
		Map<Integer, AutosysBaseDependency> exitCodeDependencyValueMap = new LinkedHashMap<>();

		while (matcher.find()) {
			String rawExitCodeDependencyName = matcher.group(2);
			// extracting dependency name nested in parentheses pair...
			String exitCodeDependencyName = rawExitCodeDependencyName.substring(rawExitCodeDependencyName.indexOf("(") + 1, rawExitCodeDependencyName.indexOf(")"));

			String relationalOperatorString = matcher.group(3).trim();
			AutosysExitCodeDependency.AutosysExitCodeDependencyOperator relationalOperator = AutosysExitCodeDependency.AutosysExitCodeDependencyOperator.getOperator(relationalOperatorString);
			String exitcodeString = matcher.group(4).trim();
			
			int value = Integer.parseInt(exitcodeString);

			AutosysExitCodeDependency exitCodeDependency = new AutosysExitCodeDependency(exitCodeDependencyName, relationalOperator, value);

			exitCodeDependencyValueMap.put(exitCodeDependency.getId(), exitCodeDependency);
			matcher.appendReplacement(sb, Integer.toString(exitCodeDependency.getId()));

		}

		if (!exitCodeDependencyValueMap.isEmpty()) {
			getMapByJobType(job).put(job.getId(), exitCodeDependencyValueMap);
		}

		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Creating adequate AUTOSYS Dependency object type.
	 *
	 * @param autosysJobStatus job status
	 * @param dependencyName   job/variable name
	 * @return instance of <code>AutosysBaseDependency</code> type
	 */
	static AutosysBaseDependency createAdequateDependencyType(AutosysJobStatus autosysJobStatus, String dependencyName, AutosysAbstractJob job) {
		AutosysBaseDependency autosysBaseDependency;
		String sanitizedDependencyName = dependencyName.substring(dependencyName.indexOf("(") + 1, dependencyName.indexOf(")"));
		switch (autosysJobStatus) {
		case EXITCODE:
			// autosysBaseDependency =
			// exitCodeDependencyValueMap.get(extractDependencyIndex(dependencyName));
			throw new TidalException("Brian Breaks Code Dumb American");
		// break;
		case VALUE:
			// autosysBaseDependency =
			// variableDependencyValueMap.get(extractDependencyIndex(dependencyName));
			throw new TidalException("Brian Breaks Code Dumb American");
		// break;
		case DONE:
			autosysBaseDependency = new AutosysJobStatusDependency(sanitizedDependencyName, AutosysJobStatus.DONE);
			break;
		case FAILURE:
			autosysBaseDependency = new AutosysJobStatusDependency(sanitizedDependencyName, AutosysJobStatus.FAILURE);
			break;
		case SUCCESS:
			autosysBaseDependency = new AutosysJobStatusDependency(sanitizedDependencyName, AutosysJobStatus.SUCCESS);
			break;
		case NOTRUNNING:
			autosysBaseDependency = new AutosysJobStatusDependency(sanitizedDependencyName, AutosysJobStatus.NOTRUNNING);
			break;
		case TERMINATED:
			autosysBaseDependency = new AutosysJobStatusDependency(sanitizedDependencyName, AutosysJobStatus.TERMINATED);
			break;
		default:
			throw new RuntimeException("Error, unknown AUTOSYS Dependency type!");
		}
		return autosysBaseDependency;
	}

	// From here or the other places we deal with dependency we should be able to detect a FileTrigger type
	// add use that data to build a new file dependency to my targetJob.
	public static void doProcessJob(final AutosysAbstractJob sourceJob, BaseCsvJobObject targetJob, TidalDataModel model, AutosysDataModel autosys) {

		if (sourceJob.getName().equals("ABS_SMT_6110_010.Master_smt_m_post_batch_stat_hist_load.FW01")) {
			targetJob.getName();
		}

		// final String expresiondata = sourceJob.getCondition();

		if (StringUtils.isBlank(sourceJob.getCondition())) {
			return; // Nothing to process we dont have any expresions to process, aka no dependency data.
		}

		targetJob.setCompoundDependency(sourceJob.getCondition());

		/*
		 * `autosysConditionExpressionMap` holds reference to parsed
		 * compound-dependencies expression as String which is referenced by
		 * AutosysAbstractJob. Holds String value: s(#0) & (e(#1) | t(#2)) | v(#3)
		 * ...which was parsed from following original expression (AUTOSYS `condition`
		 * job property): s(jobName1) & (e(job_name2) <= 1542 | t(jobName3)) |
		 * v(variable_name) = "some_value"
		 */
		HashMap<Integer, Map<Integer, AutosysBaseDependency>> localMap = AutosysDependencyParserUtil.getMapByJobType(sourceJob);

		// This is the current job list of dependencies. From Local Map for reading, we
		// MUST have data if we have expression data.
		Map<Integer, AutosysBaseDependency> mapOfJobDep = localMap.get(sourceJob.getId());

		if (targetJob.getName().equals("EMB_FACE")) {
			targetJob.getName();
		}

		if (mapOfJobDep != null) {

			if (!mapOfJobDep.isEmpty()) {
				// log.info(sourceJob.getFullPath());
				// targetJob.setCompoundDependency(expresiondata); // Set me to this so we can replace with real data later.

				// We have depenencies to work with.
				// Contains Autosys dependency ID's and we need to lookup the csv job, create
				// new dependency object and replace iD's

				// We better have an expression to work with containing ALL the ID's of our map
				// of deps.

				mapOfJobDep.entrySet().forEach(f -> {

					AutosysBaseDependency autoSysBaseDepObject = f.getValue();

					String sourcejobname = sourceJob.getName();
					String dependsOnThisJobObjectName = autoSysBaseDepObject.getDependencyName();

					if (sourcejobname.equalsIgnoreCase(dependsOnThisJobObjectName)) {
						log.error("doProcessJob -> Dependency is a loop, source[{}] and target[{}] are the same object.", targetJob.getName(), dependsOnThisJobObjectName);
						String tempdata = targetJob.getCompoundDependency();

						tempdata = tempdata.replace(Integer.toString(autoSysBaseDepObject.getId()), "").trim();

						targetJob.setCompoundDependency(tempdata);

					} else {

						// String expressiondata = targetJob.getCompoundDependency();
						// log.debug("[doProcessJobDeps] looking for our dependent job=[{}] ", dependsOnThisJobObjectName);
						BaseJobOrGroupObject dependsOnThisAutoSysJob = autosys.getBaseObjectByName(dependsOnThisJobObjectName);

						if (dependsOnThisAutoSysJob == null) {
							log.info("[doProcessJobDeps] missing job in our AutoSys Data, looking for a job with this name[" + dependsOnThisJobObjectName + "]");

							return;
							// throw new TidalException("[doProcessJobDeps] missing job in our AutoSys Data, looking for a job with this name["+dependsOnThisJobObjectName+"]");
						} else {
							BaseCsvJobObject dependsOnThisRealCsvJob = model.findFirstJobByFullPath(dependsOnThisAutoSysJob.getFullPath());

							if (dependsOnThisRealCsvJob == null) {
								// Major issues, we should always find a job matching by name for Autosys.
								log.info("[doProcessJobDeps] missing dependenct job[" + dependsOnThisAutoSysJob.getFullPath() + "] in TIDAL");
								throw new TidalException("[doProcessJobDeps] missing dependenct job[" + dependsOnThisAutoSysJob.getFullPath() + "] in TIDAL");

							} else {

								if (autoSysBaseDepObject instanceof AutosysJobStatusDependency) {

									AutosysJobStatusDependency jdep = (AutosysJobStatusDependency) autoSysBaseDepObject;

									DependentJobStatus usestatus = null;
									Operator oper = Operator.EQUAL;

									if (jdep.getStatus() == AutosysJobStatus.SUCCESS) {
										usestatus = DependentJobStatus.COMPLETED_NORMAL;
									} else if (jdep.getStatus() == AutosysJobStatus.DONE) {
										usestatus = DependentJobStatus.COMPLETED;
									} else if (jdep.getStatus() == AutosysJobStatus.FAILURE) {
										usestatus = DependentJobStatus.COMPLETED_ABNORMAL;
									} else if (jdep.getStatus() == AutosysJobStatus.TERMINATED) {
										usestatus = DependentJobStatus.TERMINATED;
									} else if (jdep.getStatus() == AutosysJobStatus.NOTRUNNING) {
										usestatus = DependentJobStatus.RUNNING;
										oper = Operator.NOT_EQUAL;
									}

									// jdep.getStatus().DONE ;
									CvsDependencyJob csvjobdep = model.addJobDependencyForJob(targetJob, dependsOnThisRealCsvJob, DepLogic.MATCH, oper, usestatus, null);

									// CvsDependencyJob csvjobdep = getTidal().addJobDependencyForJobCompletedNormal(targetJob,dependsOnThisRealCsvJob,null);

									String tempdata = targetJob.getCompoundDependency();

									tempdata = tempdata.replace(Integer.toString(autoSysBaseDepObject.getId()), Integer.toString(csvjobdep.getId())).trim();

									targetJob.setCompoundDependency(tempdata);

								} else if (autoSysBaseDepObject instanceof AutosysVariableDependency) {

									AutosysVariableDependency vdep = (AutosysVariableDependency) autoSysBaseDepObject;

									String variableName = vdep.getDependencyName(); // Think this is a variable

									// TODO: Code not completed for variables.

								} else if (autoSysBaseDepObject instanceof AutosysExitCodeDependency) {

									AutosysExitCodeDependency edep = (AutosysExitCodeDependency) autoSysBaseDepObject;

									CvsDependencyJob csvjobdep = model.addJobDependencyForJobCompletedNormal(targetJob, dependsOnThisRealCsvJob, null);

									csvjobdep.setExitCodeOperator(ExitCodeOperator.EQ);
									csvjobdep.setExitcodeStart(edep.getExitCode());
									csvjobdep.setExitcodeEnd(edep.getExitCode());

									String tempdata = targetJob.getCompoundDependency();

									tempdata = tempdata.replace(Integer.toString(autoSysBaseDepObject.getId()), Integer.toString(csvjobdep.getId())).trim();

									targetJob.setCompoundDependency(tempdata);
								}

							}

						}
					}
				});

				String data = targetJob.getCompoundDependency();
				// Setup and register the compound deps if needed.
				if (!StringUtils.isBlank(data)) {
	
					registerCompoundDep(targetJob, model);
				}

			} else {
				if (!StringUtils.isBlank(sourceJob.getCondition())) {
					// We have an issue.. We do not have any data matching but do have an expression of data.
					// TODO: Throw exception here.
					log.error("[doProcessJobDeps] job{} missing map data but has this condition={}", sourceJob.getFullPath(), sourceJob.getCondition());
				}
			}
		} else {
			// This job has no dependency?
			// TODO: Throw exception here.
			log.error("[doProcessJobDeps] missing job in our map but has this condition={}", sourceJob.getCondition());
		}

	}

	private static String cleanupExpresion(String tempdata) {
		tempdata = tempdata.trim();

			if (tempdata.startsWith("|") | tempdata.startsWith("&")) {
				tempdata = tempdata.substring(1, tempdata.length()).trim();
			}

			if (tempdata.endsWith("|") | tempdata.endsWith("&")) {
				tempdata = tempdata.substring(0, tempdata.length() - 1).trim();
			}

		return tempdata;
	}

	private static void registerCompoundDep(BaseCsvJobObject targetJob, TidalDataModel model) {

		if (targetJob.getName().equals("EMB_FACE_7290_030.FHGInbound_837_ENC")) {
			targetJob.getName();
		}

		String expresiondata = targetJob.getCompoundDependency();

		if (StringUtils.isBlank(expresiondata)) {
			return; // Nothing to process we dont have any expresions to process.
		}

		if (expresiondata.trim().equals("|") || expresiondata.trim().equals("&")) {
			targetJob.setCompoundDependency(null);
			return;
		}
		
		if (expresiondata.contains("&") | expresiondata.contains("|")) {
			List<String> deplistdata = new ArrayList<>();

			// ugg ugly but lets try to capture all styles of data.
			expresiondata = expresiondata.replace("& &", "&").replace("&  &", "&");
			expresiondata = expresiondata.replace("& )", ")");
			expresiondata = cleanupExpresion(expresiondata);
			
			if (StringUtils.isBlank(expresiondata)) {
				return; 
			}
			
			try {

				deplistdata = targetJob.getCompoundDependencyBuilder().build(expresiondata).getExpressionList();

			} catch (RuntimeException e) {
				log.info("registerCompoundDep ERROR in Job{} unable to process expression{}", targetJob.getFullPath(), expresiondata);
				log.error("registerCompoundDep ERROR in Job{} unable to process expression{}", targetJob.getFullPath(), expresiondata);

			}

			int datalen = deplistdata.size();

			if (datalen == 1 || deplistdata.isEmpty()) {
				// Single depedency in the expression data.
				targetJob.setDependencyOrlogic(false);
			} else {

				boolean isAllAnds = ExpressionUtil.isExpressionOfType(ExpressionType.and, targetJob.getCompoundDependencyBuilder().getExpression());
				boolean isAllOrs = ExpressionUtil.isExpressionOfType(ExpressionType.or, targetJob.getCompoundDependencyBuilder().getExpression());

				if (isAllAnds) { // single sized elements, no need to go any deeper
					// TODO: Look at refactoring this code but for now it works.
					// if the deps are all ands, then ignore it and just set the Or logic to false
					targetJob.setDependencyOrlogic(false);
				} else if (isAllOrs) {
					// if all or's then just set the Or logic to true.
					targetJob.setDependencyOrlogic(true);
				} else {
					// BUT IF WE ARE Setting up a compound dep, then lets process correctly
					// mainjob.setCompoundDependency(depbuilder.toString());

					// If we set this we ignore the two above as we are a complex type.
					targetJob.setCompoundDependency(targetJob.getCompoundDependencyBuilder().toString());
					// Register the job has having compound dependencies. OR addJob to model AFTER you setup your compound dependencies.
					model.registerCompoundDependencyJob(targetJob);
				}
			}
		} else {
			targetJob.setDependencyOrlogic(false);
		}
	}
}
