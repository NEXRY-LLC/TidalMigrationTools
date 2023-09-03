package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule;

import lombok.Data;

@Data
public class JobFollows {
	
	String inContainer;
	String inWorkflow;
	String jobToFollow;
	boolean previousDay = false;
	boolean dependsOnGroup = false;
	boolean islocal = true;
	boolean completedOnlyLogic = false;
}
