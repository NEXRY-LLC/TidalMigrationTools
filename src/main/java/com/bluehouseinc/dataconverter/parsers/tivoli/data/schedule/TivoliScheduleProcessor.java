package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.job.JobScheduleData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOn;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOnRequest;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOnRunCycle;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOnType;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@Component
public class TivoliScheduleProcessor {

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private static String SCHED_PATTERN = "^SCHEDULE (\\w+)#(\\w+)";

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private static String END_PATTERN = "END";

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private static String JOB_EXTERN_DEP_PATTERN = "^(\\w+)#(\\w+)";

	Map<String, List<SchedualData>> scheduleData = new HashMap<>();

	TivoliJobProcessor jobProcessor;

	public TivoliScheduleProcessor(TivoliJobProcessor job) {
		this.jobProcessor = job;
	}

	public void doProcessFile(File datafile) {

		BufferedReader reader = null;

		try {
			if (datafile == null) {
				throw new TidalException("Missing Scheduler file");
			}

			reader = new BufferedReader(new FileReader(datafile));

			String line;

			while ((line = EspFileReaderUtils.readLineTrimmed(reader)) != null) {

				line.trim();

				if (line.toUpperCase().contains("NANOTERM")) {
					line.getBytes();
				}

				if (EspFileReaderUtils.skippedLine(line)) {
					continue;
				}
				if (line.startsWith("#")) {
					continue;
				}

				if (isSchedulePattern(line)) {

					procesData(reader, line);

				}
			}
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {

			}
		}
	}

	private boolean isSchedulePattern(String line) {
		return RegexHelper.matchesRegexPattern(line, SCHED_PATTERN);
	}

	private void procesData(final BufferedReader reader, String cpuline) throws IOException {

		String groupname = RegexHelper.extractNthMatch(cpuline, SCHED_PATTERN, 0).trim();
		String workflowname = RegexHelper.extractNthMatch(cpuline, SCHED_PATTERN, 1).trim();

		if (workflowname.toUpperCase().contains("HNANOTES")) {
			workflowname.getBytes();
		}
		SchedualData schedule = new SchedualData();

		schedule.setGroupName(groupname);
		schedule.setWorkflowName(workflowname);

		List<String> lines = EspFileReaderUtils.parseJobLines(reader, END_PATTERN, null);

		boolean isworkflowdata = false;
		List<String> workflowdata = new ArrayList<>();

		for (String line : lines) {

			if (line.startsWith(":")) { // ALL data from this point is dep data.
				isworkflowdata = true;
				continue;
			} else if (isworkflowdata) {
				workflowdata.add(line);
				continue;
			} else {

				String data[] = line.split(" ", 2);
				String element = data[0];
				String value = null;

				if (line.toUpperCase().contains("NANOTERM")) {
					line.getBytes();

				}

				if (data.length >= 2) {
					value = data[1].trim();
				}

				switch (element) {
				case "NEEDS":
					schedule.getNeeds().add(getNeedsData(value));
					break;
				case "ON":
					// REQUEST is on demand
					doSetOnData(value, schedule);
					break;
				case "AT":
					schedule.setAtTime(new JobRunTime(value));
					break;
				case "CARRYFORWARD":
					// Not needed
					break;
				case "FOLLOWS":
					schedule.getFollows().add(getFollowsFromData(value));
					break;
				case "DESCRIPTION":
					if (StringUtils.isBlank(schedule.getDescription())) {
						schedule.setDescription(value);
					} else {
						schedule.setDescription(schedule.getDescription() + "\n" + value);
					}

					break;
				case "DRAFT":
					break;
				case "LIMIT":
					break;
				case "OPENS":
					if (value.contains("#")) {
						String filename = value.split("#", 2)[1];
						schedule.getFiledeps().add(filename);
					} else {
						schedule.getFiledeps().add(value);
					}
					break;
				case "PRIORITY":
					break;
				case "CRITICAL":
					schedule.setCritical(true);
					break;
				case "DEADLINE":
					schedule.setDeadline(new JobRunTime(value));
					break;
				case "EXCEPT":
					break;
				default:
					log.info("Unknown Data Element: " + line);
				}
			}
		}

		if (!workflowdata.isEmpty()) {
			doProcessWorkflowData(workflowdata, schedule);
		}

		if (this.scheduleData.containsKey(groupname)) {
			this.scheduleData.get(groupname).add(schedule);
		} else {
			List<SchedualData> schedlist = new ArrayList<>();
			schedlist.add(schedule);
			this.scheduleData.put(groupname, schedlist);
		}
	}

	private NeedsResource getNeedsData(String data) {
		// NEEDS 1 AMFINAN1#BONUS2DF

		NeedsResource res = new NeedsResource();

		data.split(" ", 2);

		String myamount = data.split(" ", 2)[0];
		String myresourcedata = data.split(" ", 2)[1];
		String groupname = myresourcedata.split("#")[0];
		String resourcename = myresourcedata.split("#")[1];

		int amount = Integer.valueOf(myamount);

		res.setAmount(amount);
		res.setName(resourcename);
		res.setGroupName(groupname);

		return res;
		// Expect number and then
		// Group and name of resource
		// AMFINAN1#BONUS2DF;

	}

	private void doSetOnData(String data, SchedualData obj) {
		// ON RUNCYCLE APTMON DESCRIPTION "10th of Month" "FREQ=MONTHLY;INTERVAL=1;BYMONTHDAY=10"
		// ON RUNCYCLE CALENDAR1 LN4THWD
		// ON RUNCYCLE RULE1 "FREQ=WEEKLY;BYDAY=SA"
		// ON RUNCYCLE RULE2 "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;"

		String[] elements = data.split(" ", 2);

		RunOnType runtype = RunOnType.valueOf(elements[0]); // SHould always be the type.

		RunOn ondata = null;

		switch (runtype) {
		case REQUEST:
			ondata = new RunOnRequest();
			obj.getRunOn().add(ondata);
			return; // This is on demand, nothing more to do.
		case RUNCYCLE:
			ondata = new RunOnRunCycle();
			break;

		default:
			log.info("Unknown Data RunOnType: " + runtype);
		}

		String calendardata = elements[1].trim(); // RULE, CALENDAR, NAMED CAL, ETC..

		if (calendardata.contains("DESCRIPTION")) {
			int idx = calendardata.indexOf("DESCRIPTION");
			String desc = calendardata.substring(idx, calendardata.length());
			calendardata = calendardata.replace(desc, ""); // remove it.
		}

		if (ondata instanceof RunOnRunCycle) {
			String calendarPattern = "^CALENDAR(\\d+)";
			String rulePattern = "^RULE(\\d+)";

			RunOnRunCycle runcycleobj = (RunOnRunCycle) ondata;
			if (RegexHelper.matchesRegexPattern(calendardata, calendarPattern)) {
				// We are a calendar type.
				String finaldata = RegexHelper.replaceAllSameLength(calendardata, calendarPattern, "");
				runcycleobj.setCalendarName(finaldata.trim());
			} else if (RegexHelper.matchesRegexPattern(calendardata, "^RULE(\\d+)")) {
				String finaldata = RegexHelper.replaceAllSameLength(calendardata, rulePattern, "");
				runcycleobj.setCalendarData(finaldata.trim());
			} else {
				// Who knows.
				runcycleobj.setCalendarData(calendardata);
			}

		}
	}

	private void doProcessWorkflowData(List<String> objdata, SchedualData obj) {

		JobScheduleData jobdetail = null;

		for (String line : objdata) {
			line = line.trim();
			if (RegexHelper.matchesRegexPattern(line, JOB_EXTERN_DEP_PATTERN)) {

				jobdetail = new JobScheduleData();
				String groupname = RegexHelper.extractNthMatch(line, JOB_EXTERN_DEP_PATTERN, 0);
				String jobname = RegexHelper.extractNthMatch(line, JOB_EXTERN_DEP_PATTERN, 1);

				TivoliJobObject job = getJobProcessor().getJobInGroupByName(groupname, jobname);

				if (job == null) {
					log.info("ERROR in Schedual Data, Job {} is missing in group {}", jobname, groupname);
				} else {
					jobdetail.setJob(job);
					jobdetail.setGroupName(groupname);
					obj.getJobScheduleData().add(jobdetail);
				}
				// AMFINAN1#LAWSTAMP
				// AMFINAN1#BNS00A
				// FOLLOWS LAWSTAMP
				// We will start with AMFINAN1#LAWSTAMP (Group and Job) , aka who I should follow if anyone
				// Data looks to start with an anchor job that each will FOLLOW.
				// Then if
			} else {
				// Until we hit our dep pattern again , assume all is for the current one.

				String data[] = line.split(" ", 2);
				String element = data[0];
				String value = null;

				if (data.length >= 2) {
					value = data[1].trim();
				}

				switch (element) {
				case "FOLLOWS":
					jobdetail.getFollows().add(getFollowsFromData(value));
					break;
				case "PRIORITY":
					jobdetail.setPriority(value);
					break;
				case "AT":
					// AT time or AT Time unitl TIME
					if (value.contains("UNTIL")) {
						String tmpat = value.substring(0, value.indexOf("UNTIL"));
						String tmpuntil = value.substring(value.indexOf("UNTIL") + 6, value.length());
						jobdetail.setAtTime(new JobRunTime(tmpat));
						jobdetail.setUntilTime(new JobRunTime(tmpuntil));
					} else {
						jobdetail.setAtTime(new JobRunTime(value));
					}
					break;
				case "EVERY":
					jobdetail.setRerunEvery(Integer.valueOf(value));
					break;
				case "UNTIL":
					jobdetail.setUntilTime(new JobRunTime(value));
					break;
				case "DEADLINE":
					jobdetail.setDeadline(new JobRunTime(value));
					break;
				case "CRITICAL":
					jobdetail.setCritical(true);
					break;
				case "OPENS":
					if (value.contains("#")) {
						jobdetail.setOpensFile(value.split("#", 2)[1]);
					} else {
						jobdetail.setOpensFile(value);
					}
				case "PROMPT":
					break;
				case "NEEDS":
					jobdetail.getNeeds().add(getNeedsData(value));
					break;
				default:
					log.info("Unknown SCHED Data Element: " + line);
				}
			}
		}
	}

	// AGENT#WORKFLOW.JOBNAME
	// AGENT#WORKFLOW.@ // Group Level
	// AGENT#WORKFLOW.JOBNAME PREVIOUS = Yesterday Schedule
	/**
	 * 
	 * @param value
	 * @return
	 */
	private JobFollows getFollowsFromData(String value) {

		if (StringUtils.isBlank(value)) {
			return null;
		}

		JobFollows jobfollows = new JobFollows();

		// Space is important , could have done an ends with too.
		if (value.endsWith(" PREVIOUS")) {
			jobfollows.setPreviousDay(true);
			value = value.replace(" PREVIOUS", "");
		}

		if (RegexHelper.matchesRegexPattern(JOB_EXTERN_DEP_PATTERN, value)) {
			String ingroup = RegexHelper.extractNthMatch(value, JOB_EXTERN_DEP_PATTERN, 0);
			String followthisjob = RegexHelper.extractNthMatch(value, JOB_EXTERN_DEP_PATTERN, 1);
			jobfollows.setInGroup(ingroup);
			if (followthisjob.contains("@")) {
				jobfollows.setDependsOnGroup(true);
			} else {
				jobfollows.setJobToFollow(followthisjob);
			}
		} else {
			jobfollows.setJobToFollow(value);
		}

		return jobfollows;
	}

	public List<SchedualData> getSchedualDataByGroupName(String group) {
		return this.scheduleData.get(group);
	}

	public SchedualData getSchedualDataInGroupByName(String group, String name) {

		List<SchedualData> resdata = getSchedualDataByGroupName(group);

		if (resdata == null) {
			return null;
		}
		return resdata.stream().filter(f -> f.getWorkflowName().trim().equalsIgnoreCase(name.trim())).findFirst().orElse(null);
	}
}
