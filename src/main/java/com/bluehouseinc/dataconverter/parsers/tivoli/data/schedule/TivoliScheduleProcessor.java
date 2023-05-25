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
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.ResourceData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.job.JobScheduleDetail;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOn;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOnRequest;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOnRunCycle;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOnType;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@Component
public class TivoliScheduleProcessor {

	private final static String SCHED_PATTERN = "^SCHEDULE (\\w+)#(\\w+)";
	private final static String END_PATTERN = "END";
	private final static String JOB_DEP_PATTERN = "(\\w+)#(\\w+)";

	Map<String, List<SchedualData>> data = new HashMap<>();

	public void doProcessFile(File datafile) {

		BufferedReader reader = null;

		try {
			if (datafile == null) {
				throw new TidalException("Missing cpu file");
			}

			reader = new BufferedReader(new FileReader(datafile));

			String line;

			while ((line = EspFileReaderUtils.readLineTrimmed(reader)) != null) {

				line.trim();

				if (EspFileReaderUtils.skippedLine(line)) {
					continue;
				}
				if (line.startsWith("#")) {
					continue;
				}

				if (isSchedulePattern(line)) {

					processata(reader, line);

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

	private void processata(final BufferedReader reader, String cpuline) throws IOException {

		String groupname = RegexHelper.extractNthMatch(cpuline, SCHED_PATTERN, 0);
		String applytojobname = RegexHelper.extractNthMatch(cpuline, SCHED_PATTERN, 1);

		SchedualData schedule = new SchedualData();

		schedule.setGroupName(groupname);
		schedule.setName(applytojobname);

		List<String> lines = EspFileReaderUtils.parseJobLines(reader, END_PATTERN, null);

		boolean isdepdata = false;
		List<String> depdata = new ArrayList<>();

		for (String line : lines) {

			if (line.startsWith(":")) { // ALL data from this point is dep data.
				isdepdata = true;
				continue;
			} else if (isdepdata) {
				depdata.add(line);
				continue;
			} else {

				String data[] = line.split(" ", 2);
				String element = data[0];
				String value = null;

				if (data.length >= 2) {
					value = data[1].trim();
				}

				switch (element) {
				case "NEEDS":
					doSetNeedsData(value, schedule);
					break;
				case "ON":
					doSetOnData(value, schedule);
					break;
				case "AT":
					schedule.setAtTime(new JobRunTime(value));
					break;
				case "CARRYFORWARD":
					// Not needed
					break;
				case "FOLLOWS":
					JobFollows jobfollows = new JobFollows();
					if (RegexHelper.matchesRegexPattern(JOB_DEP_PATTERN, value)) {
						String ingroup = RegexHelper.extractNthMatch(value, JOB_DEP_PATTERN, 0);
						String followthisjob = RegexHelper.extractNthMatch(value, JOB_DEP_PATTERN, 1);
						jobfollows.setInGroup(ingroup);
						jobfollows.setJobToFollow(followthisjob);
					} else {
						jobfollows.setJobToFollow(value);
						jobfollows.setInGroup(schedule.getGroupName());
					}
					schedule.getFollows().add(jobfollows);
					break;
				case "DESCRIPTION":
					if(StringUtils.isBlank(schedule.getDescription())) {
						schedule.setDescription(value);
					}else {
						schedule.setDescription(schedule.getDescription() +"\n" + value);
					}
					
					break;
				case "DRAFT":
				case "LIMIT":
				case "OPENS":
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

		if (!depdata.isEmpty()) {
			doProcessDepData(depdata, schedule);
		}

		if (this.data.containsKey(groupname)) {
			this.data.get(groupname).add(schedule);
		} else {
			List<SchedualData> schedlist = new ArrayList<>();
			schedlist.add(schedule);
			this.data.put(groupname, schedlist);
		}
	}

	public void doSetNeedsData(String data, SchedualData obj) {
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

		obj.getNeeds().add(res);
		// Expect number and then
		// Group and name of resource
		// AMFINAN1#BONUS2DF;

	}

	public void doSetOnData(String data, SchedualData obj) {
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

	public void doProcessDepData(List<String> objdata, SchedualData obj) {

		JobScheduleDetail jobdetail = null;

		for (String line : objdata) {
			line = line.trim();
			if (RegexHelper.matchesRegexPattern(line, JOB_DEP_PATTERN)) {

				jobdetail = new JobScheduleDetail();
				String groupname = RegexHelper.extractNthMatch(line, JOB_DEP_PATTERN, 0);
				String jobname = RegexHelper.extractNthMatch(line, JOB_DEP_PATTERN, 1);
				jobdetail.setGroupName(groupname);
				jobdetail.setJobName(jobname);

				obj.getJobScheduleDetail().add(jobdetail);
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
					JobFollows jobfollows = new JobFollows();
					if (RegexHelper.matchesRegexPattern(JOB_DEP_PATTERN, value)) {
						String ingroup = RegexHelper.extractNthMatch(value, JOB_DEP_PATTERN, 0);
						String followthisjob = RegexHelper.extractNthMatch(value, JOB_DEP_PATTERN, 1);
						jobfollows.setInGroup(ingroup);
						jobfollows.setJobToFollow(followthisjob);
					} else {
						jobfollows.setJobToFollow(value);
						jobfollows.setInGroup(obj.getGroupName());
					}

					jobdetail.getFollows().add(jobfollows);
					break;
				case "PRIORITY":
					jobdetail.setPriority(value);
					break;
				case "AT":
					// AT time or AT Time unitl TIME
					if (value.contains("UNTIL")) {
						String tmpat = value.substring(0, value.indexOf("UNTIL"));
						String tmpuntil = value.substring(value.indexOf("UNTIL")+6, value.length());
						jobdetail.setAtTime(tmpat);
						jobdetail.setUntilTime(tmpuntil);
					} else {
						jobdetail.setAtTime(value);
					}
					break;
				case "EVERY":
					jobdetail.setRerunEvery(value);
					break;
				case "UNTIL":
					jobdetail.setUntilTime(value);
					break;
				case "DEADLINE":
					jobdetail.setDeadline(new JobRunTime(value));
					break;
				case "CRITICAL":
					jobdetail.setCritical(true);
					break;
				case "OPENS":
				case "PROMPT":
					break;
				default:
					log.info("Unknown SCHED Data Element: " + line);
				}
			}
		}
	}
	
	public SchedualData getSchedualDataByGroupName(String group, String name) {
		
		List<SchedualData> resdata = this.data.get(group);
		
		if(resdata == null) {
			return null;
		}
		return resdata.stream().filter(f -> f.getName().trim().equalsIgnoreCase(name.trim())).findFirst().orElse(null);
	}
}
