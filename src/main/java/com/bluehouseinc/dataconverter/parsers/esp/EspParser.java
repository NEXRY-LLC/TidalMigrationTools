package com.bluehouseinc.dataconverter.parsers.esp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspDataModel;
import com.bluehouseinc.dataconverter.parsers.esp.model.MailListDataProcessor;
import com.bluehouseinc.dataconverter.parsers.esp.model.ScheduleEventDataProcessor;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitor;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitorImpl;
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
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspTaskProcessJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspTextMonJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspUnixJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspWindowsJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspZosJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchComment;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchEventElement;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchScheduleAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspJobResourceStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspRunStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EspParser extends AbstractParser<EspDataModel> {

	private final static String JOB_GROUP_PATTERN = "^APPL (.*)";
	private final static String JOB_GROUP_END_PATTERN = "^APPLEND (.*)";
	private final static String JOB_PATTERN = "(.*?)_?JOB\\s{1}[a-zA-Z0-9_.!]+";
	private final static String EMPTY_LINE_PATTERN = "^\\s*$";
	private final static String AGENT_MONITOR_PATTERN = "^AGENT_MONITOR (.*?)";
	private final static String FILE_TRIGGER_PATTERN = "^FILE_TRIGGER (.*?)";
	private final static String DATA_OBJECT_PATTERN = "^DATA_OBJECT (.*?)";
	private final static String SERVICE_MON_PATTERN = "^SERVICE_MON (.*?)";
	private final static String TEXT_MON_PATTERN = "^TEXT_MON (.*?)";

	private final static String COMMENT_PATTERN = "^\\/*(.*) *\\/$";
	private final static String TEMPLATE_START_PATTERN = "^TEMPLATE (.*)";
	private final static String TEMPLATE_END_PATTERN = "^ENDTEMPL";

	private final static String VARIABLE_PATTERN = "^(.*)=";
	private final static String IF_THEN_RUN_PATTERN = "IF\\s(.*?)\\sTHEN\\sRUN\\s(\\S+).*";
	private final static String ENDING_WITH_8_DIGITS_PATTERN = "\\s{1,}([0-9]{8})$";

	EspJobVisitor espJobVisitor;
	// private ScheduleEventDataProcessor scheduleDataProcessor;

	public EspParser(ConfigurationProvider cfgProvider) {
		super(new EspDataModel(cfgProvider, new ScheduleEventDataProcessor(), new MailListDataProcessor()));
		this.espJobVisitor = new EspJobVisitorImpl(getParserDataModel());
	}

	private DirectoryStream<Path> getFilteredDirectories(String pathToFiles) throws IOException {
		final List<String> filesToSkip = getParserDataModel().getConfigeProvider().getFilesToSkip() == null ? new ArrayList<>() : Arrays.asList(getParserDataModel().getConfigeProvider().getFilesToSkip().toLowerCase().split("\\s*,\\s*"));
		final List<String> filesToInclude = getParserDataModel().getConfigeProvider().getFilesToInclude() == null ? new ArrayList<>() : Arrays.asList(getParserDataModel().getConfigeProvider().getFilesToInclude().toLowerCase().split("\\s*,\\s*"));

		if (!Paths.get(pathToFiles).toFile().exists()) {
			throw new TidalException(pathToFiles);
		}

		return Files.newDirectoryStream(Paths.get(pathToFiles), path -> {
			String fileName = path.getFileName().toString().toLowerCase();

			if (Pattern.matches("@(.*?)", fileName)) {
				return false; // Not wanting this file monkey file
			}

			if (Pattern.matches("#(.*?)", fileName)) {
				return false; // Not wanting this file Hashtag file
			}

			List<String> localskip = filesToSkip;
			// If we have skip file data and exclude from future processing.
			boolean doskipfile = localskip.contains(fileName);

			if (doskipfile) {
				return false; // Its in our skip list to return false
			} else {
				final List<String> localinclude = filesToInclude;

				if (localinclude.isEmpty()) {
					return true; // If not data then always true.
				} else {

					boolean doincludefile = localinclude.contains(fileName);

					if (doincludefile) {
						return true;
					}
					return doincludefile;
				}
			}

		});
	}

	@Override
	protected String readLine(final BufferedReader reader) throws IOException {
		String line = EspFileReaderUtils.readLineTrimmed(reader);
		// Check if line needs to be sanitized. For example if line ends with ESP 8-digit generated line number preceded with empty (i.e. space) characters.
		// E.g. AGENT CYBB_BFP030 00060000
		if (line != null && RegexHelper.matchesRegexPattern(line, ENDING_WITH_8_DIGITS_PATTERN)) {
			line = line.replaceAll(ENDING_WITH_8_DIGITS_PATTERN, "");
		}
		return line;
	}

	private List<String> parseJobLines(BufferedReader reader) throws IOException {

		// return EspFileReaderUtils.parseJobLines(reader,"ENDJOB",'+');

		List<String> lines = new ArrayList<>();
		String line;
		while (!Objects.equals(line = readLine(reader), "ENDJOB")) {

			if (line == null) {
				break;
			}

			line = EspFileReaderUtils.readLineMerged(reader, line, '-');
			line = EspFileReaderUtils.readLineMerged(reader, line, '+');

			if (skipLine(line)) {
				continue;
			}

			if (line.toUpperCase().contains("!USER")) {
				line = line.replace("!USER", "USER");
				line = line.replace("!user", "user");
				line = line.replace("!", "");
			}

			// Just remove all single quotes vs trying to figure out where they all are.
			if (line.contains("'")) {
				line = line.replace("'", "");
			}

			lines.add(line.trim());
		}

		return lines;
	}

	private boolean skipLine(String line) {

		if (line.matches(EMPTY_LINE_PATTERN) || line.trim().startsWith("!USER")) {
			return true;
		}

		return false;
	}

	@Override
	public void parseFile() throws Exception {

		String eventdatafile = getParserDataModel().getConfigeProvider().getEspEventDataFile();

		this.getParserDataModel().getScheduleEventDataProcessor().doProcessScheduleData(eventdatafile);

		String maildata = getParserDataModel().getConfigeProvider().getEspMailListDataPath();

		this.getParserDataModel().getMailListDataProcessor().doProcessMailListData(maildata);

		String pathToFiles = this.getParserDataModel().getConfigeProvider().getEspDataPath();

		DirectoryStream<Path> directoryStream = getFilteredDirectories(pathToFiles);

		directoryStream.forEach(filePath -> {
			try {
				final BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()));

				String line = readLine(reader);

				// Skip over the first empty lines.
				if (EspFileReaderUtils.isEmptyLine(line)) {
					while (EspFileReaderUtils.isEmptyLine(line)) {
						line = readLine(reader);
					}
				}

				EspAbstractJob currentJob;

				String filename = filePath.getFileName().toString();

				if (filename.equals("IPDREU01") || filename.contains("MNAPPL")) {
					filename.chars();
				}

				if (!isValidApplicationLine(line, filename)) { // Checks does 1st line in file start with APPL keyword
					log.error("Not valid filename for file = {} missing APPL section", filePath.getFileName().toString());
					// log.info("Not valid filename for file = {} missing APPL section", filePath.getFileName().toString());
					return;
				}
				String jobGroupName = line.split(" ")[1];

				EspJobGroup currentJobGroup;
				BaseJobOrGroupObject existingEspJobGroup = this.getParserDataModel().getDataObjects().stream().filter(group -> group.getName().equals(jobGroupName)).findFirst().orElse(null);

				if (existingEspJobGroup == null) {
					currentJobGroup = new EspJobGroup(jobGroupName);
					this.getParserDataModel().addDataDuplicateLevelCheck(currentJobGroup);
				} else {
					currentJobGroup = (EspJobGroup) existingEspJobGroup;
				}

				while ((line = readLine(reader)) != null) {

					if (line.contains("SUNMAINT")) {
						line.toCharArray();
					}

					line = EspFileReaderUtils.readLineMerged(reader, line, '-');
					line = EspFileReaderUtils.readLineMerged(reader, line, '+');

					if (skipLine(line)) {
						continue;
					}

					if (isTemplateStartLine(line)) {
						readThroughTemplateLines(reader);
						continue;
					}

					if (isCommentLine(line)) {
						if (currentJobGroup.getNotesData() == null) {
							currentJobGroup.setNotesData(new ArrayList<>());
						}
						currentJobGroup.getNotesData().add(line);
						continue;
					}

					if (line.contains("!ESPAPPL")) {
						line = line.replace("!ESPAPPL", currentJobGroup.getName());
					}

					if (line.contains("!MNAPPL")) {
						line = line.replace("!MNAPPL", currentJobGroup.getName());
					}

					if (line.contains("!MNJOB")) {
						line = line.replace("!MNJOB", currentJobGroup.getName());

					}

					if (this.isJobLine(line)) {
						try {

							String[] jobdata = line.split(" ");

							String jobName = jobdata[1];
							String jobTypeString = jobdata[0];

							if (jobName.contains("!")) { // cant have jobs with variables in the name
								jobName = jobName.replace("!", "");
							}

							if (jobName.contains("DB0469A.MON")) {
								line.toLowerCase();
							}
							EspJobType jobType = extractJobType(jobTypeString, line);

							if (StringUtils.isBlank(jobName) || jobType == null) {
								log.error("Job Name is Blank or Job Type is null, ESP data related at line " + line);
								continue;
							}

							this.extractJob(reader, jobName, jobType, currentJobGroup, line);

						} catch (Exception e) {
							throw new TidalException(e);
						}
					}

					// TODO: SHould I put else statement around this because this line is in job or not.
					this.doProcessGroupData(currentJobGroup, line);

					// Assign event data if we have it for this group.
					SchEventElement edata = this.getParserDataModel().getScheduleEventDataProcessor().getElementByName(jobGroupName);

					this.doProcessGroupSchEventElementLogic(currentJobGroup, edata);

				}

			} catch (IOException ioException) {
				log.error(Arrays.toString(ioException.getStackTrace()));
				ioException.printStackTrace();
				throw new RuntimeException(ioException);
			} catch (Exception e) {
				log.error("RuntimeException exception occurred {}", Arrays.toString(e.getStackTrace()));
				throw new RuntimeException(e);
			}
		});

	}

	private void readThroughTemplateLines(BufferedReader reader) throws Exception {
		String line;
		while ((line = readLine(reader)) != null) {
			if (isTemplateEndLine(line)) {
				return;
			}
		}
	}

	private void extractJob(final BufferedReader reader, String jobName, EspJobType jobType, EspJobGroup parent, String rawdata) throws Exception {

		if (parent != null && jobName.equals("DMTIC_LOADBAT")) {
			if (parent.getName().equalsIgnoreCase("AOBOSS01")) {
				jobName.getBytes();
			}
		}

		List<String> lines = parseJobLines(reader);

		if (jobType == null) {
			throw new TidalException("Unknown jobType , type is null");
		}

		EspAbstractJob currentJob = null;

		switch (jobType) {
		case AIX:
			currentJob = new EspAixJob(jobName);
			break;
		case AS400:
			currentJob = new EspAs400Job(jobName);
			break;
		case BWPC:
			currentJob = new EspSAPBwpcJob(jobName);
			break;
		case DSTRIG:
			currentJob = new EspDStrigJob(jobName);
			break;
		case FILE_TRIGGER:
			currentJob = new EspFileTriggerJob(jobName);
			break;
		case FTP:
			currentJob = new EspFtpJob(jobName);
			break;
		case LINUX:
			currentJob = new EspLinuxJob(jobName);
			break;
		case SAP:
			currentJob = new EspSapJob(jobName);
			break;
		case SAPE:
			currentJob = new EspSapEventJob(jobName);
			break;
		case SCP:
			currentJob = new EspSecureCopyJob(jobName);
			break;
		case SERVICE_MON:
			currentJob = new EspServiceMonitorJob(jobName);
			break;
		case TEXT_MON:
			currentJob = new EspTextMonJob(jobName);
			break;
		case UNIX:
			currentJob = new EspUnixJob(jobName);
			break;
		case NT:
			currentJob = new EspWindowsJob(jobName);
			break;
		case ZOS:
			currentJob = new EspZosJob(jobName);
			break;
		case TASK:
			currentJob = new EspTaskProcessJob(jobName);
			break;
		case LINK_PROCESS:
			currentJob = new EspLinkProcessData(jobName);
			break;
		case APPLEND:
			currentJob = new EspAppEndData(jobName);
			break;
		case EXTERNAL:
			currentJob = new EspExternalApplicationData(jobName);
			break;
		case LIE:
			currentJob = new EspLIEData(jobName);
			break;
		case LIS:
			currentJob = new EspLISData(jobName);
			break;
		case DATA_OBJECT:
			currentJob = new EspDataObjectJob(jobName);
			break;
		case AGENT_MONITOR: // Do Nothing with these.
			currentJob = new EspAgentMonitorJob(jobName);
			break;
		default:
			throw new TidalException("Unknown Job Type[" + jobType.name() + "]");
		}

		// Go and do this visitor pattern stuff :) I rewrote this to work with my mind.
		currentJob.processData(espJobVisitor, lines);

		if (currentJob instanceof EspDataObjectJob) {
			if (parent.getVariables() == null) {
				parent.setVariables(new HashMap<>());
			}

			// extracting variables from DATA_OBJECT job and setting them to jobGroup level variables
			((EspDataObjectJob) currentJob).getVariables().forEach(setVarPair -> {
				parent.getVariables().put(setVarPair.getVariableName(), setVarPair.getVariableValue());
			});

			return;
		} else if (currentJob instanceof EspLIEData) {
			doProcessEspLIEData(parent, (EspLIEData) currentJob);
			return;
		} else if (currentJob instanceof EspLISData) {
			doProcessEspLISData(parent, (EspLISData) currentJob);
			return;
		} else if (currentJob instanceof EspLinkProcessData) {
			doProcessLinkedListData(parent, (EspLinkProcessData) currentJob);
			return;
		} else if (currentJob instanceof EspAppEndData) {
			parent.setApplicationEndData((EspAppEndData) currentJob);
			return;
		} else if (currentJob instanceof EspExternalApplicationData) {

			if (currentJob.getName().contains("DB0469A.MON")) {
				rawdata.toCharArray();
			}

			EspExternalApplicationData exj = (EspExternalApplicationData) currentJob;
			if (rawdata.contains("EXTERNAL APPLID(")) {
				String[] jobdata = rawdata.split(" ");
				if (jobdata.length >= 3) {
					String appid = jobdata[3];
					appid = appid.replace("APPLID(", "").replace(")", "");
					exj.setExternAppID(appid);

				}
			}

			if (rawdata.contains("LIE.") || rawdata.contains("LIS.")) {
				rawdata.chars(); // DO NOTHING. ITS a jobname we are looking for.
			} else {
				// use the object name for the dependency.
				exj.setExternJobName(jobName);
			}

			parent.getExternalApplicationDep().add(exj);
			return;
		} else {

			if (parent != null) {
				parent.addChild(currentJob);
				log.debug("Adding Child[" + currentJob.getFullPath() + "] to Parent");
			} else {
				getParserDataModel().addDataDuplicateLevelCheck(currentJob);
			}

		}

	}

	/**
	 *
	 * @param jobType
	 * @return
	 */
	private EspJobType extractJobType(String jobType, String rawdata) {

		if (rawdata.contains("TASK PROCESS")) {
			return EspJobType.TASK;
		}

		if (rawdata.contains("LINK PROCESS")) {
			return EspJobType.LINK_PROCESS;
		}

		if (rawdata.startsWith("APPLEND")) {
			return EspJobType.APPLEND;
		}

		if (rawdata.contains("EXTERNAL")) {
			return EspJobType.EXTERNAL;
		}

		if (rawdata.contains("JOB LIE.")) {
			return EspJobType.LIE;
		}

		if (rawdata.contains("JOB LIS.")) {
			return EspJobType.LIS;
		}

		try {
			if (jobType.contains("_JOB")) {
				jobType = jobType.split("_JOB")[0];

			} else {
				jobType = jobType.split(" ")[0];
			}

			if (jobType.equals("JOB")) {
				return EspJobType.ZOS;
			} else {
				return EspJobType.valueOf(jobType.toUpperCase());
			}
		} catch (Exception e) {
			log.error("EspJobType for jobType = '{}' does NOT exist (yet)!", jobType);
			return null;
		}
	}

	private boolean isValidApplicationLine(String line, String fileName) {
		return RegexHelper.matchesRegexPattern(line, JOB_GROUP_PATTERN) && (!line.equals("") && line.split(" ")[1].equals(fileName));
	}

	private boolean isJobLine(String line) {

		return RegexHelper.matchesRegexPattern(line, JOB_PATTERN) || RegexHelper.matchesRegexPattern(line, AGENT_MONITOR_PATTERN) || RegexHelper.matchesRegexPattern(line, FILE_TRIGGER_PATTERN)
				|| RegexHelper.matchesRegexPattern(line, DATA_OBJECT_PATTERN) || RegexHelper.matchesRegexPattern(line, SERVICE_MON_PATTERN) || RegexHelper.matchesRegexPattern(line, TEXT_MON_PATTERN)
				|| RegexHelper.matchesRegexPattern(line, JOB_GROUP_END_PATTERN);
	}

	private boolean isVariablesLine(String line) {
		return RegexHelper.matchesRegexPattern(line, VARIABLE_PATTERN);
	}

	private boolean isIfThenStatement(String line) {
		if (line == null) {
			return false;
		}
		return line.startsWith("IF");
		// Why this complicated. If the line starts with the word IF, its a If statement.
		// return RegexHelper.matchesRegexPattern(line, IF_THEN_STATEMENT_PATTERN);
	}

	private boolean isCommentLine(String line) {
		return RegexHelper.matchesRegexPattern(line, COMMENT_PATTERN) || line.startsWith("/*");
	}

	private boolean isTemplateStartLine(String line) {
		return line.matches(TEMPLATE_START_PATTERN);
	}

	private boolean isTemplateEndLine(String line) {
		return line.matches(TEMPLATE_END_PATTERN);
	}

	private void doProcessGroupData(EspJobGroup currentGroup, String line) {
		if (line.startsWith("NOTIFY")) {
			currentGroup.getNotifyList().add(line.split(" ", 2)[1]);
		}

		if (line.startsWith("TAG")) {
			if (currentGroup.getTags() == null) {
				currentGroup.setTags(new ArrayList<>());
			}
			currentGroup.getTags().add(line.split(" ", 2)[1]);
		}

		if (line.startsWith("RESOURCE")) {
			if (currentGroup.getStatementObject().getResources() == null) {
				currentGroup.getStatementObject().setResources(new ArrayList<>());
			}

			String jobResource = line.split(" ", 2)[1];
			currentGroup.getStatementObject().getResources().add(extractJobResource(jobResource));
		}

		String trimmedLine = line.trim();
		if (!isIfThenStatement(trimmedLine) && !trimmedLine.startsWith("/*") && isVariablesLine(trimmedLine)) {
			String[] splittedLine = line.split("=");
			String variableName = splittedLine[0].trim();
			String variableValue = splittedLine[1].trim();

			// Mapping variable where variable name AGENTNAME equals to empty/blank string into UnknownAgent.
			if ((variableName.equals("AGENTNAME")) && (variableValue.equals("") || variableValue.equals("''") || StringUtils.isBlank(variableValue))) {
				variableValue = "UnknownAgent";
			}

			if (currentGroup.getVariables() == null) {
				currentGroup.setVariables(new HashMap<>());
			}
			currentGroup.getVariables().put(variableName, variableValue);
		}
	}

	private EspJobResourceStatement extractJobResource(String jobResource) {
		String[] resourceParams = jobResource.substring(jobResource.indexOf("(") + 1, jobResource.indexOf(")")).split(",");
		int limit = Integer.parseInt(resourceParams[0]);
		String resourceName = resourceParams[1];
		return new EspJobResourceStatement(limit, resourceName);
	}

	@Override
	public IModelReport getModelReporter() {
		return new EspReporter();
	}

	/*
	 * As we progress we are discovering more we can do with this data. Do that work here.
	 */
	private void doProcessGroupSchEventElementLogic(EspJobGroup in, SchEventElement data) {
		// Per rules for now, we can only process the elements are are only schedule at time only.

		if (data != null) {
			in.setEventData(data);

			
			// Per ANkit , if the application (AKA group) in tidal contains a calendar 
			data.getComments().forEach(c -> in.setComment(in.getComment() + "\n" + c.getData()));

			if (data.isScheduleDataOnly()) {

				// Add our calendar as a run statement to a job.
				if (data.getCalendar() != null) {
					String eventCal = data.getCalendar().getData();
					EspRunStatement runstm = new EspRunStatement();
					runstm.setCriteria(eventCal);
					runstm.setJobName(in.getName());

					in.getStatementObject().getEspRunStatements().add(runstm);
				}

				// Attempt to process this and set data on the group object per requirement.
				List<SchScheduleAction> actions = ScheduleEventDataProcessor.getActionsByType(data, SchScheduleAction.class);

				List<String> times = new ArrayList<>();
				List<String> cals = new ArrayList<>();

				// We need to get all our times our of our action data and set them into a list
				// We also need to double check for the same on data, where we way say at this time no this day
				// If that on data is different, we need to account for it in our calendar data.
				// The general data is RUN statements so we will simply add to that data set vs making something new.

				actions.forEach(f -> {

					String t = f.getTime().replace(".", "").replace(":", ":").trim();

					String caldata = f.getCalendarData();

					if (caldata != null) {
						if (!cals.contains(caldata)) {
							cals.add(caldata); // Add our first element
						}
					}
					if (t.equals("0000")) {
						t.getClass();
					} else {

						if (NumberUtils.isParsable(t)) {
							times.add(t);
						} else {
							log.error("doProcessSchEventElementLogic Incorrect Time [" + t + "] for Job: " + in.getFullPath());
						}
					}
				});

				// Must be a single cal description !!!!!!
				if (cals.size() == 1) {
					if (!times.isEmpty()) {

						if (times.size() == 1) {
							in.setDelaySubmission(times.get(0).toString());

							EspRunStatement runstm = new EspRunStatement();
							runstm.setCriteria(cals.get(0));
							runstm.setJobName(in.getName());
							in.getStatementObject().getEspRunStatements().add(runstm);

						} else {
							// add all our start times.
							in.setRuntimes(times);
						}
					}
				}
				// else {
				//
				// cals.forEach(f -> {
				// // Just make this up as run statements
				// EspRunStatement runstm = new EspRunStatement();
				// runstm.setCriteria(f);
				// runstm.setJobName(in.getName());
				// in.getEspRunStatements().add(runstm);
				// });
				// }
			}
		}

	}

	private void doProcessLinkedListData(EspJobGroup in, EspLinkProcessData data) {
		in.getLinkProcessDataList().add(data);
		doProcessGenericData(in, data);
	}

	private void doProcessEspLIEData(EspJobGroup in, EspLIEData data) {
		doProcessGenericData(in, data);
	}

	private void doProcessEspLISData(EspJobGroup in, EspLISData data) {
		doProcessGenericData(in, data);
	}

	private void doProcessGenericData(EspJobGroup in, EspAbstractJob data) {
		if (data != null) {

			String desub = data.getDelaySubmission();
			String duout = data.getDueout();

			if (!StringUtils.isBlank(duout)) {
				if (StringUtils.isBlank(in.getDueout())) {
					in.setDueout(duout);
				} else {
					in.getName();
				}
			}

			if (!StringUtils.isBlank(desub)) {
				if (StringUtils.isBlank(in.getDelaySubmission())) {
					in.setDelaySubmission(desub);
				} else {
					in.getName();
				}

			}
			in.addEspStatementObject(data.getStatementObject());
		}
	}
}
