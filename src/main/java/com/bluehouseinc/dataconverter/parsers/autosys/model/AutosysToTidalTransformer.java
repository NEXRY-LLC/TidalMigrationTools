package com.bluehouseinc.dataconverter.parsers.autosys.model;

import java.util.List;

import com.bluehouseinc.dataconverter.api.importer.APIJobUtils;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvFileWatcherJob;
import com.bluehouseinc.dataconverter.model.impl.CsvJobClass;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvMsSqlJob;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CsvResource;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysBoxJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysCommandLineJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysFileTriggerJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysFileWatcherJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysSqlAgentJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysWindowsServiceMonitoringJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.statements.AutosysResourceStatement;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.DateParser;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.toolkit.CommandLine;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;
import com.bluehouseinc.util.APIDateUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class AutosysToTidalTransformer implements ITransformer<List<AutosysAbstractJob>, TidalDataModel> {

	private TidalDataModel tidalDataModel;
	AutosysDataModel autosysdatamodel;

	public AutosysToTidalTransformer(TidalDataModel tidalDataModel, AutosysDataModel autosysdatamodel) {
		this.tidalDataModel = tidalDataModel;
		this.autosysdatamodel = autosysdatamodel;
	}

	@Override
	public TidalDataModel transform(List<AutosysAbstractJob> baseJobOrGroupObjects) throws TransformationException {
		baseJobOrGroupObjects.forEach(f -> doProcessObjects(f, null));

		return tidalDataModel;
	}

	public void doProcessObjects(AutosysAbstractJob autosysAbstractJob, CsvJobGroup parent) {
		if (autosysAbstractJob.isGroup()) {

			CsvJobGroup group = (CsvJobGroup) processBaseJobOrGroupObject(autosysAbstractJob, parent);

			autosysAbstractJob.getChildren().forEach(autosysChildJob -> doProcessObjects((AutosysAbstractJob) autosysChildJob, group)); // Parse children

		} else {
			BaseCsvJobObject newJob = processBaseJobOrGroupObject(autosysAbstractJob, parent);
			if (newJob == null) {
				log.error("newJob IS NULL for autosysAbstractJob for name={}; fullPath={}", autosysAbstractJob.getName(), autosysAbstractJob.getFullPath());
				throw new RuntimeException("Error, (converted BaseCsvJobObject) newJob is NULL!");
			}

			if (parent != null) {
				parent.addChild(newJob);
			} else {
				this.getTidalDataModel().addJobToModel(newJob);
			}

			log.debug("Processing Job Name[{}]", newJob.getFullPath());
		}

	}


	// TODO: Check here to see if we can take our file trigger job type and use it to apply to the children jobs that depende on me.

	private BaseCsvJobObject processBaseJobOrGroupObject(AutosysAbstractJob base, CsvJobGroup parent) {

		if (base == null) {
			throw new TidalException("BaseJobOrGroupObject is null");
		}

		log.debug("processBaseJobOrGroupObject() Processing Job/Group Name[{}]", base.getFullPath());


		BaseCsvJobObject baseCsvJobObject = null;
		if (base instanceof AutosysBoxJob) {
			baseCsvJobObject = processAutosysBoxJob((AutosysBoxJob) base, parent);
		} else if (base instanceof AutosysCommandLineJob) {
			baseCsvJobObject = processAutosysCommandLineJob((AutosysCommandLineJob) base);
		} else if (base instanceof AutosysFileWatcherJob) {
			baseCsvJobObject = processAutosysFileWatcherJob((AutosysFileWatcherJob) base);
		} else if (base instanceof AutosysFileTriggerJob) {
			baseCsvJobObject = processAutosysFileTriggerJob((AutosysFileTriggerJob) base);
		} else if (base instanceof AutosysSqlAgentJob) {
			baseCsvJobObject = processPlaceHolderJob(base);
		} else if (base instanceof AutosysWindowsServiceMonitoringJob) {
			baseCsvJobObject = processPlaceHolderJob(base);
		} else {
			throw new TidalException("Error, unknown job type for Name=[" + base.getFullPath() + "]");
		}


		doSetCommonJobInformation(base, baseCsvJobObject);

		doAddJobClassToJob(base, baseCsvJobObject);

		if (base instanceof AutosysBoxJob) {
			// Nothing here
		} else {
			if (StringUtils.isBlank(base.getRunCalendar())) {
				baseCsvJobObject.setInheritCalendar(true);
			}
		}


		return baseCsvJobObject;
	}

	private BaseCsvJobObject processAutosysBoxJob(AutosysBoxJob autosysBoxJob, CsvJobGroup parent) {
		CsvJobGroup csvJobGroup = new CsvJobGroup();

		if (parent != null) {
			parent.addChild(csvJobGroup);
		} else {
			this.getTidalDataModel().addJobToModel(csvJobGroup);
		}

		return csvJobGroup;
	}

	private BaseCsvJobObject processAutosysCommandLineJob(AutosysCommandLineJob autosysCommandLineJob) {
		CsvOSJob csvOSJob = new CsvOSJob();
		String cmd = autosysCommandLineJob.getCommand();

		// TODO: Fix this issue with processing unbalanced quotes either:
		// a) Here in this current method (so APIJobUtils.setJobCommandDetail is NOT modified due to potential unexpected behaviour in conversions for other
		// parsers)
		// b) In AutosysJobVisitorImpl class
		// APIJobUtils.setJobCommandDetail(csvOSJob, cmd);
		setJobCommandDetails(csvOSJob, cmd);

		if (autosysCommandLineJob.getProfile() != null) {
			autosysCommandLineJob.setProfile(autosysCommandLineJob.getProfile().replace(".ksh", ".env").replace(".sh", ".env"));
			csvOSJob.setEnvironmentFile(autosysCommandLineJob.getProfile());
		}

		return csvOSJob;
	}

	private BaseCsvJobObject processAutosysFileTriggerJob(AutosysFileTriggerJob autosysFileTriggerJob) {

		autosysFileTriggerJob.setName(autosysFileTriggerJob.getName() + "-XXXX-FILETRIGGER");

		CsvOSJob csvOSJob = new CsvOSJob();
		String cmd = "UNSET";

		APIJobUtils.setJobCommandDetail(csvOSJob, cmd);

		return csvOSJob;
	}

	private BaseCsvJobObject processAutosysFileWatcherJob(AutosysFileWatcherJob autosysFileWatcherJob) {
		CsvFileWatcherJob csvFileWatcherJob = new CsvFileWatcherJob();
		int interval = Integer.parseInt(autosysFileWatcherJob.getWatchInterval());
		csvFileWatcherJob.setPollInterval(interval);

		if (autosysFileWatcherJob.getWatchMaxIntervals() != null) {
			int max = Integer.parseInt(autosysFileWatcherJob.getWatchMaxIntervals());
			csvFileWatcherJob.setPollMaxDuration(max * interval); // Looks like TIDAL wants a total amount of time where AS says how many times so just multiply
		}

		String file = autosysFileWatcherJob.getWatchFile();
		String dir = "UNSET";
		String filemask = "UNSET";

		if (file.contains("/")) {
			filemask = file.substring(file.lastIndexOf("/") + 1);
			dir = file.substring(0, file.lastIndexOf("/") + 1);
		} else if (file.contains("\\")) {
			filemask = file.substring(file.lastIndexOf("\\") + 1);
			dir = file.substring(0, file.lastIndexOf("\\") + 1);
		}

		// Remove quotes from our path.. Not needed in TIDAL.
		csvFileWatcherJob.setDirectory(dir.replace("\"", ""));
		csvFileWatcherJob.setFilemask(filemask.replace("\"", ""));

		if (autosysFileWatcherJob.getProfile() != null) {
			autosysFileWatcherJob.setProfile(autosysFileWatcherJob.getProfile().replace(".ksh", ".env").replace(".sh", ".env"));
			csvFileWatcherJob.setEnvironmentFile(autosysFileWatcherJob.getProfile());
		}

		log.debug("DIR[{}] FILEMASK[{}]", dir, filemask);
		return csvFileWatcherJob;
	}

	private BaseCsvJobObject processAutosysSqlAgentJob(AutosysSqlAgentJob autosysSqlAgentJob) {
		// TODO: Implement processAutosysSqlAgentJob as a job placeholder just like in ESP!!!
		CsvMsSqlJob csvMsSqlJob = new CsvMsSqlJob();
		csvMsSqlJob.setId(autosysSqlAgentJob.getId());
		csvMsSqlJob.setAgentName(autosysSqlAgentJob.getSqlAgentJobname());

		return csvMsSqlJob;
	}

	private BaseCsvJobObject processPlaceHolderJob(AutosysAbstractJob autosysAbstractJob) {


		// NOTE: This method is used as replacement for handling AutosysSqlAgentJob and processAutosysWindowsServiceMonitoringJob job
		CsvOSJob csvOSJob = new CsvOSJob();
		String jobType;
		String currentJobNotes;
		if (autosysAbstractJob instanceof AutosysSqlAgentJob) {
			AutosysSqlAgentJob autosysSqlAgentJob = (AutosysSqlAgentJob) autosysAbstractJob;
			jobType = "SQLAGENT";
			currentJobNotes = "sqlagent_jobname=" + autosysSqlAgentJob.getSqlAgentJobname() + System.lineSeparator() + "sqlagent_user_name=" + autosysSqlAgentJob.getSqlAgentUserName() + System.lineSeparator() + "sqlagent_domain_name="
					+ autosysSqlAgentJob.getSqlAgentDomainName() + System.lineSeparator() + "sqlagent_server_name=" + autosysSqlAgentJob.getSqlAgentServerName() + System.lineSeparator() + "sqlagent_target_db="
					+ autosysSqlAgentJob.getSqlAgentTargetDb();

		} else if (autosysAbstractJob instanceof AutosysWindowsServiceMonitoringJob) {
			AutosysWindowsServiceMonitoringJob autosysWindowsServiceMonitoringJob = (AutosysWindowsServiceMonitoringJob) autosysAbstractJob;
			jobType = "OMS";
			currentJobNotes = "monitor_mode=" + autosysWindowsServiceMonitoringJob.getMonitorMode().toString() + System.lineSeparator() + "win_service_name=" + autosysWindowsServiceMonitoringJob.getWinServiceName() + System.lineSeparator()
					+ "win_service_status=" + autosysWindowsServiceMonitoringJob.getWinServiceStatus();
		} else {
			jobType = "UNKNOWN";
			currentJobNotes = null;
		}

		csvOSJob.setName(autosysAbstractJob.getName() + "-PLACEHOLDER-" + jobType);
		csvOSJob.setCommandLine("PLACEHOLDER");
		csvOSJob.setNotes(currentJobNotes);

		return csvOSJob;
	}


	private void setJobCommandDetails(CsvOSJob csvOSJob, String cmd) {
		if (cmd == null) {
			cmd = "ERROR NOT SET";
		}
		// quotes are in the XML we get back sometimes.
		cmd = cmd.replaceAll("&quot;", "\"");

		if (cmd.contains("\"\"")) {
			cmd = cmd.replace("\"\"", "");
		}
		if (cmd.contains("\"")) {
			cmd = cmd.replace("\"", "");
		}

		// TODO-IMPORTANT: Also, modify value of `cmd` parameter since it can contain batch/executable/script file along with parameters and that should be
		// processd as separate field of CsvOSJob variable (`commandLine` and `parameters`)

		try {
			CommandLine cl = CommandLine.parse(cmd);
			String exe = cl.getExecutable();
			String params = String.join("\n", cl.getArguments());
			csvOSJob.setCommandLine(exe);

			if (!StringUtils.isBlank(params)) {
				csvOSJob.setParamaters(params);
			}
		} catch (IllegalArgumentException e) {
			throw new TidalException(e);
		}
	}

	private void doSetCommonJobInformation(AutosysAbstractJob autosysAbstractJob, BaseCsvJobObject baseCsvJobObject) {

		baseCsvJobObject.setId(autosysAbstractJob.getId());
		baseCsvJobObject.setName(autosysAbstractJob.getName());
		baseCsvJobObject.setNotes(autosysAbstractJob.getDescription());

		if (baseCsvJobObject.getName().equalsIgnoreCase("CTS_UTIL_PRD2_0410_010.Monitor_DEV2-0001_WAE_App_Server")) {
			baseCsvJobObject.getName();
		}

		getTidalDataModel().addOwnerToJobOrGroup(baseCsvJobObject, getTidalDataModel().getDefaultOwner());

		String rte = autosysAbstractJob.getOwner();
		if (rte != null && !rte.isEmpty()) {
			// I think this is a runtime user parse the @ and left is
			// machine: w2k3prod
			// owner: administrator@WILSONS
			if (rte.contains("@")) {
				String[] vals = rte.split("@");
				getTidalDataModel().addRunTimeUserToJobOrGroup(baseCsvJobObject, new CsvRuntimeUser(vals[0], vals[1]));
			} else {
				getTidalDataModel().addRunTimeUserToJobOrGroup(baseCsvJobObject, new CsvRuntimeUser(rte));
			}
		}

		String agent = autosysAbstractJob.getMachine();
		if (StringUtils.isBlank(agent)) {
			getTidalDataModel().addNodeToJobOrGroup(baseCsvJobObject, "UNKNOWN");
		} else {
			getTidalDataModel().addNodeToJobOrGroup(baseCsvJobObject, agent);

		}


		if (autosysAbstractJob.getRunCalendar() != null) {
			CsvCalendar cal = new CsvCalendar(autosysAbstractJob.getRunCalendar());
			getTidalDataModel().addCalendarToJobOrGroup(baseCsvJobObject, cal);
		} else {
			String daysOf = autosysAbstractJob.getDaysOfWeek();
			if (daysOf != null) {
				if (daysOf.equals("all")) {
					getTidalDataModel().addCalendarToJobOrGroup(baseCsvJobObject, new CsvCalendar("Daily"));
				} else {
					// Just combine them
					getTidalDataModel().addCalendarToJobOrGroup(baseCsvJobObject, new CsvCalendar(daysOf.replace(",", "-")));
				}
			}
		}

		if (autosysAbstractJob.getStartTimes() != null) {

			// Time objects... We can have a single start time in our start times, so this would be just a plain start time on a job
			String startTimes = autosysAbstractJob.getStartTimes().replace(" ", "").replace("\"", "");
			// 12:01
			// startTimes = startTimes;//.replace("12:01", "12:00").replace("0:01", "0000");

			APIDateUtils.setRerunSameStartTimes(startTimes, baseCsvJobObject, getTidalDataModel(), true);
		}

		if (autosysAbstractJob.getRunWindow() != null) {
			// 05:00 - 17:05 . odd layout but simple
			String[] wins = autosysAbstractJob.getRunWindow().replace(" ", "").replace("\"", "").split("-");
			baseCsvJobObject.setStartTime(wins[0].replace(":", ""));
			baseCsvJobObject.setEndTime(wins[1].replace(":", ""));

		}

		if (!StringUtils.isBlank(autosysAbstractJob.getStartMins())) {
			// If not blank and no comma, must be bad data?
			String startdata = autosysAbstractJob.getStartMins().trim();
			// Cant have a start minutes be a single number.
			if (startdata.contains(",")) {
				// TODO: Make this a prop setting to override this but for now make it static
				// 00,05,10,15,20,25,30,35,40,45,50,55
				String startTimes = autosysAbstractJob.getStartMins();

				APIDateUtils.setRerunSameStartMinutes(startTimes, baseCsvJobObject, getTidalDataModel(), true);
			}
		}

		if (getAutosysdatamodel().getConfigeProvider().disableCaryOverOnRerun()) {
			// We want to disable carry over on a rerunning jobs.

			if (baseCsvJobObject.getRerunLogic().isReRunningJob()) {
				baseCsvJobObject.setDisableCarryOver(true);
			}
		}

		if (autosysAbstractJob.getResource() != null) {
			AutosysResourceStatement autosysResource = autosysAbstractJob.getResource();
			CsvResource csvResource = new CsvResource(autosysResource.getResourceName(), this.getTidalDataModel().getDefaultOwner());
			csvResource.setLimit(autosysResource.getQuantity());

			this.getTidalDataModel().addResourceToJob(baseCsvJobObject, csvResource);
		}


		String offstart = getAutosysdatamodel().getConfigeProvider().offSetTimeStart();
		String offend = getAutosysdatamodel().getConfigeProvider().offSetTimeEnd();
		String offpref = getAutosysdatamodel().getConfigeProvider().offSetCalendarPrefix();
		Integer offday = getAutosysdatamodel().getConfigeProvider().offSetCalendarDays();

		if (offstart.equals(offend)) {
			// do nothing.
		} else {
			// We are asking for an offset to be set because ?? Customer new to TIDAL decided to change from defaults.

			if (StringUtils.isBlank(baseCsvJobObject.getStartTime())) {
				// DO nothing.
			} else {
				String sttime = baseCsvJobObject.getStartTime();
				java.util.Calendar starttime = DateParser.parseMilitaryTime(offstart.replace(":", ""));
				java.util.Calendar endtime = DateParser.parseMilitaryTime(offend.replace(":", ""));
				java.util.Calendar jobstart = DateParser.parseMilitaryTime(sttime.replace(":", ""));

				// Long range = ChronoUnit.HOURS.between(starttime.toInstant(), endtime.toInstant());
				boolean startisbeforejob = starttime.before(jobstart);
				boolean endtimeisafterjob = endtime.after(jobstart);

				if (startisbeforejob && endtimeisafterjob) {
					// We are time between

					if (StringUtils.isBlank(offstart) && offday == 0) {
						throw new TidalException("Must set offset prefix or time");
					} else {
						if (StringUtils.isBlank(offstart)) {
							baseCsvJobObject.setCalendarOffset(offday);
						} else {
							String calpre = baseCsvJobObject.getCalendar().getCalendarName() + offpref;
							getTidalDataModel().addCalendarToJobOrGroup(baseCsvJobObject, new CsvCalendar(calpre));
						}
					}

				}

			}
		}
	}

	private void doAddJobClassToJob(AutosysAbstractJob autosysAbstractJob, BaseCsvJobObject baseCsvJobObject) {
		if (autosysAbstractJob.getGroupAttribute() != null || autosysAbstractJob.getApplicationAttribute() != null) {
			CsvJobClass csvJobClass;
			if (autosysAbstractJob.getGroupAttribute() != null) {
				csvJobClass = new CsvJobClass(autosysAbstractJob.getGroupAttribute());
			} else {
				csvJobClass = new CsvJobClass(autosysAbstractJob.getApplicationAttribute());
			}

			this.getTidalDataModel().addJobClassToJob(baseCsvJobObject, csvJobClass);
		}
	}

}
