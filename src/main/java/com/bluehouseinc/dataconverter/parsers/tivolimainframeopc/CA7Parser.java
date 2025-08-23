package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc;

//CA7Parser.java - Main parser class
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.CA7JobNameParser.ParsedJobName;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.TivoliMainframeOPCDataModel;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl.CA7BaseJobObject;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl.CA7BaseJobObject.JobType;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl.CA7Dependency;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CA7Parser extends AbstractParser<TivoliMainframeOPCDataModel> {

	public CA7Parser(ConfigurationProvider parserDataModel) {
		super(new TivoliMainframeOPCDataModel(parserDataModel));

	}

	private static final Logger logger = Logger.getLogger(CA7Parser.class.getName());

	// Regular expressions for parsing
	private static final Pattern ADSTART_PATTERN = Pattern.compile("^\\s*ADSTART\\s+ACTION\\(ADD\\)");
	private static final Pattern ADOP_PATTERN = Pattern.compile("^\\s*ADOP\\s+ACTION\\(ADD\\)");
	private static final Pattern ADDEP_PATTERN = Pattern.compile("^\\s*ADDEP\\s+ACTION\\(ADD\\)");
	private static final Pattern ADSR_PATTERN = Pattern.compile("^\\s*ADSR\\s+ACTION\\(ADD\\)");
	private static final Pattern ADUSF_PATTERN = Pattern.compile("^\\s*ADUSF");
	private static final Pattern ADRUN_PATTERN = Pattern.compile("^\\s*ADRUN\\s+ACTION\\(ADD\\)");
	private static final Pattern ADRULE_PATTERN = Pattern.compile("^\\s*ADRULE");

	private static final Pattern PARAMETER_PATTERN = Pattern.compile("(\\w+)\\(([^)]*)\\)");
	private static final Pattern QUOTED_VALUE_PATTERN = Pattern.compile("'([^']*)'");

	// Parser state
	private enum ParseState {
		NONE, ADSTART, ADOP, ADDEP, ADSR, ADUSF, ADRUN, ADRULE
	}

	private ParseState currentState = ParseState.NONE;
	private CA7BaseJobObject currentApplication = null;
	private CA7BaseJobObject currentJob = null;
	private CA7Dependency currentDependency = null;
	private CA7Resource currentResource = null;
	private CA7UserField currentUserField = null;
	private CA7Schedule currentSchedule = null;
	private StringBuilder continuationBuffer = new StringBuilder();
	private List<CA7BaseJobObject> applications = new ArrayList<>();

	/**
	 * Parse a CA7 application definition file
	 */
	static int lineNumber = 0;
	String lastline = "";

	@Override
	public void parseFile() throws Exception {

		applications.clear();
		String datafile = getParserDataModel().getConfigeProvider().getCA7FilePath();
		log.info("Starting parse of file: " + datafile);
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(datafile))) {
			String line;
			int lineNumber = 0;

			while ((line = reader.readLine()) != null) {
				lineNumber++;
				parseLine(line.trim(), lineNumber);

			}
		}

		log.info(String.format("Parsing complete. %s", getStatistics()));

	}

	/**
	 * Parse a single line, handling continuation lines
	 */
	private void parseLine(String line, int lineNumber) {
		if (line.isEmpty() || line.startsWith("*") || line.equals(")")) {
			return; // Skip empty lines and comments
		}

		processCompleteLine(line, lineNumber);
		lastline = line;
	}

	/**
	 * Process a complete line (after handling continuations)
	 */
	private void processCompleteLine(String line, int lineNumber) {
		// Determine section type
		if (ADSTART_PATTERN.matcher(line).find()) {
			currentState = ParseState.ADSTART;
			currentApplication = new CA7BaseJobObject();
			parseADSTART(line, currentApplication);
		} else if (ADOP_PATTERN.matcher(line).find()) {
			currentState = ParseState.ADOP;
			if (currentJob != null && currentJob.getName() != null) {
				// are we a start or end job?
				if (currentJob.getName().trim().equalsIgnoreCase("start")) {
					currentApplication.setStartTime(currentJob.getStartTime());
					currentApplication.getDependencies().addAll(currentJob.getDependencies());
					currentApplication.getResources().addAll(currentJob.getResources());

				} else if (currentJob.getName().trim().equalsIgnoreCase("end")) {
					// Not sure if we need to do anything with this..

					currentJob.setJobType(JobType.END);
					currentJob.setName("MILESTONE-END");
					// Get only external dependencies.
					//List<CA7Dependency> external = new ArrayList<CA7Dependency>(currentJob.getDependencies().stream().filter(f -> Objects.nonNull(f.getPredecessorWorkstationId())).collect(Collectors.toList()));
					//currentJob.getDependencies().clear();
					//currentJob.getDependencies().addAll(external);
					
					if (!currentApplication.addChildTest(currentJob)) {
						currentJob.setName(currentJob.getName() + "-" + currentJob.getOperationNumber());
						currentApplication.addChild(currentJob);
					}
	
				} else {
					if (!currentApplication.addChildTest(currentJob)) {
						currentJob.setName(currentJob.getName() + "-" + currentJob.getOperationNumber());
						currentApplication.addChild(currentJob);
					}
				}
			}

			currentJob = new CA7BaseJobObject();
			parseADOP(line, currentJob);

		} else if (ADDEP_PATTERN.matcher(line).find()) {
			currentState = ParseState.ADDEP;
			currentDependency = new CA7Dependency();
			currentApplication.addDeppendency(currentDependency, currentJob);
			parseADDEP(line, currentDependency);
		} else if (ADSR_PATTERN.matcher(line).find()) {
			currentState = ParseState.ADSR;
			currentResource = new CA7Resource();
			currentApplication.addResource(currentResource, currentJob);
			parseADSR(line, currentResource);
		} else if (ADUSF_PATTERN.matcher(line).find()) {
			currentState = ParseState.ADUSF;
			currentUserField = new CA7UserField();
			currentJob.addUserField(currentUserField);
			parseADUSF(line, currentUserField);
		} else if (ADRUN_PATTERN.matcher(line).find()) {
			currentState = ParseState.ADRUN;
			currentSchedule = new CA7Schedule();
			currentApplication.addSchedule(currentSchedule);
			parseADRUN(line, currentSchedule);
		} else if (ADRULE_PATTERN.matcher(line).find()) {
			currentState = ParseState.ADRULE;
			parseADRULE(line, currentSchedule);
		} else {
			// Continuation of current section
			switch (currentState) {
			case NONE:
				// Do nothing - shouldn't reach here in normal parsing
				break;
			case ADSTART:
				parseADSTART(line, currentApplication);
				break;
			case ADOP:
				parseADOP(line, currentJob);
				break;
			case ADDEP:
				parseADDEP(line, currentDependency);
				break;
			case ADSR:
				parseADSR(line, currentResource);
				break;
			case ADUSF:
				parseADUSF(line, currentUserField);
				break;
			case ADRUN:
				parseADRUN(line, currentSchedule);
				break;
			case ADRULE:
				parseADRULE(line, currentSchedule);
				break;
			default:
				logger.warning("Unknown parse state: " + currentState);
				break;
			}
		}
	}

	/**
	 * Parse ADSTART section
	 */
	private void parseADSTART(String line, CA7BaseJobObject application) {
		if (application == null)
			return;

		Map<String, String> params = extractParameters(line);

		if (params.containsKey("ADID")) {
			String name = cleanValue(params.get("ADID"));
			if (name.startsWith("#")) {
				name = name.substring(1);
			}
			ParsedJobName pname = CA7JobNameParser.parseJobName(name);
			application.setNameParser(pname);

			application.setName(pname.getOriginalValue());

			if (!getParserDataModel().addDataObjectTest(application)) {
				String newname = application.getName() + "-" + application.getOperationNumber();
				application.setName(newname);
				application.invalidatePathCache();

				String errorMsg = String.format("Top-Level Validation Error: Renaming Object -> %s", application.getFullPath());

				log.error(errorMsg);

				getParserDataModel().addDataObject(application);
			}
		}
		if (params.containsKey("ADVALFROM")) {
			application.setValidFrom(parseDate(params.get("ADVALFROM")));
		}
		if (params.containsKey("CALENDAR")) {
			application.setCalendar(cleanValue(params.get("CALENDAR")));
		}
		if (params.containsKey("DESCR")) {
			application.setDescription(cleanValue(params.get("DESCR")));
		}
		if (params.containsKey("ADTYPE")) {
			application.setType(cleanValue(params.get("ADTYPE")));
		}
		if (params.containsKey("GROUP")) {
			application.setGroup(cleanValue(params.get("GROUP")));
		}
		if (params.containsKey("OWNER")) {
			application.setOwner(cleanValue(params.get("OWNER")));
		}
		if (params.containsKey("PRIORITY")) {
			application.setPriority(parseInt(params.get("PRIORITY")));
		}
		if (params.containsKey("ADSTAT")) {
			application.setStatus(cleanValue(params.get("ADSTAT")));
		}
	}

	/**
	 * Parse ADOP section
	 */
	private void parseADOP(String line, CA7BaseJobObject job) {
		if (job == null)
			return;

		Map<String, String> params = extractParameters(line);

		if (params.containsKey("OPNO")) {
			job.setOperationNumber(parseInt(params.get("OPNO")));
		}
		if (params.containsKey("JOBN")) {
			String jobname = params.get("JOBN");

			if (jobname == null || jobname.equals("null")) {
				jobname = "XXXXXXX";
			} else {
				jobname = cleanValue(jobname).trim();
			}

			if (jobname.contains("XP00MVBT")) {
				jobname.chars();
			}

			if (job.getFullPath().contains("TWS#TEST#DFB")) {
				jobname.chars();
			}

			// ParsedJobName pname = CA7JobNameParser.parseJobName(jobname);

			job.setName(jobname);
			// logger.warning(String.format("Error parsing line %d: %s - %s", lineNumber, line, e.getMessage()));
			// logger.warning("Processing Job: " + job.getName());
			job.setJobType(JobType.SCRIPT);
			job.setCommandLineData("NOTSET");

			addJobFileData(job);

			// getParserDataModel().addChildObject((BaseJobOrGroupObject) parent, (BaseJobOrGroupObject)job);

			// parent.addChild(job);
		}
		if (params.containsKey("WSID")) {
			job.setWorkstationId(cleanValue(params.get("WSID")));
		}
		if (params.containsKey("ADOPCATM")) {
			job.setCategory(cleanValue(params.get("ADOPCATM")));
		}
		if (params.containsKey("CONDRJOB")) {
			job.setConditionalJob(parseBoolean(params.get("CONDRJOB")));
		}
		if (params.containsKey("ADOPNOP")) {
			job.setNoOperation(parseBoolean(params.get("ADOPNOP")));
		}
		if (params.containsKey("ADOPMH")) {
			job.setManualHold(parseBoolean(params.get("ADOPMH")));
		}
		if (params.containsKey("DESCR")) {
			job.setDescription(cleanValue(params.get("DESCR")));
		}
		if (params.containsKey("DURATION")) {
			job.setDuration(parseInt(params.get("DURATION")));
		}
		if (params.containsKey("HIGHRC")) {
			job.setHighReturnCode(parseInt(params.get("HIGHRC")));
		}
		if (params.containsKey("AEC")) {
			job.setAutoExecuteComplete(parseBoolean(params.get("AEC")));
		}
		if (params.containsKey("STARTDAY")) {
			job.setStartDay(parseInt(params.get("STARTDAY")));
		}
		if (params.containsKey("STARTTIME")) {
			job.setStartTime(cleanValue(params.get("STARTTIME")));
		}
		if (params.containsKey("AJSUB")) {
			job.setAutoJobSubmit(parseBoolean(params.get("AJSUB")));
		}
		if (params.containsKey("AJR")) {
			job.setAutoJobRestart(parseBoolean(params.get("AJR")));
		}
		if (params.containsKey("TIME")) {
			job.setTimeRestricted(parseBoolean(params.get("TIME")));
		}
		if (params.containsKey("CLATE")) {
			job.setCancelLate(parseBoolean(params.get("CLATE")));
		}
		if (params.containsKey("PSNUM")) {
			job.setProcessingStation(parseInt(params.get("PSNUM")));
		}
		if (params.containsKey("ADOPPWTO")) {
			job.setOperatorWriteToOperator(parseBoolean(params.get("ADOPPWTO")));
		}
		if (params.containsKey("MONITOR")) {
			job.setMonitor(parseBoolean(params.get("MONITOR")));
		}
		if (params.containsKey("ADOPJOBCRT")) {
			job.setJobCreateRequired(parseBoolean(params.get("ADOPJOBCRT")));
		}
		if (params.containsKey("ADOPUSRSYS")) {
			job.setUserSystem(parseBoolean(params.get("ADOPUSRSYS")));
		}
		if (params.containsKey("ADOPEXPJCL")) {
			job.setExpandJCL(parseBoolean(params.get("ADOPEXPJCL")));
		}
		if (params.containsKey("CSCRIPT")) {
			job.setCScript(parseBoolean(params.get("CSCRIPT")));
		}
		if (params.containsKey("USEXTNAME")) {
			job.setUseExternalName(parseBoolean(params.get("USEXTNAME")));
		}
		if (params.containsKey("USEXTSE")) {
			job.setUseExternalSchedulingEngine(parseBoolean(params.get("USEXTSE")));
		}
		if (params.containsKey("USESAI")) {
			job.setUseSAI(parseBoolean(params.get("USESAI")));
		}
		if (params.containsKey("SMOOTHING")) {
			job.setSmoothing(parseInt(params.get("SMOOTHING")));
		}
		if (params.containsKey("LIMFDBK")) {
			job.setLimitFeedback(parseInt(params.get("LIMFDBK")));
		}
	}

	/**
	 * Parse ADDEP section
	 */
	private void parseADDEP(String line, CA7Dependency dep) {
		if (dep == null)
			return;

		Map<String, String> params = extractParameters(line);

		if (params.containsKey("PREADID")) {
			dep.setPredecessorApplicationId(cleanValue(params.get("PREADID")));
		}
		if (params.containsKey("PREWSID")) {
			dep.setPredecessorWorkstationId(cleanValue(params.get("PREWSID")));
		}
		if (params.containsKey("PREOPNO")) {
			dep.setPredecessorOperationNumber(parseInt(params.get("PREOPNO")));
		}
		if (params.containsKey("PRECSEL")) {
			dep.setConditionSelection(cleanValue(params.get("PRECSEL")));
		}
		if (params.containsKey("PREMAND")) {
			dep.setMandatory(parseBoolean(params.get("PREMAND")));
		}
	}

	/**
	 * Parse ADSR section
	 */
	private void parseADSR(String line, CA7Resource res) {
		if (res == null)
			return;

		Map<String, String> params = extractParameters(line);

		if (params.containsKey("RESOURCE")) {
			res.setResourceName(cleanValue(params.get("RESOURCE")));
		}
		if (params.containsKey("USAGE")) {
			res.setUsage(cleanValue(params.get("USAGE")));
		}
		if (params.containsKey("QUANTITY")) {
			res.setQuantity(parseInt(params.get("QUANTITY")));
		}
	}

	/**
	 * Parse ADUSF section
	 */
	private void parseADUSF(String line, CA7UserField usaf) {
		if (usaf == null)
			return;

		// easy fix.
		if (!line.endsWith(")")) {
			line += ")";
		}
		Map<String, String> params = extractParameters(line);

		if (params.containsKey("UFNAME")) {
			usaf.setFieldName(cleanValue(params.get("UFNAME")));
		}
		if (params.containsKey("UFVALUE")) {
			usaf.setFieldValue(cleanValue(params.get("UFVALUE")));
		}
	}

	/**
	 * Parse ADRUN section
	 */
	private void parseADRUN(String line, CA7Schedule schedule) {
		if (schedule == null)
			return;

		Map<String, String> params = extractParameters(line);

		if (params.containsKey("NAME")) {
			schedule.setName(cleanValue(params.get("NAME")));
		}
		if (params.containsKey("RULE")) {
			schedule.setRule(cleanValue(params.get("RULE")));
		}
		if (params.containsKey("VALFROM")) {
			schedule.setValidFrom(parseDate(params.get("VALFROM")));
		}
		if (params.containsKey("VALTO")) {
			schedule.setValidTo(parseDate(params.get("VALTO")));
		}
		if (params.containsKey("SHIFT")) {
			schedule.setShift(parseInt(params.get("SHIFT")));
		}
		if (params.containsKey("SHSIGN")) {
			schedule.setShiftSign(cleanValue(params.get("SHSIGN")));
		}
		if (params.containsKey("TYPE")) {
			schedule.setType(cleanValue(params.get("TYPE")));
		}
		if (params.containsKey("IATIME")) {
			schedule.setIaTime(cleanValue(params.get("IATIME")));
		}
		if (params.containsKey("DLDAY")) {
			schedule.setDeadlineDay(parseInt(params.get("DLDAY")));
		}
		if (params.containsKey("DLTIME")) {
			schedule.setDeadlineTime(cleanValue(params.get("DLTIME")));
		}
	}

	/**
	 * Parse ADRULE section
	 */
	private void parseADRULE(String line, CA7Schedule addrule) {
		if (addrule == null)
			return;

		// Extract rule description from ADRULE line
		String ruleDesc = line.replaceFirst("^\\s*ADRULE\\s*", "").trim();
		addrule.setRuleDescription(ruleDesc);
	}

	/**
	 * Extract parameters from a line using regex
	 */
	private Map<String, String> extractParameters(String line) {
		Map<String, String> params = new HashMap<>();
		Matcher matcher = PARAMETER_PATTERN.matcher(line);

		while (matcher.find()) {
			String key = matcher.group(1);
			String value = matcher.group(2);
			if (value != null) {
				value = value.trim();
			}
			params.put(key, value);
		}

		return params;
	}

	/**
	 * Clean parameter values (remove quotes, trim)
	 */
	private String cleanValue(String value) {
		if (value == null)
			return null;

		// Remove surrounding quotes
		Matcher quoteMatcher = QUOTED_VALUE_PATTERN.matcher(value);
		if (quoteMatcher.matches()) {
			value = quoteMatcher.group(1);
		}

		return value.trim();
	}

	/**
	 * Parse integer values
	 */
	private int parseInt(String value) {
		if (value == null || value.trim().isEmpty())
			return 0;
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			logger.warning("Failed to parse integer: " + value);
			return 0;
		}
	}

	/**
	 * Parse boolean values (Y/N)
	 */
	private boolean parseBoolean(String value) {
		if (value == null)
			return false;
		return "Y".equalsIgnoreCase(value.trim());
	}

	/**
	 * Parse date values (YYMMDD format)
	 */
	private LocalDate parseDate(String value) {
		if (value == null || value.trim().isEmpty())
			return null;

		try {
			String cleanValue = value.trim();
			if (cleanValue.length() == 6) {
				// Convert YYMMDD to LocalDate
				int year = Integer.parseInt(cleanValue.substring(0, 2));
				int month = Integer.parseInt(cleanValue.substring(2, 4));
				int day = Integer.parseInt(cleanValue.substring(4, 6));

				// Handle Y2K - assume years 00-50 are 2000-2050, 51-99 are 1951-1999
				if (year <= 50) {
					year += 2000;
				} else {
					year += 1900;
				}

				return LocalDate.of(year, month, day);
			}
		} catch (Exception e) {
			logger.warning("Failed to parse date: " + value);
		}

		return null;
	}

	private void addJobFileData(CA7BaseJobObject job) {

		String jobName = job.getName();

		if (jobName.equals("START")) {
			return;
		}

		try {

			String directory = "./cfg/bkbsks/dev/WSCA";

			if (jobName.startsWith("LBBL")) {
				directory = directory + "2";
			}

			Path filePath = Paths.get(directory, jobName); // adjust path as needed

			if (Files.exists(filePath)) {
				processJobFileWithContinuations(filePath, job);
				// logger.info("Read job file for: " + jobName);
				// return content;

			} else {
				// logger.fine("No job file found for: " + jobName);
				// return null;
				parseGenericMainFrameJobFile(job);
				// No file must be a MF job.

			}
		} catch (IOException e) {
			logger.warning("Failed to read job file for " + jobName + ": " + e.getMessage());
			// return null;
		}
	}

	private void processJobFileWithContinuations(Path filePath, CA7BaseJobObject job) throws IOException {
		List<String> processedLines = new ArrayList<>();
		StringBuilder currentLine = new StringBuilder();

		try (BufferedReader reader = Files.newBufferedReader(filePath)) {
			String line;

			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();

				// Skip empty lines and // comments
				if (trimmed.isEmpty() || trimmed.startsWith("//")) {
					continue;
				}

				if (trimmed.endsWith("\\")) {
					// Line continues - remove backslash and accumulate
					String content = trimmed.substring(0, trimmed.length() - 1).trim();
					currentLine.append(content);
					if (!content.isEmpty()) {
						currentLine.append(" "); // Add space between continued parts
					}
				} else {
					// Line ends - add accumulated content plus this line
					currentLine.append(trimmed);
					if (currentLine.length() > 0) {
						processedLines.add(currentLine.toString().trim());
					}
					currentLine.setLength(0);
				}
			}

			// Handle case where file ends with continuation
			if (currentLine.length() > 0) {
				processedLines.add(currentLine.toString().trim());
			}
		}

		// Process line data here..
		parseJobFileLines(processedLines, job);

		return; // String.join("\n", processedLines);
	}

	private void parseJobFileLines(List<String> lines, CA7BaseJobObject job) {
		if (lines.isEmpty())
			return;

		String firstLine = lines.get(0);

		if (firstLine.startsWith("TASKTYPE=") || firstLine.contains("jsr352javabatch")) {
			parseJavaJobFile(lines, job);
		} else {
			parseWindowsJobFile(lines, job);
		}
	}

	private void parseGenericMainFrameJobFile(CA7BaseJobObject job) {
		job.setJobType(JobType.CA7);
		job.setCommandLineData("//USER.CM30.JCLLIB(" + job.getName() + ")");
	}

	private void parseWindowsJobFile(List<String> lines, CA7BaseJobObject job) {
		job.setJobType(JobType.SCRIPT);
		StringBuilder scriptPath = new StringBuilder();

		for (String line : lines) {

			if (line.startsWith("JOBUSR(") && line.endsWith(")")) {
				String user = line.substring(7, line.length() - 1);
				job.setRuntimeUserName(user);
				continue;
			} else if (line.startsWith("JOBPWD(") && line.endsWith(")")) {
				String pwd = line.substring(7, line.length() - 1);
				job.setRuntimeUsePassword(pwd);
				continue;
			} else if (line.startsWith("INTRACTV(") && line.endsWith(")")) {
				String interactive = line.substring(9, line.length() - 1);
				job.setRuntimeInteractive(interactive);
				continue;
			} else if (line.startsWith("JOBCMD(") && line.endsWith(")")) {
				// String commandtype = line.substring(7, line.length() - 1);
				continue;
			}

		}

		if (!lines.isEmpty()) {
			scriptPath.append(lines.get(lines.size() - 1)).append("\n");
		}

		job.setCommandLineData(scriptPath.toString());

	}

	private void parseJavaJobFile(List<String> lines, CA7BaseJobObject job) {
		// job.setJobType(JobType.JAVA);
		// Extract task type
		// String firstLine = lines.get(0);
		// if (firstLine.contains("=")) {
		// String taskType = firstLine.split("=", 2)[1].trim();
		// // Store taskType in job object properties as needed
		// }
		//
		// // Combine all XML content (skip first line which is task type)
		// StringBuilder xmlContent = new StringBuilder();
		// for (int i = 1; i < lines.size(); i++) {
		// xmlContent.append(lines.get(i)).append("\n");
		// }

		// job.setCommandLineData(xmlContent.toString());
		// Parse XML content and extract what you need
		job.setJobType(JobType.CA7);
		job.setCommandLineData("//SYS3.IWS.V101.WSCA.JSRLIB(" + job.getName() + ")");
		// etc.
	}

	/**
	 * Get parsing statistics
	 */
	public String getStatistics() {

		int totalJobs = getParserDataModel().getDataObjects().stream().mapToInt(app -> app.getChildren().size()).sum();
		int totalDependencies = getParserDataModel().getDataObjects().stream().flatMap(app -> app.getChildren().stream()).mapToInt(job -> ((CA7BaseJobObject) job).getDependencies().size()).sum();
		// int totalResourcesApp = applications.stream().mapToInt(app -> app.getResources().size()).sum();
		int totalResources = getParserDataModel().getDataObjects().stream().flatMap(app -> app.getChildren().stream()).mapToInt(job -> ((CA7BaseJobObject) job).getResources().size()).sum();

		return String.format("CA7 Statistics: %d applications, %d jobs, %d dependencies, %d resources", getParserDataModel().getDataObjects().size(), totalJobs, totalDependencies, totalResources);
	}

	@Override
	public IModelReport getModelReporter() {
		return new TivoliMainframeOPCReporter();
	}
}
