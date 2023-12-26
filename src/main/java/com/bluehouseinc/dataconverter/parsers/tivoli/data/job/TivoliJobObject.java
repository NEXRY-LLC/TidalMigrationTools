package com.bluehouseinc.dataconverter.parsers.tivoli.data.job;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.CpuData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.ResourceData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.SchedualData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.job.JobScheduleData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TivoliJobObject extends BaseJobOrGroupObject {
	private boolean isGroupFlag = false;
	
	private CpuData cpuData;
	private ResourceData resourceData;
	private String doCommand;
	private String streamLogon;
	private String description;
	private TaskType taskType;
	private String recovery;
	private String returnCodeSucess;
	private String scriptName;
	private String afterJob;
	private String abendPrompt;
	private boolean operatorRelease = false; // Priority = 0
	/*
	 * This object represents the tivoli schedule details in the schedule file. AKA everyting before the defining 
	 * workflow that contains all our job schedule details. 
	 * 
	 * Logically , a group will have schedule data and jobs job schedule detail
	 */
	private SchedualData schedualData; 
	private JobScheduleData jobScheduleData;
	
	@Override
	public boolean isGroup() {
		if (!this.getChildren().isEmpty()) {
			isGroupFlag = true;
		}
		return isGroupFlag;
	}

	public enum TaskType {
		UNIX, WINDOWS, UNIT
	}
}
