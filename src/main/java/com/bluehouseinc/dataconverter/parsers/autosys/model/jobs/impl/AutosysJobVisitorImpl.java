package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.parsers.autosys.model.AutosysDataModel;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util.AutosysDependencyParserUtil;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysJobVisitor;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.util.AutosysYesNoType;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.util.SendNotificationType;
import com.bluehouseinc.dataconverter.parsers.autosys.model.statements.AutosysResourceStatement;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

import io.vavr.Function2;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AutosysJobVisitorImpl implements AutosysJobVisitor {

	AutosysDataModel model;

	@Autowired
	AutosysDependencyParserUtil autosysDependencyParserUtil;

	public AutosysJobVisitorImpl(AutosysDataModel model) {
		this.model = model;
	}

	/**
	 * Helper method unique to all other implementations of `visit` method. Same as
	 * one in EspJobVisitorImpl class.
	 */
	private void visitCommon(AutosysAbstractJob job, List<String> lines, List<AutosysAbstractJob> parents, Function2<String, String, Boolean> lambdaFunction) {

		if(job.getName().equals("AHW_QNXT_0300_010.CreateMemberRelation")) {
			job.getName();
		}
		List<String> notificationEmailAddressOnSuccessList = new ArrayList<>();
		List<String> notificationEmailAddressOnAlarmList = new ArrayList<>();
		List<String> notificationEmailAddressOnTerminated = new ArrayList<>();
		List<String> notificationEmailAddressOnFailureList = new ArrayList<>();
		List<String> notificationEmailAddressList = new ArrayList<>();
		List<String> notificationAlarmTypesList = new ArrayList<>();

		for (String line : lines) {

			String key = null;
			String value = null;

			if(line.contains(":")) {
				// Fixing this. description: "Special Instructions: For Exit 1012, or 1096 Set job to completed normally,For any other errors. Escalations:  EDI Hosted Support"
				// where there are multiple : chars
				int idx = line.indexOf(":");

				key = line.substring(0, idx);
				value = line.substring(idx+1).trim();

			}else {
				throw new TidalException("Unknown Line type, missing : char" + line);
			}


			switch (key) {
			case "notification_emailaddress_on_success":
				notificationEmailAddressOnSuccessList.add(value);
				continue;
			case "notification_emailaddress_on_alarm":
				notificationEmailAddressOnAlarmList.add(value);
				continue;
			case "notification_emailaddress_on_terminated":
				notificationEmailAddressOnTerminated.add(value);
				continue;
			case "notification_emailaddress_on_failure":
				notificationEmailAddressOnFailureList.add(value);
				continue;
			case "notification_emailaddress":
				notificationEmailAddressList.add(value);
				continue;
			case "notification_alarm_types":
				notificationAlarmTypesList.add(value);
				continue;
			default:
				boolean hasHitStatement = lambdaFunction.apply(key, value);
				if (hasHitStatement)
					continue;
			}

			fillInDefaultProperties(job, parents, key, value);

			log.debug("Processing job[{}]; Setting name[{}]={}...", job.getFullPath(), key, value);
		}

		if (!notificationEmailAddressOnSuccessList.isEmpty()) {
			job.setNotificationEmailAddressOnSuccessList(notificationEmailAddressOnSuccessList);
		}
		if (!notificationEmailAddressOnAlarmList.isEmpty()) {
			job.setNotificationEmailAddressOnAlarmList(notificationEmailAddressOnAlarmList);
		}
		if (!notificationEmailAddressOnTerminated.isEmpty()) {
			job.setNotificationEmailAddressOnTerminated(notificationEmailAddressOnTerminated);
		}
		if (!notificationEmailAddressOnFailureList.isEmpty()) {
			job.setNotificationEmailAddressOnFailureList(notificationEmailAddressOnFailureList);
		}
		if (!notificationEmailAddressList.isEmpty()) {
			job.setNotificationEmailAddressList(notificationEmailAddressList);
		}
		if (!notificationAlarmTypesList.isEmpty()) {
			job.setNotificationAlarmTypesList(notificationAlarmTypesList);
		}

	}

	@Override
	public void visit(AutosysBoxJob autosysBoxJob, List<String> lines, List<AutosysAbstractJob> parents) {
		if(autosysBoxJob.getName().equals("CMC_FACE_6180__ETS_FACE_CMCX_OPTI_MED_RX_IN")) {
			autosysBoxJob.getName();
		}

		this.visitCommon(autosysBoxJob, lines, parents, (key, value) -> {
			switch (key) {
			case "box_success":
					//autosysBoxJob.setBoxSuccess(AutosysDependencyParserUtil.processJobExpresionData(autosysBoxJob, value));
				break;
			default:
				// no statement hit
				return false;
			}
			return true;
		});
	}

	@Override
	public void visit(AutosysCommandLineJob autosysCommandLineJob, List<String> lines, List<AutosysAbstractJob> parents) {
		this.visitCommon(autosysCommandLineJob, lines, parents, (key, value) -> {
			switch (key) {
			case "chk_files": // currently NOT present in any AUTOSYS dump file
				autosysCommandLineJob.setChkFiles(value);
				break;
			case "command":
				autosysCommandLineJob.setCommand(value);
				break;
			case "elevated":
				autosysCommandLineJob.setElevated(value);
				break;
			case "max_exit_success":
				autosysCommandLineJob.setMaxExitSuccess(Integer.parseInt(value));
				break;
			case "profile":
				autosysCommandLineJob.setProfile(value);
				break;
			case "success_codes":
				autosysCommandLineJob.setSuccessCodes(value);
				break;
			case "std_out_file":
				autosysCommandLineJob.setStdOutFile(value);
				break;
			case "std_err_file":
				autosysCommandLineJob.setStdErrFile(value);
				break;
			default:
				// no statement hit
				return false;
			}
			return true;
		});
	}

	@Override
	public void visit(AutosysFileTriggerJob autosysFileTriggerJob, List<String> lines, List<AutosysAbstractJob> parents) {
		this.visitCommon(autosysFileTriggerJob, lines, parents, (key, value) -> {
			switch (key) {
			case "continuous":
				autosysFileTriggerJob.setContinuous(value);
				break;
			case "watch_file_recursive":
				if (!StringUtils.isBlank(value)) {
					autosysFileTriggerJob.setWatchFileRecursive(AutosysYesNoType.getAutosysYesNoType(value.toUpperCase()));
				} else {
					autosysFileTriggerJob.setWatchFileRecursive(null);
				}
				break;
			case "watch_file_type":
				autosysFileTriggerJob.setWatchFileType(AutosysFileTriggerJob.WatchFileType.valueOf(value.toUpperCase()));
				break;
			case "watch_file_change_value":
				autosysFileTriggerJob.setWatchFileChangeValue(Integer.parseInt(value));
				break;
			case "watch_file_change_type":
				value = value.replaceAll("\"", "");
				autosysFileTriggerJob.setWatchFileChangeType(AutosysFileTriggerJob.WatchFileChangeType.valueOf(value));
				break;
			case "watch_file":
				autosysFileTriggerJob.setWatchFile(value);
				break;
			case "watch_file_owner":
				autosysFileTriggerJob.setWatchFileOwner(value);
				break;
			case "watch_file_win_user":
				autosysFileTriggerJob.setWatchFileWinUser(value);
				break;
			case "watch_no_change":
				autosysFileTriggerJob.setWatchNoChange(value);
				break;
			default:
				// no statement hit
				return false;
			}

			return true;
		});
	}

	@Override
	public void visit(AutosysFileWatcherJob autosysFileWatcherJob, List<String> lines, List<AutosysAbstractJob> parents) {
		this.visitCommon(autosysFileWatcherJob, lines, parents, (key, value) -> {
			switch (key) {
			case "profile":
				autosysFileWatcherJob.setProfile(value);
				break;
			case "watch_file":
				autosysFileWatcherJob.setWatchFile(value);
				break;
			case "watch_interval":
				autosysFileWatcherJob.setWatchInterval(value);
				break;
			case "watch_file_min_size":
				autosysFileWatcherJob.setWatchFileMinSize(value);
				break;
			default:
				// no statement hit
				return false;
			}

			return true;
		});
	}

	@Override
	public void visit(AutosysSqlAgentJob autosysSqlAgentJob, List<String> lines, List<AutosysAbstractJob> parents) {
		this.visitCommon(autosysSqlAgentJob, lines, parents, (key, value) -> {
			switch (key) {
			case "sqlagent_user_name":
				autosysSqlAgentJob.setSqlAgentUserName(value);
				break;
			case "sqlagent_domain_name":
				autosysSqlAgentJob.setSqlAgentDomainName(value);
				break;
			case "sqlagent_target_db":
				autosysSqlAgentJob.setSqlAgentTargetDb(value);
				break;
			case "sqlagent_jobname":
				autosysSqlAgentJob.setSqlAgentJobname(value);
				break;
			case "sqlagent_server_name":
				autosysSqlAgentJob.setSqlAgentServerName(value);
				break;
			default:
				// no statement hit
				return false;
			}
			return true;
		});
	}

	@Override
	public void visit(AutosysWindowsServiceMonitoringJob autosysWindowsServiceMonitoringJob, List<String> lines, List<AutosysAbstractJob> parents) {
		this.visitCommon(autosysWindowsServiceMonitoringJob, lines, parents, (key, value) -> {
			switch (key) {
			case "monitor_mode":
				autosysWindowsServiceMonitoringJob.setMonitorMode(AutosysWindowsServiceMonitoringJob.MonitorModeType.valueOf(value));
				break;
			case "win_service_name":
				autosysWindowsServiceMonitoringJob.setWinServiceName(value);
				break;
			case "win_service_status":
				autosysWindowsServiceMonitoringJob.setWinServiceStatus(AutosysWindowsServiceMonitoringJob.AutosysWinServiceStatus.valueOf(value));
				break;
			default:
				// no statement hit
				return false;
			}
			return true;
		});
	}

	/*********************************************
	 * Other HELPER methods
	 ******************************************************/

	private AutosysResourceStatement extractResourceStatement(String resourceString) {
		
		if(StringUtils.isBlank(resourceString)) {
			return null;
		}
		
		// resources: (ABS_FACE.RC_BatchControl,QUANTITY=10,FREE=A)
		String[] resourceStringParts = resourceString.substring(resourceString.indexOf("(") + 1, resourceString.indexOf(")")).split(",");
		List<String> statementParts = new LinkedList<>();

		for (int i = 0; i < resourceStringParts.length; i++) {
			String statementPart = resourceStringParts[i].substring(resourceStringParts[i].indexOf("=") + 1);
			if (i == resourceStringParts.length - 1 && resourceStringParts.length > 2) {
				return new AutosysResourceStatement(statementParts.get(0), Integer.parseInt(statementParts.get(1)), statementPart);
			}
			statementParts.add(statementPart);
		}

		return new AutosysResourceStatement(statementParts.get(0), Integer.parseInt(statementParts.get(1)));
	}

	private void fillInDefaultProperties(AutosysAbstractJob job, List<AutosysAbstractJob> parents, String key, String value) {
		if (job.getName().equals("CMC_FACE_6160__ETS_FACE_CMCX_PHPIN_IN")) {
			job.getName();
		}

		switch (key) {
		case "box_name":
			// TODO: Analyze box_name in AUTOSYS documentation for better understanding how
			// it works as parent. Also, does each job MUST have a parent?
			AutosysAbstractJob parent = parents.stream().filter(f -> f.getName().trim().toLowerCase().equals(value.trim().toLowerCase())).findAny().orElse(null);

			if (parent != null) {
				this.model.getBaseObjectById(parent.getId()).addChild(job);
				log.debug("Added Job[{}] to parent[{}]", job.getFullPath(), parent.getFullPath());
			} else {
				log.error("ERROR in locating Parent[{}] for Job[{}]; object.name={}", value, job.getFullPath(), job.getName());
				// TODO: Uncomment later below thrown RuntimeException!
				// throw new RuntimeException("ERROR in locating Parent[" + value + "] for Job["
				// + object.getFullPath() + "]");
			}
			break;
		case "condition":
			job.setCondition(AutosysDependencyParserUtil.processJobExpresionData(job, value));
			break;
		case "machine":
			job.setMachine(value);
			break;
		case "owner":
			job.setOwner(value);
			break;
		case "permission":
			job.setPermission(value);
			break;
		case "date_conditions":
			job.setDateConditions(Integer.parseInt(value) == 1);
			break;
		case "days_of_week":
			job.setDaysOfWeek(value);
			break;
		case "run_calendar":
			job.setRunCalendar(value);
			break;
		case "start_times":
			job.setStartTimes(value);
			break;
		case "start_mins":
			job.setStartMins(value);
			break;
		case "run_window":
			job.setRunWindow(value);
			break;
		case "description":
			job.setDescription(value);
			break;
		case "term_run_time":
			job.setTermRunTime(Integer.parseInt(value));
			break;
		case "box_terminator":
			if (!StringUtils.isBlank(value)) {
				job.setBoxTerminator(AutosysYesNoType.getAutosysYesNoType(value.toUpperCase()));
			} else {
				job.setBoxTerminator(null);
			}
			break;
		case "job_terminator":
			if (!StringUtils.isBlank(value)) {
				job.setJobTerminator(AutosysYesNoType.getAutosysYesNoType(value.toUpperCase()));
			} else {
				job.setJobTerminator(null);
			}
			break;
		case "alarm_if_fail":
			if (!StringUtils.isBlank(value)) {
				job.setAlarmIfFail(AutosysYesNoType.getAutosysYesNoType(value.toUpperCase()));
			} else {
				job.setAlarmIfFail(null);
			}
			break;
		case "job_load": // Does TIDAL currently support (something like) this?
			job.setJobLoad(Integer.parseInt(value));
			break;
		case "priority":
			job.setPriority(Integer.parseInt(value));
			break;
		case "auto_delete":
			job.setAutoDelete(Integer.parseInt(value));
			break;
		case "group":
			job.setGroupAttribute(value);
			break;
		case "application":
			job.setApplicationAttribute(value);
			break;
		case "exclude_calendar":
			job.setExcludeCalendar(value);
			break;
		case "resources":
			job.setResource(value == null ? null : extractResourceStatement(value));
			break;
		case "n_retrys":
			// TODO: Research what does this field do exactly:
			// https://techdocs.broadcom.com/us/en/ca-enterprise-software/intelligent-automation/autosys-workload-automation/12-0-01/reference/ae-job-information-language/jil-job-definitions/n-retrys-attribute-define-the-number-of-times-to-restart-a-job-after-a-failure.html
			job.setNumberRetrys(value);
			break;
		case "status":
			job.setStatus(value);
			break;
		case "max_run_alarm":
			job.setWatchMaxIntervals(value);
			break;
		case "min_run_alarm":
			job.setMinRunAlarm(Integer.parseInt(value));
			break;
		case "alarm_if_terminated":
			if (!StringUtils.isBlank(value)) {
				job.setAlarmIfTerminated(AutosysYesNoType.getAutosysYesNoType(value.toUpperCase()));
			} else {
				job.setAlarmIfTerminated(null);
			}
			break;
		case "must_start_times":
			// TODO: Implement it
			// https://techdocs.broadcom.com/us/en/ca-enterprise-software/intelligent-automation/autosys-workload-automation/12-0-01/reference/ae-job
			// -information-language/jil-job-definitions/must-start-times-attribute-specify-the-time-a-job-must-start-by.html
			job.setMustStartTimes(value);
			break;
		case "must_complete_times":
			job.setMustCompleteTimes(value);
			break;
		case "send_notification":
			if (!StringUtils.isBlank(value)) {
				job.setSendNotification(SendNotificationType.getSendNotificationType(value.toUpperCase()));
			} else {
				job.setSendNotification(null);
			}
			break;
		case "notification_template":
			job.setNotificationTemplate(value);
			break;
		case "auto_hold": // applies only to jobs that are in a BOX job type
			if (!StringUtils.isBlank(value)) {
				job.setAutoHold(AutosysYesNoType.getAutosysYesNoType(value.toUpperCase()));
			} else {
				job.setAutoHold(null);
			}
			break;
		case "notification_msg":
			job.setNotificationMsg(value);
			break;
		case "watch_file_groupname":
		case "fail_codes":
		case "svcdesk_imp":
		case "svcdesk_pri":
			//job.setNotificationMsg(value);
			break;
			
		default:
			throw new IllegalArgumentException("Invalid key: [" + key + "]; job.name=" + job.getName() + "; job.fullPath=" + job.getFullPath());
		}
	}

}
