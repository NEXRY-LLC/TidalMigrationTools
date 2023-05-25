package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.job;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.JobFollows;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.JobRunTime;

import lombok.Data;

@Data
public class JobScheduleDetail {
	List<JobFollows> follows = new ArrayList<>();
	
	String jobName;
	String groupName;
	String atTime;
	String untilTime;
	String priority;
	String rerunEvery;
	JobRunTime deadline;
	boolean critical = false;
}
