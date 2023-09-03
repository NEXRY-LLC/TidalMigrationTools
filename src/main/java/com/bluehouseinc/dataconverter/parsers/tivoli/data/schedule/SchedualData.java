package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule;

import java.util.LinkedList;
import java.util.List;

import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.CpuData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.TivoliCPUProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.job.JobScheduleData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOn;

import lombok.Data;


@Data
public class SchedualData {
	
	private String description;
	private String groupName;
	private String workflowName;
	
	//ON RUNCYCLE RULE1 "FREQ=WEEKLY;BYDAY=TH"
	//ON REQUEST
	private List<RunOn> runOn; 
	//EXCEPT RUNCYCLE CALENDAR2 POSHDAYS
	private List<String> exceptOn;
	
	private JobRunTime atTime;
	private JobRunTime untilTime;
	
	private boolean critical = false;

	
	// Looks like a resource to me 
	private List<NeedsResource> needs; //NEEDS 1 AMFINAN1#BONUS2DF

	// This version is a group, follow all jobs that match this name. 
	//FOLLOWS AMFINAN1#LNCLOSE.@ 
	// Follow this one object. 
	//FOLLOWS AMFINAN1#OBDLYLAW.BSTRTLL 
	private List<JobFollows> follows;
	
	private List<JobScheduleData> jobScheduleData;
	
	private List<String> filedepData;
	
	private CpuData cpuData;
	
	public SchedualData(){
		this.needs = new LinkedList<>();
		this.runOn = new LinkedList<>();
		this.follows = new LinkedList<>();
		this.jobScheduleData = new LinkedList<>();
		this.filedepData = new LinkedList<>();
		this.exceptOn = new LinkedList<>();		
	}
}
