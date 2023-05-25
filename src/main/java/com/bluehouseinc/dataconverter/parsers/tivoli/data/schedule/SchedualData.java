package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.job.JobScheduleDetail;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOn;

import lombok.Data;


@Data
public class SchedualData {
	
	private String description;
	private String groupName;
	private String name;
	
	//ON RUNCYCLE RULE1 "FREQ=WEEKLY;BYDAY=TH"
	//ON REQUEST
	private List<RunOn> runOn; 
	private JobRunTime atTime;
	
	private boolean critical = false;
	private JobRunTime deadline;
	
	// Looks like a resource to me 
	private List<NeedsResource> needs; //NEEDS 1 AMFINAN1#BONUS2DF

	// This version is a group, follow all jobs that match this name. 
	//FOLLOWS AMFINAN1#LNCLOSE.@ 
	// Follow this one object. 
	//FOLLOWS AMFINAN1#OBDLYLAW.BSTRTLL 
	private List<JobFollows> follows;
	
	private List<JobScheduleDetail> jobScheduleDetail;
	

	public SchedualData(){
		this.needs = new LinkedList<>();
		this.runOn = new LinkedList<>();
		this.follows = new LinkedList<>();
		this.jobScheduleDetail = new LinkedList<>();
	}
}
