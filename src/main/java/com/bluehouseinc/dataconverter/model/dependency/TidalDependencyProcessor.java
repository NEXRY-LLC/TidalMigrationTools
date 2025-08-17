package com.bluehouseinc.dataconverter.model.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.BaseCvsDependency;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyFile;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyJob;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.model.dependency.job.DepLogic;
import com.bluehouseinc.tidal.api.model.dependency.job.DependentJobStatus;
import com.bluehouseinc.tidal.api.model.dependency.job.Operator;

import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TidalDependencyProcessor {

	@Getter
	private List<BaseCvsDependency> dependencies = new ArrayList<>();
	private TidalDataModel tidal;

	static TidalDependencyProcessor me;
	
	TidalDependencyProcessor(TidalDataModel tidal) {
		this.tidal = tidal;
	}

	/**
	 * Singlton of our data model so we can get it from everywhere.. New Concept??
	 * 
	 * @param cfgProvider
	 * @return
	 */
	public static TidalDependencyProcessor instance(TidalDataModel model) {

		if (me == null) {
			me = new TidalDependencyProcessor(model);
		}
		return me;
	}

	/**
	 * Get all upstream dependencies for a job (complete dependency chain)
	 * Uses the existing dependencies collection that all parsers populate
	 */
	public Set<BaseCsvJobObject> getUpstreamDependencies(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> upstreamJobs = new LinkedHashSet<>();
		Set<String> visited = new HashSet<>();

		collectUpstreamDependencies(job, upstreamJobs, visited);
		return upstreamJobs;
	}

	/**
	 * Get immediate upstream dependencies (direct dependencies only)
	 */
	public List<BaseCsvJobObject> getImmediateUpstreamDependencies(BaseCsvJobObject job) {
		return getJobDependencies(job).stream().map(CvsDependencyJob::getDependsOnJob).collect(Collectors.toList());
	}

	/**
	 * Get root dependencies (jobs with no upstream dependencies)
	 */
	public Set<BaseCsvJobObject> getRootDependencies(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> allUpstream = getUpstreamDependencies(job);
		return allUpstream.stream().filter(upstreamJob -> getJobDependencies(upstreamJob).isEmpty()).collect(Collectors.toSet());
	}

	/**
	 * Get dependency execution levels for TIDAL scheduling
	 * Returns Map where key = execution level (0 = root jobs) and value = jobs at that level
	 */
	public Map<Integer, Set<BaseCsvJobObject>> getDependencyLevels(BaseCsvJobObject job) {
		Map<Integer, Set<BaseCsvJobObject>> levels = new HashMap<>();
		Set<String> processed = new HashSet<>();

		buildDependencyLevels(job, levels, processed);
		return levels;
	}

	/**
	 * Get dependency levels for ALL jobs in the model
	 * Useful for understanding the entire execution flow across all parsers
	 */
	public Map<Integer, Set<BaseCsvJobObject>> getAllJobDependencyLevels() {
		Map<Integer, Set<BaseCsvJobObject>> allLevels = new HashMap<>();
		Set<String> processed = new HashSet<>();

		// Get all jobs from the model (flattened)
		List<BaseCsvJobObject> allJobs = ObjectUtils.toFlatStream(this.tidal.getJobOrGroups()).collect(Collectors.toList());

		// Process each job to determine its level
		for (BaseCsvJobObject job : allJobs) {
			if (!processed.contains(job.getFullPath())) {
				buildDependencyLevels(job, allLevels, processed);
			}
		}

		return allLevels;
	}

	/**
	 * Check for circular dependencies starting from a specific job
	 */
	public boolean hasCircularDependency(BaseCsvJobObject job) {
		Set<String> visited = new HashSet<>();
		Set<String> recursionStack = new HashSet<>();

		return hasCircularDependencyHelper(job, visited, recursionStack);
	}

	/**
	 * Check for circular dependencies across the entire model
	 * Critical for validating complex migrations from multiple source systems
	 */
	public List<BaseCsvJobObject> findAllCircularDependencies() {
		List<BaseCsvJobObject> circularJobs = new ArrayList<>();
		List<BaseCsvJobObject> allJobs = ObjectUtils.toFlatStream(this.tidal.getJobOrGroups()).collect(Collectors.toList());

		Set<String> alreadyChecked = new HashSet<>();

		for (BaseCsvJobObject job : allJobs) {
			if (!alreadyChecked.contains(job.getFullPath()) && hasCircularDependency(job)) {
				circularJobs.add(job);
				alreadyChecked.add(job.getFullPath());
			}
		}

		return circularJobs;
	}

	/**
	 * Enhanced version of your existing addJobDependencyForJob with circular dependency detection
	 * This extends your existing method with validation
	 */
	public CvsDependencyJob addJobDependencyForJobWithValidation(BaseCsvJobObject myjob, BaseCsvJobObject dependsOnMe, DepLogic logic, Operator operator, DependentJobStatus status, Integer dateOffset) {

		// First check if this would create a circular dependency
		if (wouldCreateCircularDependency(myjob, dependsOnMe)) {
			log.warn("Circular dependency detected: {} -> {}. Dependency not created.", myjob.getFullPath(), dependsOnMe.getFullPath());
			return null;
		}

		// Use your existing method
		return addJobDependencyForJob(myjob, dependsOnMe, logic, operator, status, dateOffset);
	}

	/**
	 * Get comprehensive dependency chain summary for reporting
	 */
	public DependencyChainSummary getDependencyChainSummary(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> upstream = getUpstreamDependencies(job);
		Set<BaseCsvJobObject> roots = getRootDependencies(job);
		Map<Integer, Set<BaseCsvJobObject>> levels = getDependencyLevels(job);

		int maxLevel = levels.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);

		return new DependencyChainSummary(job, upstream.size(), roots.size(), maxLevel + 1, // levels are 0-based
				hasCircularDependency(job), upstream, roots, levels);
	}

	/**
	 * Print dependency chain for debugging - useful during parser development
	 */
	public void printDependencyChain(BaseCsvJobObject job) {
		log.info("=== Dependency Chain for Job: {} ===", job.getFullPath());
		log.info("Job ID: {} | Type: {}", job.getId(), job.getType());

		List<CvsDependencyJob> directDeps = getJobDependencies(job);
		if (!directDeps.isEmpty()) {
			log.info("Direct Dependencies: {}", directDeps.size());
			directDeps.forEach(dep -> log.info("  -> {} (Status: {}, Logic: {})", dep.getDependsOnJob().getFullPath(), dep.getJobStatus(), dep.getLogic()));
		}

		Map<Integer, Set<BaseCsvJobObject>> levels = getDependencyLevels(job);

		for (int level = 0; level <= levels.keySet().stream().mapToInt(Integer::intValue).max().orElse(0); level++) {
			Set<BaseCsvJobObject> jobsAtLevel = levels.get(level);
			if (jobsAtLevel != null && !jobsAtLevel.isEmpty()) {
				String jobsInfo = jobsAtLevel.stream().map(levelJob -> levelJob.getFullPath() + " (" + levelJob.getType() + ")").collect(Collectors.joining(", "));
				log.info("Level {}: {}", level, jobsInfo);
			}
		}

		Set<BaseCsvJobObject> roots = getRootDependencies(job);
		if (!roots.isEmpty()) {
			String rootInfo = roots.stream().map(rootJob -> rootJob.getFullPath() + " (" + rootJob.getType() + ")").collect(Collectors.joining(", "));
			log.info("Root Jobs: {}", rootInfo);
		}

		if (hasCircularDependency(job)) {
			log.warn("CIRCULAR DEPENDENCY DETECTED for job: {}", job.getFullPath());
		}
	}

	/**
	 * Get comprehensive model statistics for migration validation
	 */
	public DependencyModelStatistics getModelStatistics() {
		List<BaseCsvJobObject> allJobs = ObjectUtils.toFlatStream(this.tidal.getJobOrGroups()).collect(Collectors.toList());

		List<CvsDependencyJob> allDependencies = this.dependencies.stream().filter(dep -> dep instanceof CvsDependencyJob).map(dep -> (CvsDependencyJob) dep).collect(Collectors.toList());

		Map<Integer, Set<BaseCsvJobObject>> allLevels = getAllJobDependencyLevels();
		List<BaseCsvJobObject> circularJobs = findAllCircularDependencies();

		long jobsWithDependencies = allJobs.stream().mapToLong(job -> getJobDependencies(job).size() > 0 ? 1 : 0).sum();

		long rootJobs = allJobs.stream().mapToLong(job -> getJobDependencies(job).isEmpty() ? 1 : 0).sum();

		return new DependencyModelStatistics(allJobs.size(), allDependencies.size(), (int) jobsWithDependencies, (int) rootJobs, allLevels.size(), circularJobs.size(), allLevels, circularJobs,this.dependencies.size());
	}

	// ===== PRIVATE HELPER METHODS =====

	/**
	 * Get job dependencies using your existing dependencies collection
	 */
	private List<CvsDependencyJob> getJobDependencies(BaseCsvJobObject job) {
		return this.dependencies.stream().filter(dep -> dep instanceof CvsDependencyJob).map(dep -> (CvsDependencyJob) dep).filter(dep -> Objects.equals(dep.getJobObject().getId(), job.getId())).collect(Collectors.toList());
	}

	private void collectUpstreamDependencies(BaseCsvJobObject currentJob, Set<BaseCsvJobObject> upstreamJobs, Set<String> visited) {
		String currentPath = currentJob.getFullPath();

		if (visited.contains(currentPath)) {
			return; // Prevent infinite loops
		}
		visited.add(currentPath);

		List<CvsDependencyJob> dependencies = getJobDependencies(currentJob);
		for (CvsDependencyJob dependency : dependencies) {
			BaseCsvJobObject targetJob = dependency.getDependsOnJob();
			if (targetJob != null) {
				upstreamJobs.add(targetJob);
				collectUpstreamDependencies(targetJob, upstreamJobs, visited);
			}
		}
	}

	private void buildDependencyLevels(BaseCsvJobObject currentJob, Map<Integer, Set<BaseCsvJobObject>> levels, Set<String> processed) {
		String currentPath = currentJob.getFullPath();

		if (processed.contains(currentPath)) {
			return;
		}

		List<CvsDependencyJob> dependencies = getJobDependencies(currentJob);

		if (dependencies.isEmpty()) {
			// Root job - Level 0
			levels.computeIfAbsent(0, k -> new LinkedHashSet<>()).add(currentJob);
			processed.add(currentPath);
			return;
		}

		// Process all dependencies first
		int maxDependencyLevel = -1;
		for (CvsDependencyJob dependency : dependencies) {
			BaseCsvJobObject targetJob = dependency.getDependsOnJob();
			if (targetJob != null && !processed.contains(targetJob.getFullPath())) {
				buildDependencyLevels(targetJob, levels, processed);
			}

			// Find the level of this dependency
			for (Map.Entry<Integer, Set<BaseCsvJobObject>> levelEntry : levels.entrySet()) {
				if (levelEntry.getValue().contains(targetJob)) {
					maxDependencyLevel = Math.max(maxDependencyLevel, levelEntry.getKey());
					break;
				}
			}
		}

		// Place this job one level after its highest dependency
		int jobLevel = maxDependencyLevel + 1;
		levels.computeIfAbsent(jobLevel, k -> new LinkedHashSet<>()).add(currentJob);
		processed.add(currentPath);
	}

	private boolean hasCircularDependencyHelper(BaseCsvJobObject job, Set<String> visited, Set<String> recursionStack) {
		String currentPath = job.getFullPath();

		if (recursionStack.contains(currentPath)) {
			return true; // Circular dependency found
		}

		if (visited.contains(currentPath)) {
			return false; // Already processed this path
		}

		visited.add(currentPath);
		recursionStack.add(currentPath);

		// Check all dependencies
		List<CvsDependencyJob> dependencies = getJobDependencies(job);
		for (CvsDependencyJob dependency : dependencies) {
			BaseCsvJobObject targetJob = dependency.getDependsOnJob();
			if (targetJob != null && hasCircularDependencyHelper(targetJob, visited, recursionStack)) {
				return true;
			}
		}

		recursionStack.remove(currentPath);
		return false;
	}

	/**
	 * Check if adding a dependency would create a circular dependency
	 * Use this in your addJobDependencyForJob method
	 */
	private boolean wouldCreateCircularDependency(BaseCsvJobObject sourceJob, BaseCsvJobObject targetJob) {
		// If targetJob has sourceJob in its upstream chain, adding this dependency would create a circle
		Set<BaseCsvJobObject> targetUpstream = getUpstreamDependencies(targetJob);
		return targetUpstream.contains(sourceJob);
	}

	
	CvsDependencyJob addJobDependencyForJob(BaseCsvJobObject myjob, BaseCsvJobObject depensonme, DepLogic logic, Operator operator, DependentJobStatus status, Integer dateOffset) {

		CvsDependencyJob dep = new CvsDependencyJob();
		dep.setJobObject(myjob);
		dep.setDependsOnJob(depensonme);
		dep.setOperator(operator);
		dep.setJobStatus(status);
		dep.setLogic(logic);
		dep.setDateOffset(dateOffset);

		this.dependencies.add(dep);

		return dep;
	}

	public CvsDependencyFile addFileDependencyForJob(BaseCsvJobObject myjob, String filename) {

		CvsDependencyFile filedep = new CvsDependencyFile();
		filedep.setJobObject(myjob);
		filedep.setFilename(filename);

		this.dependencies.add(filedep);

		return filedep;
	}

	public CvsDependencyJob addJobDependencyForJobCompletedNormal(BaseCsvJobObject myjob, BaseCsvJobObject depensonme, Integer dateOffset) {
		return this.addJobDependencyForJob(myjob, depensonme, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_NORMAL, dateOffset);
	}

	public CvsDependencyJob addJobDependencyForJobCompleted(BaseCsvJobObject myjob, BaseCsvJobObject depensonme, Integer dateOffset) {
		return this.addJobDependencyForJob(myjob, depensonme, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED, dateOffset);
	}

	/**
	 * Summary data class for dependency chain analysis
	 */
	@Data
	class DependencyChainSummary {
		private final BaseCsvJobObject job;
		private final int totalUpstreamCount;
		private final int rootJobCount;
		private final int executionLevels;
		private final boolean hasCircularDependency;
		private final Set<BaseCsvJobObject> upstreamJobs;
		private final Set<BaseCsvJobObject> rootJobs;
		private final Map<Integer, Set<BaseCsvJobObject>> dependencyLevels;

		// Constructor and getters
		public DependencyChainSummary(BaseCsvJobObject job, int totalUpstreamCount, int rootJobCount, int executionLevels, boolean hasCircularDependency, Set<BaseCsvJobObject> upstreamJobs, Set<BaseCsvJobObject> rootJobs,
				Map<Integer, Set<BaseCsvJobObject>> dependencyLevels) {
			this.job = job;
			this.totalUpstreamCount = totalUpstreamCount;
			this.rootJobCount = rootJobCount;
			this.executionLevels = executionLevels;
			this.hasCircularDependency = hasCircularDependency;
			this.upstreamJobs = upstreamJobs;
			this.rootJobs = rootJobs;
			this.dependencyLevels = dependencyLevels;
		}

		// Add getters as needed...
	}

	/**
	 * Get all downstream dependencies for a job (complete dependency chain)
	 * Returns all jobs that depend on this job, directly or indirectly
	 */
	public Set<BaseCsvJobObject> getDownstreamDependencies(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> downstreamJobs = new LinkedHashSet<>();
		Set<String> visited = new HashSet<>();

		collectDownstreamDependencies(job, downstreamJobs, visited);
		return downstreamJobs;
	}

	/**
	 * Get immediate downstream dependencies (direct dependents only)
	 * Returns jobs that directly depend on this job
	 */
	public List<BaseCsvJobObject> getImmediateDownstreamDependencies(BaseCsvJobObject job) {
		return this.dependencies.stream().filter(dep -> dep instanceof CvsDependencyJob).map(dep -> (CvsDependencyJob) dep).filter(dep -> Objects.equals(dep.getDependsOnJob().getId(), job.getId())).map(CvsDependencyJob::getJobObject)
				.collect(Collectors.toList());
	}

	/**
	 * Get leaf dependencies (jobs that nothing depends on)
	 * These are the final jobs in execution chains
	 */
	public Set<BaseCsvJobObject> getLeafDependencies(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> allDownstream = getDownstreamDependencies(job);
		return allDownstream.stream().filter(downstreamJob -> getImmediateDownstreamDependencies(downstreamJob).isEmpty()).collect(Collectors.toSet());
	}

	/**
	 * Get downstream dependency levels for impact analysis
	 * Returns Map where key = levels away from source job, value = jobs at that level
	 */
	public Map<Integer, Set<BaseCsvJobObject>> getDownstreamDependencyLevels(BaseCsvJobObject job) {
		Map<Integer, Set<BaseCsvJobObject>> levels = new HashMap<>();
		Set<String> processed = new HashSet<>();

		buildDownstreamDependencyLevels(job, levels, processed, 0);
		return levels;
	}

	/**
	 * Get complete dependency impact analysis for a job
	 * Shows both upstream (what this depends on) and downstream (what depends on this)
	 */
	public DependencyImpactAnalysis getDependencyImpactAnalysis(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> upstream = getUpstreamDependencies(job);
		Set<BaseCsvJobObject> downstream = getDownstreamDependencies(job);
		Set<BaseCsvJobObject> roots = getRootDependencies(job);
		Set<BaseCsvJobObject> leaves = getLeafDependencies(job);

		Map<Integer, Set<BaseCsvJobObject>> upstreamLevels = getDependencyLevels(job);
		Map<Integer, Set<BaseCsvJobObject>> downstreamLevels = getDownstreamDependencyLevels(job);

		int maxUpstreamLevel = upstreamLevels.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
		int maxDownstreamLevel = downstreamLevels.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);

		return new DependencyImpactAnalysis(job, upstream.size(), downstream.size(), roots.size(), leaves.size(), maxUpstreamLevel + 1, maxDownstreamLevel + 1, upstream, downstream, roots, leaves, upstreamLevels, downstreamLevels);
	}

	/**
	 * Find all orphaned jobs (jobs with no upstream or downstream dependencies)
	 */
	public List<BaseCsvJobObject> findOrphanedJobs() {
		List<BaseCsvJobObject> allJobs = ObjectUtils.toFlatStream(this.tidal.getJobOrGroups()).collect(Collectors.toList());

		return allJobs.stream().filter(job -> getJobDependencies(job).isEmpty() && getImmediateDownstreamDependencies(job).isEmpty()).collect(Collectors.toList());
	}

	/**
	 * Find all terminal jobs (leaf jobs across the entire model)
	 */
	public List<BaseCsvJobObject> findAllTerminalJobs() {
		List<BaseCsvJobObject> allJobs = ObjectUtils.toFlatStream(this.tidal.getJobOrGroups()).collect(Collectors.toList());

		return allJobs.stream().filter(job -> getImmediateDownstreamDependencies(job).isEmpty()).collect(Collectors.toList());
	}

	/**
	 * Print downstream dependency chain for impact analysis
	 */
	public void printDownstreamDependencyChain(BaseCsvJobObject job) {
		log.info("=== Downstream Dependency Chain for Job: {} ===", job.getFullPath());
		log.info("Job ID: {} | Type: {}", job.getId(), job.getType());

		List<BaseCsvJobObject> directDownstream = getImmediateDownstreamDependencies(job);
		if (!directDownstream.isEmpty()) {
			log.info("Direct Dependents: {}", directDownstream.size());
			directDownstream.forEach(dep -> log.info("  <- {} (Type: {})", dep.getFullPath(), dep.getType()));
		}

		Map<Integer, Set<BaseCsvJobObject>> levels = getDownstreamDependencyLevels(job);

		for (int level = 1; level <= levels.keySet().stream().mapToInt(Integer::intValue).max().orElse(0); level++) {
			Set<BaseCsvJobObject> jobsAtLevel = levels.get(level);
			if (jobsAtLevel != null && !jobsAtLevel.isEmpty()) {
				String jobsInfo = jobsAtLevel.stream().map(levelJob -> levelJob.getFullPath() + " (" + levelJob.getType() + ")").collect(Collectors.joining(", "));
				log.info("Downstream Level {}: {}", level, jobsInfo);
			}
		}

		Set<BaseCsvJobObject> leaves = getLeafDependencies(job);
		if (!leaves.isEmpty()) {
			String leafInfo = leaves.stream().map(leafJob -> leafJob.getFullPath() + " (" + leafJob.getType() + ")").collect(Collectors.joining(", "));
			log.info("Terminal Jobs: {}", leafInfo);
		}
	}


	// ===== PRIVATE HELPER METHODS =====

	private void collectDownstreamDependencies(BaseCsvJobObject currentJob, Set<BaseCsvJobObject> downstreamJobs, Set<String> visited) {
		String currentPath = currentJob.getFullPath();

		if (visited.contains(currentPath)) {
			return; // Prevent infinite loops
		}
		visited.add(currentPath);

		List<BaseCsvJobObject> dependents = getImmediateDownstreamDependencies(currentJob);
		for (BaseCsvJobObject dependent : dependents) {
			if (dependent != null) {
				downstreamJobs.add(dependent);
				collectDownstreamDependencies(dependent, downstreamJobs, visited);
			}
		}
	}

	private void buildDownstreamDependencyLevels(BaseCsvJobObject currentJob, Map<Integer, Set<BaseCsvJobObject>> levels, Set<String> processed, int currentLevel) {
		String currentPath = currentJob.getFullPath();

		if (processed.contains(currentPath)) {
			return;
		}

		levels.computeIfAbsent(currentLevel, k -> new LinkedHashSet<>()).add(currentJob);
		processed.add(currentPath);

		List<BaseCsvJobObject> dependents = getImmediateDownstreamDependencies(currentJob);
		for (BaseCsvJobObject dependent : dependents) {
			if (dependent != null && !processed.contains(dependent.getFullPath())) {
				buildDownstreamDependencyLevels(dependent, levels, processed, currentLevel + 1);
			}
		}
	}


}
