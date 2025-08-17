package com.bluehouseinc.dataconverter.model.dependency;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;

import lombok.Data;

import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

/**
 * Statistics data class for the entire dependency model
 */

@Data
public class DependencyModelStatistics {
	final int totalJobs;
	final int totalDependencies;
	final int totalJobDependencies;
	final int standAloneJobs;
	final int executionLevels;
	final int circularDependencyCount;
	final Map<Integer, Set<BaseCsvJobObject>> allExecutionLevels;
	final List<BaseCsvJobObject> circularDependencyJobs;
	final int totalFileDependencies;
	
	public DependencyModelStatistics(int totalJobs, int totalDependencies, int jobsWithDependencies, int standAloneJobs, int executionLevels, int circularDependencyCount, Map<Integer, Set<BaseCsvJobObject>> allExecutionLevels,
			List<BaseCsvJobObject> circularDependencyJobs, int filedependencies) {
		this.totalJobs = totalJobs;
		this.totalDependencies = totalDependencies;
		this.totalJobDependencies = jobsWithDependencies;
		this.standAloneJobs = standAloneJobs;
		this.executionLevels = executionLevels;
		this.circularDependencyCount = circularDependencyCount;
		this.allExecutionLevels = allExecutionLevels;
		this.circularDependencyJobs = circularDependencyJobs;
		this.totalFileDependencies = filedependencies;
	}


}
