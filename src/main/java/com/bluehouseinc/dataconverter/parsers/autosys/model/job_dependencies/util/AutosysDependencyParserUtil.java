package com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.AutosysBaseDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types.AutosysExitCodeDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types.AutosysJobStatusDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types.AutosysVariableDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
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

	// @Getter(AccessLevel.PRIVATE)
	// @Setter(AccessLevel.PRIVATE)
	// private static HashMap<Integer, AutosysBaseDependency>
	// autosysBaseDependencyMap; // used for sanitizing expression for easier
	// processing of dependencies

	// TODO: Replace key value to be of String type (to hold jobName value) instead
	// of AutosysAbstractJob type
	// public static Map<AutosysAbstractJob, String> conditionExpressionMap; //
	// s(#1) | (e(#2) | d(#3))
	// Keeps correct order of dependency expressions in `condition` string and uses
	// current AUTOSYS job as reference

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

	public static HashMap<Integer, Map<Integer, AutosysBaseDependency>> getMapByJobType(AutosysAbstractJob job) {
		if (job.isGroup()) {
			return jobBoxSuccessMapData;
		}
		return jobDependencyMapData;
	}

	// TODO-IMPORTANT: Probably will need to create instance of TidalDataModel in
	// order to store values of CsvDependencyJob objects.

	AutosysDependencyParserUtil() {
	}

	/**
	 * Main entry point to AutosysDataModel Process and registered the job with its
	 * expresios into the
	 *
	 * @param job
	 * @param conditionValue
	 */
	public static String processJobExpresionData(AutosysAbstractJob job, String conditionValue) {

		if(StringUtils.isBlank(conditionValue)) {
			return conditionValue;
		}

		String cleanedExpresionData = parserCondition(job, conditionValue);
		// conditionExpressionMap.put(job, cleanedExpresionData);
		return cleanedExpresionData;
	}

	static String parserCondition(AutosysAbstractJob job, String fullExpression) {
		String sanitizedExpressionString = sanitizeExpression(job, fullExpression);

		if(StringUtils.isBlank(sanitizedExpressionString)) {
			return null;
		}

		log.debug("sanitizedExpressionString=[{}] for job[{}]", sanitizedExpressionString, job.getFullPath());

		return sanitizedExpressionString;
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

		if (job.getName().equals("EMB_FACE_0456_070.XmsRunFileIntake_CNY_NYCAPS_Retiree_MA")) {
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

		return finalExpressionFullyReplaced;
	}

	private static String sanitizeJobExpression(final String expression, AutosysAbstractJob job) {
		final StringBuffer sb = new StringBuffer();
		Map<Integer, AutosysBaseDependency> currentJobDependencyMap = new HashMap<>();

		Matcher matcher = DEPENDENCY_PATTERN.matcher(expression);

		if (job.getName().equals("EMB_FACE_0456_070.XmsRunFileIntake_CNY_NYCAPS_Retiree_MA")) {
			job.getName();
		}

		/*
		 * Following ExitCode and Variable Dependency's placeholder value with value of
		 * above declared `i` counter. E.g.: s(jobName1) & (e(#1) | t(jobName2)) | v(#1)
		 * ... into: s(#1) & (e(#2) | t(#3)) | v(#4)
		 */
		while (matcher.find()) {
			String raw = matcher.group();
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

			String jobname = job.getName();
			String targetname = autosysBaseDependency.getDependencyName();

			if (jobname.equals(targetname)) {
				log.error("Job Dependency would result in a loop, skipping target job[{}] dependency for job[{}]", targetname, job.getFullPath());
				matcher.replaceAll(raw);
				//matcher.appendReplacement(sb, "");
			} else {
				currentJobDependencyMap.put(autosysBaseDependency.getId(), autosysBaseDependency);
				// AutosysJobStatus foundAutosysJobStatus =
				// AutosysJobStatus.getStatus(depTypeString);
				// String replacement = foundAutosysJobStatus.getCode() + "(#" + i + ")";
				matcher.appendReplacement(sb, Integer.toString(autosysBaseDependency.getId()));

			}

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
			String raw = matcher.group();
			String rawExitCodeDependencyName = matcher.group(2);
			// extracting dependency name nested in parentheses pair...
			String exitCodeDependencyName = rawExitCodeDependencyName.substring(rawExitCodeDependencyName.indexOf("(") + 1, rawExitCodeDependencyName.indexOf(")"));

			String relationalOperatorString = matcher.group(3).trim();
			AutosysExitCodeDependency.AutosysExitCodeDependencyOperator relationalOperator = AutosysExitCodeDependency.AutosysExitCodeDependencyOperator.getOperator(relationalOperatorString);
			int value = Integer.parseInt(matcher.group(4).trim());

			AutosysExitCodeDependency exitCodeDependency = new AutosysExitCodeDependency(exitCodeDependencyName, relationalOperator, value);

			String jobname = job.getName();
			String targetname = exitCodeDependency.getDependencyName();

			if (jobname.equals(targetname)) {
				log.error("Job Dependency would result in a loop, skipping target job[{}] dependency for job[{}]", targetname, job.getFullPath());
				matcher.replaceAll(raw);
			} else {
				exitCodeDependencyValueMap.put(exitCodeDependency.getId(), exitCodeDependency);
				matcher.appendReplacement(sb, Integer.toString(exitCodeDependency.getId()));
			}
		}

		if (!exitCodeDependencyValueMap.isEmpty()) {
			getMapByJobType(job).put(job.getId(), exitCodeDependencyValueMap);
		}

		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Extracting dependency index from sanitized `condition` expression, e.g., for
	 * following expression: s(JOB_NAME1) | e(#1) & e(#2) ... returned values will
	 * be `1` and `2` respectively.
	 */
	static int extractDependencyIndex(String dependencyName) {
		return Integer.parseInt(dependencyName.substring(dependencyName.indexOf("#") + 1, dependencyName.indexOf(")")));
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

}
