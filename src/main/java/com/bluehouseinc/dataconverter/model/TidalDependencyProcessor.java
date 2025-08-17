package com.bluehouseinc.dataconverter.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.dependency.DependencyImpactAnalysis;
import com.bluehouseinc.dataconverter.model.dependency.DependencyModelStatistics;
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

	// High-performance registry maps for O(1) lookups
	private Map<Integer, DependenciesRef> upstreamRegistry = new HashMap<>();
	private Map<Integer, DependenciesRef> downstreamRegistry = new HashMap<>();
	private Map<Integer, List<String>> fileDependencyRegistry = new HashMap<>();

	// Pre-computed statistics for fast access
	private Map<Integer, Set<BaseCsvJobObject>> cachedAllLevels = new HashMap<>();
	private DependencyModelStatistics cachedModelStatistics;
	private List<BaseCsvJobObject> cachedCircularJobs = new ArrayList<>();
	private Map<Integer, Integer> cachedJobLevels = new HashMap<>();

	static TidalDependencyProcessor me;

	TidalDependencyProcessor(TidalDataModel tidal) {
		this.tidal = tidal;
	}

	/**
	 * Singleton instance
	 */
	public static TidalDependencyProcessor instance(TidalDataModel model) {
		if (me == null) {
			me = new TidalDependencyProcessor(model);
		}
		return me;
	}

	/**
	 * Dependencies reference class for storing both IDs and objects
	 */
	@Data
	public static class DependenciesRef {
		private List<Integer> jobIds = new ArrayList<>();
		private List<BaseCsvJobObject> jobs = new ArrayList<>();

		public void addJob(Integer jobId, BaseCsvJobObject job) {
			jobIds.add(jobId);
			jobs.add(job);
		}

		public boolean isEmpty() {
			return jobIds.isEmpty();
		}

		public int size() {
			return jobIds.size();
		}
	}

	/**
	 * Enhanced version with circular dependency detection and registry updates
	 * All convenience methods automatically get performance benefits through this base method
	 */
	public CvsDependencyJob addJobDependencyForJob(BaseCsvJobObject myjob, BaseCsvJobObject dependsOnMe, DepLogic logic, Operator operator, DependentJobStatus status, Integer dateOffset) {

		// 1. Check for circular dependency using existing registries
		if (wouldCreateCircularDependency(myjob.getId(), dependsOnMe.getId())) {
			log.debug("Circular dependency detected: {} -> {} - Dependency not created.", myjob.getFullPath(), dependsOnMe.getFullPath());
			return null;
		}

		// 2. Create dependency object and add to master list
		CvsDependencyJob dep = new CvsDependencyJob();
		dep.setJobObject(myjob);
		dep.setDependsOnJob(dependsOnMe);
		dep.setOperator(operator);
		dep.setJobStatus(status);
		dep.setLogic(logic);
		dep.setDateOffset(dateOffset);

		this.dependencies.add(dep);

		// 3. Update registries
		updateJobRegistries(myjob.getId(), myjob, dependsOnMe.getId(), dependsOnMe);

		return dep;
	}

	/**
	 * Add file dependency with separate tracking
	 */
	public CvsDependencyFile addFileDependencyForJob(BaseCsvJobObject myjob, String filename) {
		CvsDependencyFile filedep = new CvsDependencyFile();
		filedep.setJobObject(myjob);
		filedep.setFilename(filename);

		this.dependencies.add(filedep);

		// Track file dependency separately
		fileDependencyRegistry.computeIfAbsent(myjob.getId(), k -> new ArrayList<>()).add(filename);

		return filedep;
	}

	/**
	 * Fast upstream dependencies lookup using registry
	 */
	public Set<BaseCsvJobObject> getUpstreamDependencies(BaseCsvJobObject job) {
		return getAllUpstreamJobs(job);
	}

	/**
	 * Fast downstream dependencies lookup using registry
	 */
	public Set<BaseCsvJobObject> getDownstreamDependencies(BaseCsvJobObject job) {
		return getAllDownstreamJobs(job);
	}

	/**
	 * Fast immediate upstream dependencies
	 */
	public List<BaseCsvJobObject> getImmediateUpstreamDependencies(BaseCsvJobObject job) {
		DependenciesRef upstreamRef = upstreamRegistry.get(job.getId());
		return upstreamRef != null ? new ArrayList<>(upstreamRef.getJobs()) : Collections.emptyList();
	}

	/**
	 * Fast immediate downstream dependencies
	 */
	public List<BaseCsvJobObject> getImmediateDownstreamDependencies(BaseCsvJobObject job) {
		DependenciesRef downstreamRef = downstreamRegistry.get(job.getId());
		return downstreamRef != null ? new ArrayList<>(downstreamRef.getJobs()) : Collections.emptyList();
	}

	/**
	 * Fast root dependencies lookup
	 */
	public Set<BaseCsvJobObject> getRootDependencies(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> allUpstream = getAllUpstreamJobs(job);
		return allUpstream.stream().filter(upstreamJob -> upstreamRegistry.get(upstreamJob.getId()) == null).collect(Collectors.toSet());
	}

	/**
	 * Fast leaf dependencies lookup
	 */
	public Set<BaseCsvJobObject> getLeafDependencies(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> allDownstream = getAllDownstreamJobs(job);
		return allDownstream.stream().filter(downstreamJob -> downstreamRegistry.get(downstreamJob.getId()) == null).collect(Collectors.toSet());
	}

	/**
	 * Fast dependency levels lookup using pre-computed cache
	 */
	public Map<Integer, Set<BaseCsvJobObject>> getDependencyLevels(BaseCsvJobObject job) {
		Map<Integer, Set<BaseCsvJobObject>> jobLevels = new HashMap<>();
		Set<BaseCsvJobObject> allUpstream = getAllUpstreamJobs(job);
		allUpstream.add(job); // Include the job itself

		for (BaseCsvJobObject upstreamJob : allUpstream) {
			Integer level = cachedJobLevels.get(upstreamJob.getId());
			if (level != null) {
				jobLevels.computeIfAbsent(level, k -> new LinkedHashSet<>()).add(upstreamJob);
			}
		}

		return jobLevels;
	}

	/**
	 * Fast all job dependency levels using pre-computed cache
	 */
	public Map<Integer, Set<BaseCsvJobObject>> getAllJobDependencyLevels() {
		return new HashMap<>(cachedAllLevels);
	}

	/**
	 * Fast circular dependency check using cached results
	 */
	public boolean hasCircularDependency(BaseCsvJobObject job) {
		return cachedCircularJobs.contains(job);
	}

	/**
	 * Fast all circular dependencies using cached results
	 */
	public List<BaseCsvJobObject> findAllCircularDependencies() {
		return new ArrayList<>(cachedCircularJobs);
	}

	/**
	 * Fast model statistics using cached results
	 */
	public DependencyModelStatistics getModelStatistics() {
		return cachedModelStatistics;
	}

	/**
	 * Get file dependencies for a job
	 */
	public List<String> getFileDependencies(BaseCsvJobObject job) {
		List<String> files = fileDependencyRegistry.get(job.getId());
		return files != null ? new ArrayList<>(files) : Collections.emptyList();
	}

	/**
	 * Get complete upstream job chain using registry traversal
	 */
	public Set<BaseCsvJobObject> getAllUpstreamJobs(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> allUpstream = new LinkedHashSet<>();
		Set<Integer> visited = new HashSet<>();
		traverseUpstream(job.getId(), allUpstream, visited);
		return allUpstream;
	}

	/**
	 * Get complete downstream job chain using registry traversal
	 */
	public Set<BaseCsvJobObject> getAllDownstreamJobs(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> allDownstream = new LinkedHashSet<>();
		Set<Integer> visited = new HashSet<>();
		traverseDownstream(job.getId(), allDownstream, visited);
		return allDownstream;
	}

	/**
	 * Enhanced dependency chain summary using fast lookups
	 */
	public DependencyChainSummary getDependencyChainSummary(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> upstream = getAllUpstreamJobs(job);
		Set<BaseCsvJobObject> roots = getRootDependencies(job);
		Map<Integer, Set<BaseCsvJobObject>> levels = getDependencyLevels(job);

		int maxLevel = levels.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);

		return new DependencyChainSummary(job, upstream.size(), roots.size(), maxLevel + 1, hasCircularDependency(job), upstream, roots, levels);
	}

	/**
	 * Complete dependency impact analysis
	 */
	public DependencyImpactAnalysis getDependencyImpactAnalysis(BaseCsvJobObject job) {
		Set<BaseCsvJobObject> upstream = getAllUpstreamJobs(job);
		Set<BaseCsvJobObject> downstream = getAllDownstreamJobs(job);
		Set<BaseCsvJobObject> roots = getRootDependencies(job);
		Set<BaseCsvJobObject> leaves = getLeafDependencies(job);

		Map<Integer, Set<BaseCsvJobObject>> upstreamLevels = getDependencyLevels(job);
		Map<Integer, Set<BaseCsvJobObject>> downstreamLevels = getDownstreamDependencyLevels(job);

		int maxUpstreamLevel = upstreamLevels.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
		int maxDownstreamLevel = downstreamLevels.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);

		return new DependencyImpactAnalysis(job, upstream.size(), downstream.size(), roots.size(), leaves.size(), maxUpstreamLevel + 1, maxDownstreamLevel + 1, upstream, downstream, roots, leaves, upstreamLevels, downstreamLevels);
	}

	/**
	 * Get downstream dependency levels for impact analysis
	 */
	public Map<Integer, Set<BaseCsvJobObject>> getDownstreamDependencyLevels(BaseCsvJobObject job) {
		Map<Integer, Set<BaseCsvJobObject>> levels = new HashMap<>();
		Set<String> processed = new HashSet<>();
		buildDownstreamDependencyLevels(job, levels, processed, 0);
		return levels;
	}

	/**
	 * Find standalone jobs (no dependencies in either direction)
	 */
	public List<BaseCsvJobObject> findStandLoneJobs() {
		List<BaseCsvJobObject> allJobs = ObjectUtils.toFlatStream(this.tidal.getJobOrGroups()).collect(Collectors.toList());

		return allJobs.stream().filter(job -> upstreamRegistry.get(job.getId()) == null && downstreamRegistry.get(job.getId()) == null).collect(Collectors.toList());
	}

	/**
	 * Find all terminal jobs (leaf jobs across entire model)
	 */
	public List<BaseCsvJobObject> findAllFinalJobDepJobs() {
		List<BaseCsvJobObject> allJobs = ObjectUtils.toFlatStream(this.tidal.getJobOrGroups()).collect(Collectors.toList());

		return allJobs.stream().filter(job -> downstreamRegistry.get(job.getId()) == null).collect(Collectors.toList());
	}

	/**
	 * Print dependency chain for debugging
	 */
	public void printDependencyChain(BaseCsvJobObject job) {
		log.info("=== Dependency Chain for Job: {} ===", job.getFullPath());
		log.info("Job ID: {} | Type: {}", job.getId(), job.getType());

		List<BaseCsvJobObject> directDeps = getImmediateUpstreamDependencies(job);
		if (!directDeps.isEmpty()) {
			log.info("Direct Dependencies: {}", directDeps.size());
			directDeps.forEach(dep -> log.info("  -> {} (Type: {})", dep.getFullPath(), dep.getType()));
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

	// ===== FAST COUNT METHODS =====

	public long getUpstreamJobCounts() {
		return downstreamRegistry.size();
	}

	public long getDownstreamJobCounts() {
		return upstreamRegistry.size();
	}

	public long getNoDependencyCounts() {
		return upstreamRegistry.size();
	}

	// ===== EXISTING METHODS PRESERVED =====

	/**
	 * Get job dependencies using master dependencies list (unchanged)
	 */
	public List<CvsDependencyJob> getJobDependencies(BaseCsvJobObject job) {
		return this.dependencies.stream().filter(dep -> dep instanceof CvsDependencyJob).map(dep -> (CvsDependencyJob) dep).filter(dep -> Objects.equals(dep.getJobObject().getId(), job.getId())).collect(Collectors.toList());
	}

	// ===== CONVENIENCE METHODS (AUTOMATICALLY GET PERFORMANCE BENEFITS) =====

	public CvsDependencyJob addJobDependencyForJobCompletedNormal(BaseCsvJobObject myjob, BaseCsvJobObject dependsOnMe, Integer dateOffset) {
		return this.addJobDependencyForJob(myjob, dependsOnMe, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_NORMAL, dateOffset);
	}

	public CvsDependencyJob addJobDependencyForJobCompleted(BaseCsvJobObject myjob, BaseCsvJobObject dependsOnMe, Integer dateOffset) {
		return this.addJobDependencyForJob(myjob, dependsOnMe, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED, dateOffset);
	}

	// ===== PRIVATE HELPER METHODS =====

	/**
	 * Multi-level circular dependency detection using registry traversal
	 */
	private boolean wouldCreateCircularDependency(Integer sourceJobId, Integer targetJobId) {
		return canReachUpstream(targetJobId, sourceJobId, new HashSet<>());
	}

	private boolean canReachUpstream(Integer fromJobId, Integer targetJobId, Set<Integer> visited) {
		if (fromJobId.equals(targetJobId))
			return true;
		if (visited.contains(fromJobId))
			return false;

		visited.add(fromJobId);

		DependenciesRef upstreamRef = upstreamRegistry.get(fromJobId);
		if (upstreamRef != null) {
			for (Integer upstreamJobId : upstreamRef.getJobIds()) {
				if (canReachUpstream(upstreamJobId, targetJobId, visited)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Update job registries with new relationship
	 */
	private void updateJobRegistries(Integer sourceJobId, BaseCsvJobObject sourceJob, Integer targetJobId, BaseCsvJobObject targetJob) {

		// Update upstream registry: sourceJob depends on targetJob
		upstreamRegistry.computeIfAbsent(sourceJobId, k -> new DependenciesRef()).addJob(targetJobId, targetJob);

		// Update downstream registry: targetJob is depended on by sourceJob
		downstreamRegistry.computeIfAbsent(targetJobId, k -> new DependenciesRef()).addJob(sourceJobId, sourceJob);
	}

	/**
	 * Traverse upstream dependencies using registry
	 */
	private void traverseUpstream(Integer jobId, Set<BaseCsvJobObject> result, Set<Integer> visited) {
		if (visited.contains(jobId))
			return;
		visited.add(jobId);

		DependenciesRef upstreamRef = upstreamRegistry.get(jobId);
		if (upstreamRef != null) {
			for (BaseCsvJobObject job : upstreamRef.getJobs()) {
				result.add(job);
				traverseUpstream(job.getId(), result, visited);
			}
		}
	}

	/**
	 * Traverse downstream dependencies using registry
	 */
	private void traverseDownstream(Integer jobId, Set<BaseCsvJobObject> result, Set<Integer> visited) {
		if (visited.contains(jobId))
			return;
		visited.add(jobId);

		DependenciesRef downstreamRef = downstreamRegistry.get(jobId);
		if (downstreamRef != null) {
			for (BaseCsvJobObject job : downstreamRef.getJobs()) {
				result.add(job);
				traverseDownstream(job.getId(), result, visited);
			}
		}
	}

	/**
	 * Build downstream dependency levels
	 */
	private void buildDownstreamDependencyLevels(BaseCsvJobObject currentJob, Map<Integer, Set<BaseCsvJobObject>> levels, Set<String> processed, int currentLevel) {

		String currentPath = currentJob.getFullPath();
		if (processed.contains(currentPath))
			return;

		levels.computeIfAbsent(currentLevel, k -> new LinkedHashSet<>()).add(currentJob);
		processed.add(currentPath);

		List<BaseCsvJobObject> dependents = getImmediateDownstreamDependencies(currentJob);
		for (BaseCsvJobObject dependent : dependents) {
			if (dependent != null && !processed.contains(dependent.getFullPath())) {
				buildDownstreamDependencyLevels(dependent, levels, processed, currentLevel + 1);
			}
		}
	}

	/**
	 * Pre-compute all model statistics and cache results
	 */
	public DependencyModelStatistics getComputeModelStatistics(boolean rebuild) {
		if (cachedModelStatistics == null || rebuild) {
			List<BaseCsvJobObject> allJobs = ObjectUtils.toFlatStream(this.tidal.getJobOrGroups()).collect(Collectors.toList());

			// Compute job levels
			cachedJobLevels.clear();
			cachedAllLevels.clear();
			Map<Integer, Set<BaseCsvJobObject>> allLevels = new HashMap<>();
			Set<String> processed = new HashSet<>();

			for (BaseCsvJobObject job : allJobs) {
				if (!processed.contains(job.getFullPath())) {
					buildJobLevels(job, allLevels, processed);
				}
			}
			cachedAllLevels.putAll(allLevels);

			// Find circular dependencies
			cachedCircularJobs.clear();
			Set<String> checkedForCircular = new HashSet<>();
			for (BaseCsvJobObject job : allJobs) {
				if (!checkedForCircular.contains(job.getFullPath()) && hasCircularDependencyDetailed(job)) {
					cachedCircularJobs.add(job);
					checkedForCircular.add(job.getFullPath());
				}
			}

			// Build model statistics
			long jobsWithDependencies = allJobs.stream().mapToLong(job -> upstreamRegistry.containsKey(job.getId()) ? 1 : 0).sum();

			long standAloneJobs = allJobs.stream().mapToLong(job -> !upstreamRegistry.containsKey(job.getId()) && !downstreamRegistry.containsKey(job.getId()) ? 1 : 0).sum();

			cachedModelStatistics = new DependencyModelStatistics(allJobs.size(), dependencies.size(), (int) jobsWithDependencies, (int) standAloneJobs, allLevels.size(), cachedCircularJobs.size(), allLevels, cachedCircularJobs,
					fileDependencyRegistry.size());
		}

		return cachedModelStatistics;
	}

	/**
	 * Build job levels for pre-computation
	 */
	private void buildJobLevels(BaseCsvJobObject currentJob, Map<Integer, Set<BaseCsvJobObject>> levels, Set<String> processed) {

		String currentPath = currentJob.getFullPath();
		if (processed.contains(currentPath))
			return;

		DependenciesRef upstreamRef = upstreamRegistry.get(currentJob.getId());

		if (upstreamRef == null || upstreamRef.isEmpty()) {
			// Root job - Level 0
			levels.computeIfAbsent(0, k -> new LinkedHashSet<>()).add(currentJob);
			cachedJobLevels.put(currentJob.getId(), 0);
			processed.add(currentPath);
			return;
		}

		// Process all dependencies first
		int maxDependencyLevel = -1;
		for (BaseCsvJobObject targetJob : upstreamRef.getJobs()) {
			if (!processed.contains(targetJob.getFullPath())) {
				buildJobLevels(targetJob, levels, processed);
			}

			Integer targetLevel = cachedJobLevels.get(targetJob.getId());
			if (targetLevel != null) {
				maxDependencyLevel = Math.max(maxDependencyLevel, targetLevel);
			}
		}

		// Place this job one level after its highest dependency
		int jobLevel = maxDependencyLevel + 1;
		levels.computeIfAbsent(jobLevel, k -> new LinkedHashSet<>()).add(currentJob);
		cachedJobLevels.put(currentJob.getId(), jobLevel);
		processed.add(currentPath);
	}

	/**
	 * Detailed circular dependency check for pre-computation
	 */
	private boolean hasCircularDependencyDetailed(BaseCsvJobObject job) {
		Set<String> visited = new HashSet<>();
		Set<String> recursionStack = new HashSet<>();
		return hasCircularDependencyHelper(job, visited, recursionStack);
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

		// Check all dependencies using registry
		DependenciesRef upstreamRef = upstreamRegistry.get(job.getId());
		if (upstreamRef != null) {
			for (BaseCsvJobObject targetJob : upstreamRef.getJobs()) {
				if (hasCircularDependencyHelper(targetJob, visited, recursionStack)) {
					return true;
				}
			}
		}

		recursionStack.remove(currentPath);
		return false;
	}

	/**
	 * Update job ID after TIDAL creation - remaps all registry entries
	 * This is critical after job creation when TIDAL assigns real IDs
	 */
	void updateJobId(Integer newTidalId, BaseCsvJobObject objectToUpdate) {

		int oldId = objectToUpdate.getId();
		// 1. Update the job object itself with new ID
		objectToUpdate.setId(newTidalId);

		// 2. Update upstream registry
		DependenciesRef upstreamRef = upstreamRegistry.remove(oldId);
		if (upstreamRef != null) {
			upstreamRegistry.put(newTidalId, upstreamRef);
		}

		// 3. Update downstream registry
		DependenciesRef downstreamRef = downstreamRegistry.remove(oldId);
		if (downstreamRef != null) {
			downstreamRegistry.put(newTidalId, downstreamRef);
		}

		// 4. Update file dependency registry
		List<String> fileRefs = fileDependencyRegistry.remove(oldId);
		if (fileRefs != null) {
			fileDependencyRegistry.put(newTidalId, fileRefs);
		}

		// 5. Update all references TO this job in other registries
		//updateJobReferencesInRegistries(oldId, newTidalId, objectToUpdate);

		// 6. Update master dependencies list
		updateDependenciesListIds(oldId, newTidalId, objectToUpdate);

		// 7. Update cached data
		updateCachedDataIds(oldId, newTidalId);

	}



	/**
	 * Update job references in registries when job ID changes
	 */
	private void updateJobReferencesInRegistries(Integer oldId, Integer newTidalId, BaseCsvJobObject updatedJobObject) {
		// Update upstream registries - replace job object references
		for (DependenciesRef upstreamRef : upstreamRegistry.values()) {
			for (int i = 0; i < upstreamRef.getJobIds().size(); i++) {
				if (upstreamRef.getJobIds().get(i).equals(oldId)) {
					upstreamRef.getJobIds().set(i, newTidalId);
					upstreamRef.getJobs().set(i, updatedJobObject);
				}
			}
		}

		// Update downstream registries - replace job object references
		for (DependenciesRef downstreamRef : downstreamRegistry.values()) {
			for (int i = 0; i < downstreamRef.getJobIds().size(); i++) {
				if (downstreamRef.getJobIds().get(i).equals(oldId)) {
					downstreamRef.getJobIds().set(i, newTidalId);
					downstreamRef.getJobs().set(i, updatedJobObject);
				}
			}
		}
	}

	/**
	 * Update master dependencies list when job ID changes
	 */
	private void updateDependenciesListIds(Integer oldId, Integer newTidalId, BaseCsvJobObject updatedJobObject) {
		
		this.dependencies.forEach(dependency ->{
			// Update job object reference - safe null checking
			if (Objects.equals(dependency.getJobObject().getId(), oldId)) {
				dependency.setJobObject(updatedJobObject);
			}

			
			if (dependency instanceof CvsDependencyJob) {
				CvsDependencyJob jobDep = (CvsDependencyJob) dependency;

				// Update depends on job reference - safe null checking
				if (Objects.equals(jobDep.getDependsOnJob().getId(), oldId)) {
					jobDep.setDependsOnJob(updatedJobObject);
				}

			}
		});
		
	}

	/**
	 * Update cached data when job ID changes
	 */
	private void updateCachedDataIds(Integer oldId, Integer newTidalId) {
		// Update cached job levels
		Integer level = cachedJobLevels.remove(oldId);
		if (level != null) {
			cachedJobLevels.put(newTidalId, level);
		}

		// Note: Other cached data will be rebuilt in preComputeModelStatistics()
	}
	// ===== DATA CLASSES =====

	/**
	 * Summary data class for dependency chain analysis
	 */
	@Data
	public static class DependencyChainSummary {
		private final BaseCsvJobObject job;
		private final int totalUpstreamCount;
		private final int rootJobCount;
		private final int executionLevels;
		private final boolean hasCircularDependency;
		private final Set<BaseCsvJobObject> upstreamJobs;
		private final Set<BaseCsvJobObject> rootJobs;
		private final Map<Integer, Set<BaseCsvJobObject>> dependencyLevels;
	}

}