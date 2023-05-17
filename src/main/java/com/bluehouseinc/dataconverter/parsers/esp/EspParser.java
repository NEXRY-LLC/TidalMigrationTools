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
import com.bluehouseinc.dataconverter.parsers.esp.model.ScheduleEventDataProcessor;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspAppEndData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspDataObjectJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitor;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitorImpl;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspLinkProcessData;
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
	private final static String IF_THEN_STATEMENT_PATTERN = "^IF (.*) (THEN|EQ) (.*)";
	private final static String ENDING_WITH_8_DIGITS_PATTERN = "\\s{1,}([0-9]{8})$";

	EspJobVisitor espJobVisitor;
	//private ScheduleEventDataProcessor scheduleDataProcessor;

	public EspParser(ConfigurationProvider cfgProvider) {
		super(new EspDataModel(cfgProvider,new ScheduleEventDataProcessor()));
		this.espJobVisitor = new EspJobVisitorImpl(getParserDataModel());
	}

	private DirectoryStream<Path> getFilteredDirectories(String pathToFiles) throws IOException {
		return Files.newDirectoryStream(Paths.get(pathToFiles), path -> {
			String fileName = path.getFileName().toString();

			Set<String> filesToSkip = Arrays.asList(getParserDataModel().getConfigeProvider().getFilesToSkip().split(",")).stream().collect(Collectors.toCollection(HashSet::new));
			// Set<String> filesToSkip = Stream.of("CF01", "CF02", "CF03", "CF04", "CF05").collect(Collectors.toCollection(HashSet::new));
			boolean containsFileToSkip = filesToSkip.contains(fileName);
			boolean containsMonkey = Pattern.matches("@(.*?)", fileName);
			boolean containsHashtag = Pattern.matches("#(.*?)", fileName);
			return !containsMonkey && !containsHashtag && !containsFileToSkip;
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
						if (currentJobGroup.getNoteData() == null) {
							currentJobGroup.setNoteData(new ArrayList<>());
						}
						currentJobGroup.getNoteData().add(line);
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


							if (line.contains("FSBP_ZFIGLI19")) {
								line.toCharArray();
							}

							if (jobName.contains("!")) { // cant have jobs with variables in the name
								jobName = jobName.replace("!", "");
							}

							EspJobType jobType = extractJobType(jobTypeString);

							if (StringUtils.isBlank(jobName) || jobType == null) {
								log.error("Job Name is Blank or Job Type is null, ESP data related at line " + line);
								continue;
							}

							if (jobName.contains("USCA1OFF")) {
								jobName.getBytes();
							}

							currentJob = this.extractJob(reader, jobName, jobType, currentJobGroup);

							if (currentJob instanceof EspDataObjectJob) {
								if (currentJobGroup.getVariables() == null) {
									currentJobGroup.setVariables(new HashMap<>());
								}

								// extracting variables from DATA_OBJECT job and setting them to jobGroup level variables
								((EspDataObjectJob) currentJob).getVariables().forEach(setVarPair -> {
									currentJobGroup.getVariables().put(setVarPair.getVariableName(), setVarPair.getVariableValue());
								});

								continue;
							}

							if (currentJob instanceof EspLinkProcessData) {
								doProcessLinkedListData(currentJobGroup, (EspLinkProcessData) currentJob);
								continue;
							}

							if (currentJob instanceof EspAppEndData) {
								currentJobGroup.setApplicationEndData((EspAppEndData) currentJob);
								continue;
							}

							if (line.contains("EXTERNAL APPLID(")) {
								if (jobdata.length >= 3) {
									String appid = jobdata[3];
									appid = appid.replace("APPLID(", "").replace(")", "");
									currentJob.setExternalAppID(appid);
								}
							}

						} catch (Exception e) {
							throw new TidalException(e);
							// log.error("FATAL EXCEPTION! More info:\n {}", Arrays.toString(e.getStackTrace()));
						}
					}

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

	private EspAbstractJob extractJob(final BufferedReader reader, String jobName, EspJobType jobType, EspJobGroup parent) throws Exception {

		if (jobName.contains("PAYMENT")) {
			jobName.getBytes();
		}

		List<String> lines = parseJobLines(reader);

		if (jobType == null) {
			throw new TidalException("Unknown jobType , type is null");
		}

		EspAbstractJob job = null;

		switch (jobType) {
		case AGENT_MONITOR:
			job = new EspAgentMonitorJob(jobName);
			break;
		case AIX:
			job = new EspAixJob(jobName);
			break;
		case AS400:
			job = new EspAs400Job(jobName);
			break;
		case BWPC:
			job = new EspSAPBwpcJob(jobName);
			break;
		case DATA_OBJECT:
			job = new EspDataObjectJob(jobName);
			break;
		case DSTRIG:
			job = new EspDStrigJob(jobName);
			break;
		case FILE_TRIGGER:
			job = new EspFileTriggerJob(jobName);
			break;
		case FTP:
			job = new EspFtpJob(jobName);
			break;
		case LINUX:
			job = new EspLinuxJob(jobName);
			break;
		case SAP:
			job = new EspSapJob(jobName);
			break;
		case SAPE:
			job = new EspSapEventJob(jobName);
			break;
		case SCP:
			job = new EspSecureCopyJob(jobName);
			break;
		case SERVICE_MON:
			job = new EspServiceMonitorJob(jobName);
			break;
		case TEXT_MON:
			job = new EspTextMonJob(jobName);
			break;
		case UNIX:
			job = new EspUnixJob(jobName);
			break;
		case NT:
			job = new EspWindowsJob(jobName);
			break;
		case LINK_PROCESS:
			job = new EspLinkProcessData(jobName);
			break;
		case APPLEND:
			job = new EspAppEndData(jobName);
			break;
		case ZOS:
			job = new EspZosJob(jobName);
			break;
		default:
			throw new TidalException("Unknown Job Type[" + jobType.name() + "]");
		}

		// Add our job to our parent early
		if (parent != null) {
			parent.addChild(job);
		}

		// Go and do this visitor pattern stuff :) I rewrote this to work with my mind.
		job.processData(espJobVisitor, lines);

		return job;

	}

	/**
	 *
	 * @param line
	 * @return
	 */
	private EspJobType extractJobType(String line) {

		if (line.endsWith("LINK PROCESS")) {
			return EspJobType.LINK_PROCESS;
		}

		if (line.startsWith("APPLEND")) {
			return EspJobType.APPLEND;
		}
		String jobType = "";

		try {
			if (line.contains("_JOB")) {
				jobType = line.split("_JOB")[0];

			} else {
				jobType = line.split(" ")[0];
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
			if (currentGroup.getEspJobGroupNotifyList() == null) {
				currentGroup.setEspJobGroupNotifyList(new ArrayList<>());
			}
			currentGroup.getEspJobGroupNotifyList().add(line.split(" ", 2)[1]);
		}

		if (line.startsWith("TAG")) {
			if (currentGroup.getTags() == null) {
				currentGroup.setTags(new ArrayList<>());
			}
			currentGroup.getTags().add(line.split(" ", 2)[1]);
		}

		if (line.startsWith("RESOURCE")) {
			if (currentGroup.getResources() == null) {
				currentGroup.setResources(new ArrayList<>());
			}

			String jobResource = line.split(" ", 2)[1];
			currentGroup.getResources().add(extractJobResource(jobResource));
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

	private void doProcessLinkedListData(EspJobGroup in, EspLinkProcessData data) {
		if (data != null) {
			in.getLinkProcessDataList().add(data);

			String desub = data.getDelaySubmission();
			String duout = data.getDueout();
			//String norun = data.noru
			// TODO: review all options
			
			in.setDueout(duout);
			in.setDelaySubmission(desub);

			in.getEspRunStatements().addAll(data.getEspRunStatements());
		}
	}

	/*
	 * As we progress we are discovering more we can do with this data. Do that work here.
	 */
	private void doProcessGroupSchEventElementLogic(EspJobGroup in, SchEventElement data) {
		// Per rules for now, we can only process the elements are are only schedule at time only.

		if (data != null) {
			in.setEventData(data);

			data.getComments().forEach(c -> in.setComment(in.getComment() + "\n" + c.getData()));

			if (data.isScheduleDataOnly()) {

				// Add our calendar as a run statement to a job.
				if (data.getCalendar() != null) {
					String eventCal = data.getCalendar().getData();
					EspRunStatement runstm = new EspRunStatement();
					runstm.setCriteria(eventCal);
					runstm.setJobName(in.getName());

					in.getEspRunStatements().add(runstm);
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
							in.getEspRunStatements().add(runstm);

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
}
