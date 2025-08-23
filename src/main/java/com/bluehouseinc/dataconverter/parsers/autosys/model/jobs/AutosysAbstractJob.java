package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs;

import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl.AutosysBoxJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.util.AutosysYesNoType;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.util.SendNotificationType;
import com.bluehouseinc.dataconverter.parsers.autosys.model.statements.AutosysResourceStatement;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public abstract class AutosysAbstractJob extends BaseJobOrGroupObject implements AutosysJob {

	protected String applicationAttribute; // analogue to Job Class (`application` attribute is subset of `group` attribute)
	protected int autoDelete;
	protected AutosysYesNoType autoHold; // sets job status to ON_HOLD when the status of the containing BOX job changes to RUNNING. Valid only if `box_name`
	// attribute is present
	protected AutosysYesNoType alarmIfFail;
	protected AutosysYesNoType alarmIfTerminated;
	protected AutosysYesNoType boxTerminator;
	protected String excludeCalendar; // by calendar name
	protected String groupAttribute; // analogue to Job Class (`application` attribute is subset of `group` attribute)
	protected int jobLoad;
	protected String machine;
	protected int minRunAlarm; // in minutes
	protected String notificationMsg;
	protected String owner;
	protected String permission;
	protected boolean dateConditions;
	protected String daysOfWeek;
	protected AutosysYesNoType jobTerminator; // Can contain Y (1 instead of Y) ORR N (0 instead of N)
	protected String runCalendar;
	protected SendNotificationType sendNotification;
	protected String mustCompleteTimes;
	protected String mustStartTimes;
	protected String startTimes;
	protected String startMins; // start_mins: 05,10,15,20,25,30,35,40,45,50 , every minutes after hour... With CCI this simply  means minutes after each hour.
	protected String status;
	protected String runWindow; // run_window need to code for this one. run_window: "05:00 - 17:05"
	protected String description;
	protected String notificationTemplate;
	protected String numberRetrys;
	protected int priority;
	protected AutosysResourceStatement resource;
	protected int termRunTime; // max allowed time in MINUTES for job to run. Otherwise, it terminates (seems to be analogue to SLA in TIDAL).
	protected String watchMaxIntervals; //max_run_alarm
	protected List<String> notificationAlarmTypesList;
	protected List<String> notificationEmailAddressList;
	protected List<String> notificationEmailAddressOnSuccessList;// notification_emailaddress_on_success;
	protected List<String> notificationEmailAddressOnAlarmList;
	protected List<String> notificationEmailAddressOnTerminated;
	protected List<String> notificationEmailAddressOnFailureList;
	protected String condition;
	boolean complexTimeSetup = false;
	
	public AutosysAbstractJob(String name) {
		this.name = name;
	}

	@Override
	public boolean isGroup() {
		boolean tf = this instanceof AutosysBoxJob;
		return tf;
	}

}
