package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule;

import lombok.Data;

@Data
public class JobFollows {
	String inGroup;
	String jobToFollow;
	boolean previousDay = false;
	boolean dependsOnGroup = false;
	
}
