package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvOwner;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.util.APIDateUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Adrep1DataModel extends BaseParserDataModel<OPCOSJob, OPCConfigProvider> implements IParserModel {
	private List<Application> applications = new ArrayList<>();

	public Adrep1DataModel(ConfigurationProvider cfgProvider) {
		super(new OPCConfigProvider(cfgProvider));
	}


	@Override
	public BaseVariableProcessor<OPCOSJob> getVariableProcessor(TidalDataModel model) {
		return null;
	}

	@Override
	public ITransformer<List<OPCOSJob>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return null;
	}

	private CsvJobGroup getContainer(Application app, TidalDataModel dm) {

		if (app == null) {
			return null;
		}

		// FIRST THREE OF NAME
		String name = app.getApplicationId() == null ? "NOTDEFINED" : app.getApplicationId().substring(0, 3);

		// I do not know why but calling the Datamodel findGroup gives NPE.. No idea how
		// or why. UPDATE: Kinda need to set the name :)
		// CvsJobGroup grp = toplevels.stream().filter(f ->
		// f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		CsvJobGroup grp = dm.findGroupByName(name);

		if (grp == null) {
			grp = new CsvJobGroup();
			grp.setName(name);
			grp.setRuntimeUser(null);
			CsvCalendar cal = new CsvCalendar("Daily");
			grp.setCalendar(cal);

			CsvRuntimeUser rte = new CsvRuntimeUser("OPCTRE");
			grp.setRuntimeUser(rte);
			dm.addJobToModel(grp);
		}

		return grp;
	}

	@Override
	public void doProcessData(List<OPCOSJob> dataObjects) {
		OPCCalendar ERRORCALENDAR = new OPCCalendar("ERROR_MULTIPLE_ADRULES_WITH_TIMES");
		OPCCalendar ERRORRERUNCAL = new OPCCalendar("TO_MANY_RERUN_OPTIONS");
		// Add our one needed variable for our jobs
		CsvVariable OPCJOBPATH = new CsvVariable("OPCCommandPath");
		// OPCJOBPATH.setVarName("OPCCommandPath");
		OPCJOBPATH.setVarValue("/opt/bin/runjob.sh");
		this.getTidal().addVariable(OPCJOBPATH);

		for (Application a : applications) {

			if (a.getApplicationId().equals("AOEDAILY")) {
				System.out.println();
			}
			CsvJobGroup contgrp = getContainer(a, this.getTidal());
			// Skip all pending applications
			String status = a.getCommonData().getStatus();
			if (status.equals("PENDING") || a.getOperations().isEmpty()) { // Nothing to do here so ignore.
				continue;
			}

			CsvJobGroup group = new CsvJobGroup();
			group.setName(a.getApplicationId()); // The Group Name
			String ownerid = a.getCommonData().getOwnerId();

			if (ownerid == null) {
				ownerid = "Schedulers"; // Job CXF does not have an owner.
			}

			CsvOwner owner = new CsvOwner(ownerid);
			group.setOwner(owner);
			contgrp.setOwner(owner);
			this.getTidal().addOwnerToJobOrGroup(group, owner);
			// FIXED: Review how to build this properly because the Rules, can be multiple
			// calendar days which is not supported with TIDAL
			// but the start times are supported.
			// Logically I need to take each and if the AdRule object has a different name
			// for each then that means a new job to support it
			// If however they are the same, then we are looking at a calender for each job
			// with a list of start times
			// OR, AND A BIG OR, could mean a seperate job to cover the start / end combo.
			// However for sake of time and to provide results for csv formatting, we will
			// do the basics
			// EXAMPLE: AECA39 , it has a addRule for every 30 min so this entire group must
			// run every 30 min
			// FIXED: Review app ABCA15 as it has two adrules same start time but different
			// days.

			if (!a.getRuleBasedRunCycleInformation().isEmpty()) {
				// Not empty so we are rule driven to set a start time

				List<OPCCalendar> rulecals = getCalendarsFromAdrules(a.getRuleBasedRunCycleInformation());

				// List<Duration> durations =
				// getEveryNTimes(a.getRuleBasedRunCycleInformation());

				// If anything other than one addRule Calendar type, we have a problem.
				if (rulecals.size() == 1) {
					this.getTidal().addCalendarToJobOrGroup(group, rulecals.get(0));// Add to our model as our group has
																					// a single calendar
					group.setCalendar(rulecals.get(0)); // Set our group calender to this calender
					OPCCalendar gcal = rulecals.get(0);
					if (gcal.getStarttimes().size() == 1) {
						// Just a start time;
						// README: Per Dwayne we should not set a start or end time on the groups, only
						// deal with the rerun information.
						// group.setStartTime(gcal.getStarttimes().get(0).replace(":", "")); //
						// CustomStartTimes needs colon , start does not.
					} else {
						String times = String.join(",", gcal.getStarttimes());
						APIDateUtils.setRerunNewStartTimes(times, group, this.getTidal(), true);
					}

				} else {
					System.out.println("###################################MULTIPLE ADDRULES WITH MULITPLE START TIMES [" + a.getApplicationId() + "]");
					group.setCalendar(ERRORCALENDAR);
					this.getTidal().addCalendarToJobOrGroup(group, ERRORCALENDAR);
				}

			} else {
				// Nothing, no rules, is adhoc
			}

			for (OperationData jobdata : a.getOperations()) {

				String operationName = jobdata.getOperationName();

				if (operationName.contains("DUMY_")) {
					continue;
				} // we ignore dumy_ jobs.

				// FIXME: add inhertiance to the jobs for calendar
				// FIXME: Add option to ignore deps in not in schedule
				OPCOSJob job = new OPCOSJob(operationName); // The identifier for lookup
				// for now we only know the name but for this customer only, name is actually a
				// param.
				if (jobdata.getJobName().isEmpty()) {
					// Bad data.
					job.setName(jobdata.getOperationName()); // Set to teh same name as the operation, likely DUMY jobs.
				} else {

					job.setCommandLine(OPCJOBPATH.getTokenName()); // Static variable reference , token version for
																	// replacement
					job.setParamaters(jobdata.getJobName());

					// FIXME: When we run into anything other than CPU type, we need to append it to
					// the job name.
					if (operationName.contains("CPU_")) {
						job.setName(jobdata.getJobName());
					} else {
						// This is all our batches, netv, etc. and what not.
						job.setName(jobdata.getJobName() + "_" + operationName);
						job.setParamaters("DUMMYJCL"); //
						job.setRequireOperatorRelease(true);
					}
				}

				// Only if the time is really restricted
				if (jobdata.getOptions().isTimeRestricted()) {
					if (jobdata.getExpectedArrival() != null) {
						String start = jobdata.getExpectedArrival().replace(".", ""); // Jobs can have other start
																						// times.
						start = start.replace("01 ", "").replace("02 ", ""); // No idea why we have this.
						start = start.replace("N", "").replace("Y", ""); // Again why do we have this?
						job.setStartTime(start);
					} else {
						// Get time from parent.
						// FIXME: Get time from parent as the parent is the default is the OPC job is
						// time based but arrival is blank
						// README: ABOA60 - Timewindow error message
						if (!a.getRuleBasedRunCycleInformation().isEmpty()) {
							String start = a.getRuleBasedRunCycleInformation().get(0).getInputArrive().replace(".", ""); // Jobs
																															// can
																															// have
																															// other
																															// start
																															// times.
							start = start.replace("01 ", "").replace("02 ", ""); // No idea why we have this.
							start = start.replace("N", "").replace("Y", ""); // Again why do we have this?
							job.setStartTime(start);
						}
					}
				}
				// README: Dwayne said to not include end time on the jobs.
				// if (jobdata.getDeadLine() != null) {
				// String end = jobdata.getDeadLine().replace(".", ""); // Jobs can have other end times.
				// end = end.replace("01 ", "").replace("02 ",""); // No idea why we have this.
				// end = end.replace("N", "").replace("Y", ""); // Again why do we have this?
				// job.setEndTime(end);
				// }

				group.addChild(job); // Add this job to this group
			}
			contgrp.addChild(group);
			// dm.addGroup(group); // Finally add our group of jobs to our data model.
		}

		// Now that we have setup all our jobs and groups of jobs, let's work through
		// our dep references. We have to do this outside of teh first loop
		// because of the external group -> job refernces.

		OPCOSJob currentjob = null;

		for (Application a : applications) {

			// Skip all pending applications
			String status = a.getCommonData().getStatus();
			if (status.equals("PENDING") || a.getOperations().isEmpty()) { // Nothing to do here so ignore.
				continue;
			}

			String name = a.getApplicationId();

			if (name.equals("ALMA80")) {
				// System.out.println();
			}
			// Our current group
			String curname = a.getApplicationId();
			CsvJobGroup curgroup = this.getTidal().findGroupByName(curname);

			// We have two flavors , CPU_<Name> or the actual job name , so each dep is
			// listed twice, one of each type.
			for (InternalOperationLogic log : a.getInternalOperationLogic()) {
				if (name.equals("ACCA10")) {
					// System.out.println();
				}
				// System.out.println(log.getExternalJobRef() + " <- " + log.getOperation() + "
				// -> " + log.getInternalJobRef());
				// Skip this type as we need to work with the appid identifier of CPU_, DUMMU_,
				// etc NOT JOB NAMES

				if (!log.isOperationLookupType() || (log.getExternalJobRef() == null && log.getInternalJobRef() == null)) {
					continue;
				}

				if (!log.getOperation().isEmpty()) { // Looking for the referenced internal job (E.G Operations in OPC)
					for (BaseJobOrGroupObject job : curgroup.getChildren()) {
						OPCOSJob opcjob = (OPCOSJob) job;
						String oper = log.getOperation();
						if (opcjob.getOperationName().equalsIgnoreCase(oper)) {
							currentjob = opcjob;
							break;
						}
					}
					// currentjob = (OPCOSJob) curgroup.getChildren().stream().filter(f ->
					// f.getName().equalsIgnoreCase(log.getOperation())).findFirst().orElse(null);
				}

				if (currentjob == null) {
					System.out.println("Bad Data");
					continue;
				} // bad data.

				// This is a dependency to a job in my group of jobs
				if (log.getInternalJobRef() != null) {
					String jobid = log.getInternalJobRef().getJobId();

					OPCOSJob depjob = null;

					for (BaseJobOrGroupObject job : curgroup.getChildren()) {
						OPCOSJob opcjob = (OPCOSJob) job;
						if (opcjob.getOperationName().equalsIgnoreCase(jobid)) {
							depjob = opcjob;
							break; // Found our job to be dependent on.
						}
					}

					if (depjob == null) {
						// System.out.println("Bad Data");
						continue;
					} // bad data

					// Add our dependency to our current job
					this.getTidal().addJobDependencyForJobCompletedNormal(currentjob, depjob, null);
					// System.out.println(dep);
				} else if (log.getExternalJobRef() != null) {
					// skip bad data
					if ((log.getExternalJobRef().getJobId() == null) || log.getExternalJobRef().getJobId().isEmpty()) {
						// System.out.println("Bad Data");
						continue;
					} // skip bad data
						// This is a reference to another group's job
					String externjobid = log.getExternalJobRef().getJobId();
					String externgroupname = log.getExternalJobRef().getAppId();

					CsvJobGroup externgroup = this.getTidal().findGroupByName(externgroupname);

					if (externgroup == null) {
						// System.out.println("Bad Data");
						continue;
					} // bad data ref

					OPCOSJob externdepjob = null;

					for (BaseJobOrGroupObject externjob : externgroup.getChildren()) {
						OPCOSJob externopcjob = (OPCOSJob) externjob;
						if (externopcjob.getOperationName().equalsIgnoreCase(externjobid)) {
							externdepjob = externopcjob;
							break;
						}
					}

					if (externdepjob == null) {
						// System.out.println("Bad Data");
						continue;
					} // bad data ref

					// Add our dependency to our current job
					this.getTidal().addJobDependencyForJobCompletedNormal(currentjob, externdepjob, null);
					// System.out.println(dep);
				}
			}
		}

	}

	/**
	 * TODO: Figure out if we need to deal with the naming of calendars but for now
	 * we are not. What we are concerned with is applications that have different
	 * addrules , those that have the same addrule we think are just done to set
	 * differnet start times
	 *
	 * @param rules
	 * @return
	 */
	private List<OPCCalendar> getCalendarsFromAdrules(List<RuleBasedRunCycleInformation> rules) {

		List<OPCCalendar> cals = new ArrayList<>();

		// Simple loop over all the named addrules and build a list of unique values
		for (RuleBasedRunCycleInformation rule : rules) {
			String milstart = rule.getInputArrive().replace(".", ":");
			String name = rule.getAdRule().getAdRule();
			// Do we already exists? Using original name, not the cleaned up name.
			OPCCalendar newcal = cals.stream().filter(f -> f.getOriginalName().equals(name)).findFirst().orElse(new OPCCalendar(name));

			if (!cals.contains(newcal)) {
				newcal.getStarttimes().add(milstart);
				cals.add(newcal);
			} else {
				cals.get(cals.indexOf(newcal)).getStarttimes().add(milstart);
			}
		}

		return cals;
	}

}
