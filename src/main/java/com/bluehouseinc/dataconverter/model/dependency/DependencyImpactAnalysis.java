package com.bluehouseinc.dataconverter.model.dependency;

import java.util.Map;
import java.util.Set;

import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;

/**
 * Impact analysis data class
 */

public class DependencyImpactAnalysis {

	private final BaseCsvJobObject job;
	private final int upstreamCount;
	private final int downstreamCount;
	private final int rootCount;
	private final int leafCount;
	private final int upstreamLevels;
	private final int downstreamLevels;
	private final Set<BaseCsvJobObject> upstreamJobs;
	private final Set<BaseCsvJobObject> downstreamJobs;
	private final Set<BaseCsvJobObject> rootJobs;
	private final Set<BaseCsvJobObject> leafJobs;
	private final Map<Integer, Set<BaseCsvJobObject>> upstreamLevelMap;
	private final Map<Integer, Set<BaseCsvJobObject>> downstreamLevelMap;

	public DependencyImpactAnalysis(BaseCsvJobObject job, int upstreamCount, int downstreamCount, int rootCount, int leafCount, int upstreamLevels, int downstreamLevels, Set<BaseCsvJobObject> upstreamJobs, Set<BaseCsvJobObject> downstreamJobs,
			Set<BaseCsvJobObject> rootJobs, Set<BaseCsvJobObject> leafJobs, Map<Integer, Set<BaseCsvJobObject>> upstreamLevelMap, Map<Integer, Set<BaseCsvJobObject>> downstreamLevelMap) {
		this.job = job;
		this.upstreamCount = upstreamCount;
		this.downstreamCount = downstreamCount;
		this.rootCount = rootCount;
		this.leafCount = leafCount;
		this.upstreamLevels = upstreamLevels;
		this.downstreamLevels = downstreamLevels;
		this.upstreamJobs = upstreamJobs;
		this.downstreamJobs = downstreamJobs;
		this.rootJobs = rootJobs;
		this.leafJobs = leafJobs;
		this.upstreamLevelMap = upstreamLevelMap;
		this.downstreamLevelMap = downstreamLevelMap;
	}

	// Getters
	public BaseCsvJobObject getJob() {
		return job;
	}

	public int getUpstreamCount() {
		return upstreamCount;
	}

	public int getDownstreamCount() {
		return downstreamCount;
	}

	public int getRootCount() {
		return rootCount;
	}

	public int getLeafCount() {
		return leafCount;
	}

	public int getUpstreamLevels() {
		return upstreamLevels;
	}

	public int getDownstreamLevels() {
		return downstreamLevels;
	}

	public Set<BaseCsvJobObject> getUpstreamJobs() {
		return upstreamJobs;
	}

	public Set<BaseCsvJobObject> getDownstreamJobs() {
		return downstreamJobs;
	}

	public Set<BaseCsvJobObject> getRootJobs() {
		return rootJobs;
	}

	public Set<BaseCsvJobObject> getLeafJobs() {
		return leafJobs;
	}

	public Map<Integer, Set<BaseCsvJobObject>> getUpstreamLevelMap() {
		return upstreamLevelMap;
	}

	public Map<Integer, Set<BaseCsvJobObject>> getDownstreamLevelMap() {
		return downstreamLevelMap;
	}
}
