package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.job;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.JobFollows;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.JobRunTime;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.NeedsResource;

import lombok.Data;

@Data
public class JobScheduleData {
	List<JobFollows> follows = new LinkedList<>();
	
	TivoliJobObject job;
	String groupName;
	JobRunTime atTime;
	JobRunTime untilTime;
	String priority;
	Integer rerunEvery;
	//JobRunTime deadline;
	String fileDep;
	boolean critical = false;
	private List<NeedsResource> needs = new ArrayList<>();
}
