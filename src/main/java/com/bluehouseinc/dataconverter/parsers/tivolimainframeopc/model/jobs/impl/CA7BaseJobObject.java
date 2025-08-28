package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.CA7JobNameParser;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.CA7JobNameParser.ParsedJobName;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.CA7Resource;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.CA7Schedule;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.CA7UserField;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class CA7BaseJobObject extends BaseJobOrGroupObject {

	private ParsedJobName nameParser;
	
	private LocalDate validFrom;
	private String calendar;
	private String description;
	private String type;
	private String group;
	private int priority;
	private String status;
	
	private int operationNumber;
	private String workstationId;
	private String category;
	private boolean conditionalJob;
	private boolean noOperation;
	private boolean manualHold;
	private int duration;
	private int highReturnCode;
	private boolean autoExecuteComplete;
	private int startDay;
	private String startTime;
	private boolean autoJobSubmit;
	private boolean autoJobRestart;
	private boolean timeRestricted;
	private boolean cancelLate;
	private int processingStation;
	private boolean operatorWriteToOperator;
	private boolean monitor;
	private boolean jobCreateRequired;
	private boolean userSystem;
	private boolean expandJCL;
	private boolean useExternalName;
	private boolean useExternalSchedulingEngine;
	private boolean useSAI;
	private int smoothing;
	private int limitFeedback;
	private boolean cScript; //CSCRIPT
	private String owner;

	List<CA7Dependency> dependencies = new ArrayList<>();
	List<CA7UserField> userFields = new ArrayList<>();
	List<CA7Resource> resources = new ArrayList<>();

	private List<CA7Schedule> schedules = new ArrayList<>();
	
	
	private String runtimeUsePassword;
	private String runtimeInteractive;
	private String runtimeUserName;

	private String runtimeUser;
	private String commandLineData;

	private String javaBatchXML;
	private JobType jobType = JobType.APPLICATION;

	public void addDependency(CA7Dependency dependency) {
		this.dependencies.add(dependency);
	}

	public void addUserField(CA7UserField dependency) {
		this.userFields.add(dependency);
	}

	public void addResource(CA7Resource resource) {
		this.resources.add(resource);
	}

	public enum JobType {
		CA7,LIBERTY, END, SCRIPT, APPLICATION
	}

	@Override
	public boolean isGroup() {
		return this.getChildren().isEmpty();
	}


	public void addSchedule(CA7Schedule sched) {
		this.schedules.add(sched);
	}

	public void addResource(CA7Resource res, CA7BaseJobObject job) {
		if (res != null) {
			if (job != null) {
				job.addResource(res);
			} else {
				res.getResourceName();
				this.resources.add(res); // Just for simpler processing but might not be needed.

			}
		}

	}

	public void addDeppendency(CA7Dependency dep, CA7BaseJobObject job) {
		job.addDependency(dep);
	}

	public CA7BaseJobObject findJobByOpNo(int opNo) {
		return (CA7BaseJobObject) children.stream().filter(job -> ((CA7BaseJobObject) job).getOperationNumber() == opNo).findFirst().orElse(null);
	}
}
