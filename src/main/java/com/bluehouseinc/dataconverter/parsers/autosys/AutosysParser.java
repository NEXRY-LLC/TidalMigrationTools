package com.bluehouseinc.dataconverter.parsers.autosys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.parsers.autosys.model.AutosysDataModel;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysJobVisitor;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysBoxJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysCommandLineJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysFileTriggerJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysFileWatcherJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysJobVisitorImpl;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysSqlAgentJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysWindowsServiceMonitoringJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.util.AutosysJobType;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AutosysParser extends AbstractParser<AutosysDataModel> {

	private final String JOB_HEADER_PATTERN = "(.*\\/\\* -{17} )(.*?)( -{17} \\*\\/)";
	private final String JOB_TYPE_PATTERN = ".*?(insert_job\\: )(.*?)(job_type: )(.*)";
	private final static String EMPTY_LINE_PATTERN = "^\\s*$";

	List<AutosysAbstractJob> parents = new ArrayList<>(); // parents represents job Group

	AutosysJobVisitor autosysJobVisitor;

	public AutosysParser(ConfigurationProvider cfgProvider) {
		super(new AutosysDataModel(cfgProvider));
		this.autosysJobVisitor = new AutosysJobVisitorImpl(getParserDataModel());
	}

	private boolean isJobHeaderLine(String line) {
		return Pattern.matches(JOB_HEADER_PATTERN, line);
	}

	private boolean shouldSkipLine(String line) {
		return line.matches(EMPTY_LINE_PATTERN);
	}

	@Override
	public void parseFile() throws Exception {

		Path autosysfile = this.getParserDataModel().getConfigeProvider().getAutoSysPath();

		if (autosysfile.toFile().exists()) {

			if (autosysfile.toFile().isFile()) {
				parseFile(autosysfile.toFile());
			} else {
				Files.list(autosysfile).forEach(f -> {
					parseFile(f.toFile());
				});
			}

		} else {
			throw new TidalException("File Path or File not found{" + autosysfile.toString() + "}");
		}

		log.debug("Processing TOTAL[{}] data objects", this.getParserDataModel().getDataObjects().size());



		if (this.getParserDataModel().getConfigeProvider().useGroupContainer()) {
			log.info("AUTOSYS.UseGroupContainer is true, setting up jobs to belong to the group attribute as top level grouping");

			Map<String, AutosysBoxJob> dataMap = new HashMap<>();

			this.getParserDataModel().getDataObjects().forEach(job -> {
				String containerName = job.getGroupAttribute(); // this is 1st level JobGroup in TIDAL

				if (StringUtils.isBlank(containerName)) {
					containerName = "UNKNOWN";
				}

				if (dataMap.containsKey(containerName)) {
					dataMap.get(containerName).addChild(job);
				} else {
					AutosysBoxJob jobGroup = new AutosysBoxJob(containerName);
					jobGroup.setRunCalendar("Daily");
					jobGroup.addChild(job);
					dataMap.put(containerName, jobGroup);
				}

			});

			if (!dataMap.isEmpty()) {
				List<AutosysAbstractJob> mapped = new LinkedList<>();
				dataMap.forEach((containerName, boxJob) -> mapped.add(boxJob));

				// Replace data with our new grouped data
				this.getParserDataModel().setDataObjects(mapped);
			}
		}
	}

	public void parseFile(File file) {

		log.debug("Processing File[{}]", file.getAbsolutePath());

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			AutosysAbstractJob autosysAbstractJob; // Start a new one, whether is BOX or some other job type.

			while ((line = readLine(reader)) != null) {
				if (shouldSkipLine(line)) {
					continue;
				}

				if (isJobHeaderLine(line)) {
					String jobName = RegexHelper.extractNthMatch(line, JOB_HEADER_PATTERN, 1);

					if (StringUtils.isBlank(jobName)) {
						throw new RuntimeException("jobName is EMPTY for following line=" + line);
					}

					autosysAbstractJob = extractJob(reader, line, jobName);

					if (autosysAbstractJob.getParent() == null) {
						this.getParserDataModel().addDataDuplicateLevelCheck(autosysAbstractJob); // Add our object
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private List<String> parseJobLines(BufferedReader reader) throws IOException {
		List<String> lines = new ArrayList<>();
		String line;
		while ((line = readLine(reader)) != null) {
			if (!line.matches(JOB_HEADER_PATTERN)) {
				// System.out.println(line);
				if (StringUtils.isBlank(line)) {
					break;
				}
				lines.add(line);
			}
		}
		return lines;
	}

	private AutosysAbstractJob extractJob(final BufferedReader reader, String line, String jobName) throws Exception {
		String currentLine;
		// FIXME: Read https://softwareengineering.stackexchange.com/q/419445/339761 for more info
		while (!(currentLine = readLine(reader)).matches(JOB_TYPE_PATTERN))
			;

		String jobTypeLine = RegexHelper.extractNthMatch(currentLine, JOB_TYPE_PATTERN, 3);
		List<String> lines = parseJobLines(reader);

		AutosysAbstractJob job;
		AutosysJobType autosysJobType = AutosysJobType.valueOf(jobTypeLine);
		switch (autosysJobType) {
		case BOX:
		case b:
			job = new AutosysBoxJob(jobName);
			this.parents.add(job); // considered as Job Group, i.e., parent object
			break;
		case CMD:
		case c:
			job = new AutosysCommandLineJob(jobName);
			break;
		case FW:
		case f:
			job = new AutosysFileWatcherJob(jobName);
			break;
		case FT:
			job = new AutosysFileTriggerJob(jobName);
			break;
		case OMS:
			job = new AutosysWindowsServiceMonitoringJob(jobName);
			break;
		case SQLAGENT:
			job = new AutosysSqlAgentJob(jobName);
			break;
		default:
			log.error("Unknown job type[{}]!\n", jobTypeLine);
			throw new RuntimeException("Unknown job type[" + jobTypeLine + "]!");
		}


		job.accept(autosysJobVisitor, lines, parents);


		if (this.getParserDataModel().getConfigeProvider().clearBoxConditions()) {

			if(job instanceof AutosysBoxJob) {
				AutosysBoxJob bj = (AutosysBoxJob) job;
				bj.setCondition(null); // Clear out
			}

		}

		return job;
	}

	@Override
	public IModelReport getModelReporter() {
		return new AutoSystReporter();
	}

}
