package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCvsDependency;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyFile;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyJob;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.dependency.file.DepType;
import com.bluehouseinc.tidal.api.model.dependency.file.FileDependency;
import com.bluehouseinc.tidal.api.model.dependency.job.JobDependency;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class DependencyExecutor extends AbstractAPIExecutor {

	public DependencyExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {
		getDataModel().getDependencies().forEach(f -> {
			executor.submit(() -> {
				doProcessDep(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getDependencies().forEach(f -> {
			doProcessDep(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "Dependency Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getDependencies().size();
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.dependency", "0"));
	}

	/**
	 * Process dependency - either create new or skip if existing
	 */
	protected void doProcessDep(BaseCvsDependency dependency, ProgressBar bar) {
		try {
			int jobId = dependency.getJobObject().getId();
			String jobPath = dependency.getJobObject().getFullPath();

			if (dependency instanceof CvsDependencyJob) {
				processJobDependency((CvsDependencyJob) dependency, jobId, jobPath);
			} else if (dependency instanceof CvsDependencyFile) {
				processFileDependency((CvsDependencyFile) dependency, jobId, jobPath);
			} else {
				log.warn("Unknown dependency type: {}", dependency.getClass().getSimpleName());
			}
		} finally {
			bar.step();
		}
	}

	/**
	 * Process job-to-job dependency
	 */
	private void processJobDependency(CvsDependencyJob csvDependency, int jobId, String jobPath) {
		int dependsOnJobId = csvDependency.getDependsOnJob().getId();
		String dependsOnPath = csvDependency.getDependsOnJob().getFullPath();

		log.debug("Processing dependency: Job[{}] depends on Job[{}]", jobId, dependsOnJobId);

		// Check if dependency already exists
		JobDependency existing = getTidalApi().findDependencyByJobId(jobId, dependsOnJobId);
		if (existing != null) {
			log.debug("Dependency already exists - ID[{}] Job[{}] depends on Job[{}]. Skipping creation.", existing.getId(), jobPath, dependsOnPath);
			getDataModel().updateBaseCsvDependencyID(csvDependency, existing.getId());
			return;
		}

		// Create new dependency
		JobDependency tidalDependency = createTidalJobDependency(csvDependency, jobId, dependsOnJobId);

		// Check for dependency loops if enabled
		if (getDataModel().getCfgProvider().checkDependencyLoop() && isLoopDetected(jobId, dependsOnJobId, jobPath, dependsOnPath)) {
			return;
		}

		
		// Create dependency in TIDAL
		createDependencyInTidal(tidalDependency, csvDependency, jobPath, dependsOnPath);
	}

	/**
	 * Process file dependency
	 */
	private void processFileDependency(CvsDependencyFile csvDependency, int jobId, String jobPath) {
		String filename = csvDependency.getFilename();

		log.debug("Processing file dependency: Job[{}] depends on File[{}]", jobPath, filename);

		FileDependency fileDependency = createTidalFileDependency(jobId, filename);

		try {
			TesResult result = doCreate(fileDependency);
			int dependencyId = result.getResult().getTesObjectid();

			fileDependency.setId(dependencyId);
			getDataModel().updateBaseCsvDependencyID(csvDependency, dependencyId);
			getTidalApi().getFiledeps().add(fileDependency);

			log.debug("Created file dependency - Job[{}] depends on File[{}] ID[{}] Response[{}]", jobPath, filename, dependencyId, result.getTesMessage());

		} catch (Exception e) {
			log.error("Failed to create file dependency - Job[{}] depends on File[{}]: {}", jobPath, filename, e.getMessage());
			throw new TidalException("File dependency creation failed", e);
		}
	}

	/**
	 * Create TIDAL job dependency object
	 */
	private JobDependency createTidalJobDependency(CvsDependencyJob csvDependency, int jobId, int dependsOnJobId) {
		JobDependency dependency = new JobDependency();

		dependency.setJobid(jobId);
		dependency.setDepjobid(dependsOnJobId);
		dependency.setLogic(csvDependency.getLogic());
		dependency.setOperator(csvDependency.getOperator());
		dependency.setStatus(csvDependency.getJobStatus());
		dependency.setIgnoredep("Y");

		// Configure date offset if specified
		if (csvDependency.getDateOffset() != null) {
			dependency.setDateoffset(csvDependency.getDateOffset());
			dependency.setIngroup(YesNoType.NO); // Required for offset functionality
		}

		// Configure exit code logic if specified
		if (csvDependency.hasExitCodeLogic()) {
			dependency.setExitCode(csvDependency.getExitCodeOperator(), csvDependency.getExitcodeStart(), csvDependency.getExitcodeEnd());
		}

		return dependency;
	}

	/**
	 * Create TIDAL file dependency object
	 */
	private FileDependency createTidalFileDependency(int jobId, String filename) {
		FileDependency fileDependency = new FileDependency();

		fileDependency.setJobid(jobId);
		fileDependency.setFilename(filename);
		fileDependency.setFiletype(DepType.EXISTS);
		fileDependency.setInheritagent(YesNoType.YES);

		return fileDependency;
	}

	/**
	 * Check for dependency loops using TIDAL's built-in detection
	 */
	private boolean isLoopDetected(int jobId, int dependsOnJobId, String jobPath, String dependsOnPath) {
		try {
			boolean loopDetected = getTidalApi().getSession().getServiceFactory().job().dependencyLoopDetected(jobId, dependsOnJobId);

			if (loopDetected) {
				log.error("Dependency loop detected - Job[{}] depends on Job[{}]. Skipping dependency creation.", jobPath, dependsOnPath);
				return true;
			}
			return false;

		} catch (TidalException e) {
			log.warn("Unable to check for dependency loop - Job[{}] depends on Job[{}]: {}", jobPath, dependsOnPath, e.getMessage());
			return false; // Proceed with creation if loop check fails
		}
	}

	/**
	 * Create dependency in TIDAL system
	 */
	private void createDependencyInTidal(JobDependency tidalDependency, CvsDependencyJob csvDependency, String jobPath, String dependsOnPath) {
		try {
			TesResult result = doCreate(tidalDependency);
			int dependencyId = result.getResult().getTesObjectid();

			tidalDependency.setId(dependencyId);
			getDataModel().updateBaseCsvDependencyID(csvDependency, dependencyId);
			getTidalApi().getJobdeps().add(tidalDependency);

			log.debug("Created job dependency - Job[{}] depends on Job[{}] ID[{}] Response[{}]", jobPath, dependsOnPath, dependencyId, result.getTesMessage());

		} catch (TidalException e) {
			String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();

			if (errorMessage.contains("job dependency will result in a loop")) {
				log.error("Dependency loop detected during creation - Job[{}] depends on Job[{}]", jobPath, dependsOnPath);
			} else {
				log.info("Failed to create job dependency - Job[{}] depends on Job[{}]: {}", jobPath, dependsOnPath, e.getLocalizedMessage());
				//throw new TidalException("Job dependency creation failed", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {

		if (object instanceof FileDependency) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().fileDependency();
		} else if (object instanceof JobDependency) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().jobDependency();
		} else {
			throw new RuntimeException("getExecutorAPI Unsupported Job Type: " + object.getClass().getSimpleName());
		}

	}

}
