package com.bluehouseinc.dataconverter.api.importer.exec;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.APIJobUtils;
import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.api.importer.transformers.FTPJobTransformer;
import com.bluehouseinc.dataconverter.api.importer.transformers.FileWatcherJobTransformer;
import com.bluehouseinc.dataconverter.api.importer.transformers.MilestoneJobTransformer;
import com.bluehouseinc.dataconverter.api.importer.transformers.OS400JobTransformer;
import com.bluehouseinc.dataconverter.api.importer.transformers.OSJobTransformer;
import com.bluehouseinc.dataconverter.api.importer.transformers.PeopleSoftJobTransformer;
import com.bluehouseinc.dataconverter.api.importer.transformers.SAPJobTransformer;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvFileWatcherJob;
import com.bluehouseinc.dataconverter.model.impl.CsvFtpJob;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvMilestoneJob;
import com.bluehouseinc.dataconverter.model.impl.CsvOS400;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CsvOwner;
import com.bluehouseinc.dataconverter.model.impl.CsvPeopleSoftJob;
import com.bluehouseinc.dataconverter.model.impl.CsvSAPJob;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.exceptions.TidalHttpResponseException;
import com.bluehouseinc.tidal.api.exceptions.TidalUnauthorizedException;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.agentlist.AgentList;
import com.bluehouseinc.tidal.api.model.businessunit.BusinessUnit;
import com.bluehouseinc.tidal.api.model.calendar.Calendar;
import com.bluehouseinc.tidal.api.model.job.BaseJob;
import com.bluehouseinc.tidal.api.model.job.CarryOverType;
import com.bluehouseinc.tidal.api.model.job.ConcurrentType;
import com.bluehouseinc.tidal.api.model.job.DepLogicType;
import com.bluehouseinc.tidal.api.model.job.Job;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.NearOutageType;
import com.bluehouseinc.tidal.api.model.job.SaveOutputType;
import com.bluehouseinc.tidal.api.model.job.TimeWindowOptionType;
import com.bluehouseinc.tidal.api.model.job.filewatcher.FileWatcherJob;
import com.bluehouseinc.tidal.api.model.job.ftp.FTPJob;
import com.bluehouseinc.tidal.api.model.job.group.JobGroup;
import com.bluehouseinc.tidal.api.model.job.milestone.MilestoneJob;
import com.bluehouseinc.tidal.api.model.job.os.OSJob;
import com.bluehouseinc.tidal.api.model.job.os400.OS400Job;
import com.bluehouseinc.tidal.api.model.job.service.ServiceJob;
import com.bluehouseinc.tidal.api.model.job.terminator.TerminatorJob;
import com.bluehouseinc.tidal.api.model.job.variables.JobVariable;
import com.bluehouseinc.tidal.api.model.job.variables.JobVariableRows;
import com.bluehouseinc.tidal.api.model.jobclass.JobClass;
import com.bluehouseinc.tidal.api.model.node.Node;
import com.bluehouseinc.tidal.api.model.owners.Owner;
import com.bluehouseinc.tidal.api.model.users.Users;
import com.bluehouseinc.tidal.api.model.usersession.UserSession;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.TransformationException;
import com.google.api.client.http.HttpResponseException;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class JobGroupExecutor extends AbstractAPIExecutor {

	public JobGroupExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getModelJobs().forEach(f -> {
			executor.submit(() -> {
				doProcessJobThreads(f, bar);
			});
		});

	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getModelJobs().forEach(f -> {
			doProcessJobThreads(f, bar);
		});
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.jobs", "0"));
	}

	@Override
	public String getProgressBarName() {
		return "Job/Group Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getTotalJobGroupCount().intValue();
	}

	protected void doProcessJobThreads(BaseCsvJobObject ajob, ProgressBar bar) {

		if (ajob.getFullPath().contains("DB0469A.MON")) {
			// This is me.
			ajob.getName();
		}
		doProcessJob(ajob, bar);

		if (!ajob.getChildren().isEmpty()) {
			ajob.getChildren().forEach(f -> doProcessJobThreads((BaseCsvJobObject) f, bar));
		}
	}

	/**
	 * Process a job or group - either create new or update existing
	 */
	private void doProcessJob(BaseCsvJobObject source, ProgressBar bar) {
		try {
			String path = source.getFullPath();
			log.debug("Processing Job[{}] Type[{}]", path, source.getType());

			BaseJob destination = getTidalApi().findJobByPathToLower(path);

			if (destination != null) {
				handleExistingJob(source, destination);
				return;
			}

			// Create new job
			destination = createBaseJob(source);
			processJobByType(source, destination);

		} catch (Exception e) {
			log.error("Error processing job [{}]: {}", source.getFullPath(), e.getMessage(), e);
			throw new TidalException("Failed to process job: " + source.getFullPath(), e);
		} finally {
			bar.step();
		}
	}

	/**
	 * Handle existing job found in TIDAL
	 */
	private void handleExistingJob(BaseCsvJobObject source, BaseJob destination) {
		log.debug("Skipping existing Job [{}] Type[{}]", destination.getFullpath(), destination.getClass().getSimpleName());

		// Update CSV object with existing TIDAL ID
		getDataModel().updateBaseCsvJobObjectID(source, destination.getId());
	}

	/**
	 * Create base job with common properties
	 */
	private BaseJob createBaseJob(BaseCsvJobObject source) {
		BaseJob destination = new Job();

		// Basic properties
		destination.setName(source.getName());
		destination.setConcurrency(source.getConcurrentIfActiveLogic());
		destination.setFullpath(source.getFullPath());
		destination.setNotes(source.getNotes());
		destination.setAllowunscheduled(YesNoType.YES);
		destination.setAllowrerun(YesNoType.YES);

		if (source.getParentId() != null) {
			destination.setParentid(source.getParentId());
		}

		APIJobUtils.setJobDefaults(destination);

		// Configure complex properties
		configureAgent(source, destination);
		configureRuntimeUser(source, destination);
		configureOwner(source, destination);
		configureCalendar(source, destination);
		configureTimeSettings(source, destination);
		configureJobSettings(source, destination);
		configureVariables(source, destination);
		configureBusinessUnit(source, destination);

		return destination;
	}

	/**
	 * Configure agent settings
	 */
	private void configureAgent(BaseCsvJobObject source, BaseJob destination) {
		String agentName = source.getAgentName();
		String agentListName = source.getAgentListName();

		if (source.getInheritAgent()) {
			destination.setInheritagent(YesNoType.YES);
			return;
		}

		if (agentName == null && agentListName == null) {
			destination.setInheritagent(source.getParentId() == null ? YesNoType.NO : YesNoType.YES);
			return;
		}

		destination.setInheritagent(YesNoType.NO);

		if (!StringUtils.isBlank(agentListName)) {
			AgentList agentList = getTidalApi().getAgentListtByName(agentListName);
			if (agentList == null) {
				throw new RuntimeException("Agent List[" + agentListName + "] missing from system");
			}
			destination.setAgentlistid(agentList.getId());
			destination.setAgentlistname(agentList.getName());
		} else if (!StringUtils.isBlank(agentName)) {
			Node agent = getTidalApi().getAgentByName(agentName);
			if (agent == null) {
				throw new RuntimeException("Agent[" + agentName + "] missing from system");
			}
			destination.setAgentid(agent.getId());
		}
	}

	/**
	 * Configure runtime user
	 */
	private void configureRuntimeUser(BaseCsvJobObject source, BaseJob destination) {
		if (source.getRuntimeUser() == null)
			return;

		String rteName = source.getRuntimeUser().getRunTimeUserName();
		String rteDomain = source.getRuntimeUser().getRunTimeUserDomain();
		Users rte = getTidalApi().getUserByAccountNameAndDomain(rteName, rteDomain);

		if (rte != null) {
			destination.setRuntimeusername(rteName);
			destination.setRunuserid(rte.getId());
		}
	}

	/**
	 * Configure owner
	 */
	private void configureOwner(BaseCsvJobObject source, BaseJob destination) {
		CsvOwner owner = source.getOwner();
		if (owner == null) {
			source.setOwner(getDataModel().getDefaultOwner());
			owner = source.getOwner();
		}

		String ownerName = owner.getOwnerName();
		Owner tidalOwner = getTidalApi().getOwnerByName(ownerName);

		if (tidalOwner == null) {
			tidalOwner = getTidalApi().getOwnerByName(getDataModel().getDefaultOwner().getOwnerName());
		}

		destination.setOwner(tidalOwner);
	}

	/**
	 * Configure calendar settings
	 */
	private void configureCalendar(BaseCsvJobObject source, BaseJob destination) {
		if (source.getInheritCalendar()) {
			destination.setInheritcalendar(source.getParentId() == null ? YesNoType.NO : YesNoType.YES);
			return;
		}

		destination.setInheritcalendar(YesNoType.NO);

		CsvCalendar csvCalendar = source.getCalendar();
		if (csvCalendar == null)
			return;

		String calendarName = csvCalendar.getCalendarName().trim();
		Calendar existingCalendar = getTidalApi().getCalenderByName(calendarName);

		if (existingCalendar == null) {
			throw new TidalException("Unable to locate Calendar[" + calendarName + "] for Job[" + source.getFullPath() + "]");
		}

		destination.setCalendarid(existingCalendar.getId());
		destination.setCalendarname(existingCalendar.getName());
		destination.setCalendaroffset(source.getCalendarOffset());

		// Check if parent has same calendar - if so, inherit instead
		BaseCsvJobObject parent = (BaseCsvJobObject) source.getParent();
		if (parent != null && parent.getCalendar() != null) {
			String parentCalendarName = parent.getCalendar().getCalendarName();
			if (calendarName.equalsIgnoreCase(parentCalendarName)) {
				destination.setCalendarid(null);
				destination.setCalendarname(null);
				destination.setInheritcalendar(YesNoType.YES);
			}
		}
	}

	/**
	 * Configure time-related settings
	 */
	private void configureTimeSettings(BaseCsvJobObject source, BaseJob destination) {
		APIJobUtils.setStartEndTime(destination, source);

		if (!StringUtils.isBlank(destination.getTimewindowuntiltime())) {
			TimeWindowOptionType optionType = TimeWindowOptionType.valueOf(getDataModel().getCfgProvider().getTidalJobTimeOutOption());
			destination.setTimewindowoption(optionType);
		}

		APIJobUtils.setRerunFrequency(destination, source);
	}

	/**
	 * Configure job-specific settings
	 */
	private void configureJobSettings(BaseCsvJobObject source, BaseJob destination) {
		if (Boolean.TRUE.equals(source.getRequireOperatorRelease()) || source.getOperatorRelease()) {
			destination.setWaitOperator(YesNoType.YES);
		}

		destination.setDependencylogic(source.getDependencyOrlogic() ? DepLogicType.ATLEASTONE : DepLogicType.ALL);

		if (source.getJobClass() != null && !StringUtils.isBlank(source.getJobClass().getName())) {
			String jobClassName = source.getJobClass().getName();
			JobClass jobClass = getTidalApi().getJobClassByName(jobClassName);
			if (jobClass == null) {
				throw new TidalException("Defined Job Class[" + jobClassName + "] not found in TIDAL");
			}
			destination.setJobclassid(jobClass.getId());
		}

		if (source.getDisableCarryOver()) {
			destination.setDisablecarryover(CarryOverType.TRUE);
		}

		destination.setRerundependency(source.getRerundependency());

		// Apply configuration defaults
		destination.setSaveoutputoption(SaveOutputType.valueOf(getDataModel().getCfgProvider().getJobSaveOutputOption()));
		destination.setConcurrency(ConcurrentType.valueOf(getDataModel().getCfgProvider().getTidalConcurrentTypes()));
		destination.setNearoutage(NearOutageType.valueOf(getDataModel().getCfgProvider().getTidalNearOutage()));
	}

	/**
	 * Configure job variables
	 */
	private void configureVariables(BaseCsvJobObject source, BaseJob destination) {
		if (source.getVariables().isEmpty())
			return;

		JobVariableRows rows = new JobVariableRows();
		for (CsvVariable variable : source.getVariables()) {
			rows.getRow().add(new JobVariable(variable.getVarName(), variable.getVarValue()));
		}
		destination.setVariablesFromVariableString(rows);
	}

	/**
	 * Configure business unit (timezone)
	 */
	private void configureBusinessUnit(BaseCsvJobObject source, BaseJob destination) {
		if (source.getTimeZone() == null)
			return;

		BusinessUnit businessUnit = getTidalApi().getBusinessUnitByName(source.getTimeZone().getName());
		if (businessUnit != null) {
			destination.setBusinessunitid(businessUnit.getId());
		}
	}

	/**
	 * Process job based on its specific type
	 */
	private void processJobByType(BaseCsvJobObject source, BaseJob destination) throws IllegalAccessException, InvocationTargetException {
		try {
			if (source instanceof CsvJobGroup) {
				processJobGroup((CsvJobGroup) source, destination);
			} else if (source instanceof CsvOSJob) {
				processOSJob((CsvOSJob) source, destination);
			} else if (source instanceof CsvFtpJob) {
				processFTPJob((CsvFtpJob) source, destination);
			} else if (source instanceof CsvOS400) {
				processOS400Job((CsvOS400) source, destination);
			} else if (source instanceof CsvPeopleSoftJob) {
				processPeopleSoftJob((CsvPeopleSoftJob) source, destination);
			} else if (source instanceof CsvSAPJob) {
				processSAPJob((CsvSAPJob) source, destination);
			} else if (source instanceof CsvFileWatcherJob) {
				processFileWatcherJob((CsvFileWatcherJob) source, destination);
			} else if (source instanceof CsvMilestoneJob) {
				processMilestoneJob((CsvMilestoneJob) source, destination);
			} else {
				log.error("Unknown Job Instance: {}", source.getClass().getName());
				throw new TidalException("Unsupported job type: " + source.getClass().getName());
			}
		} catch (TransformationException e) {
			log.error("Transformation error for job [{}]: {}", source.getFullPath(), e.getMessage(), e);
			throw new TidalException("Job transformation failed: " + source.getFullPath(), e);
		}
	}

	/**
	 * Process job group
	 */
	private void processJobGroup(CsvJobGroup source, BaseJob destination) throws IllegalAccessException, InvocationTargetException {
		JobGroup group = (JobGroup) destination.toJob(new JobGroup());
		group.setType(JobType.GROUP);
		doCreate(group, source);
	}

	/**
	 * Process OS job
	 */
	private void processOSJob(CsvOSJob source, BaseJob destination) throws IllegalAccessException, InvocationTargetException, TransformationException {
		OSJob osJob = (OSJob) destination.toJob(new OSJob());
		OSJobTransformer transformer = new OSJobTransformer(osJob, getTidalApi());
		osJob = transformer.transform(source);
		doCreate(osJob, source);
	}

	/**
	 * Process FTP job
	 */
	private void processFTPJob(CsvFtpJob source, BaseJob destination) throws IllegalAccessException, InvocationTargetException, TransformationException {
		FTPJob ftpJob = (FTPJob) destination.toJob(new FTPJob());
		FTPJobTransformer transformer = new FTPJobTransformer(ftpJob, getTidalApi());
		ftpJob = transformer.transform(source);
		doCreate(ftpJob, source);
	}

	/**
	 * Process OS400 job
	 */
	private void processOS400Job(CsvOS400 source, BaseJob destination) throws IllegalAccessException, InvocationTargetException, TransformationException {
		OS400Job os400Job = (OS400Job) destination.toJob(new OS400Job());
		OS400JobTransformer transformer = new OS400JobTransformer(os400Job, getTidalApi());
		os400Job = transformer.transform(source);
		doCreate(os400Job, source);
	}

	/**
	 * Process PeopleSoft job
	 */
	private void processPeopleSoftJob(CsvPeopleSoftJob source, BaseJob destination) throws IllegalAccessException, InvocationTargetException, TransformationException {
		ServiceJob serviceJob = (ServiceJob) destination.toJob(new ServiceJob());
		PeopleSoftJobTransformer transformer = new PeopleSoftJobTransformer(serviceJob, getTidalApi());
		serviceJob = transformer.transform(source);
		doCreate(serviceJob, source);
	}

	/**
	 * Process SAP job
	 */
	private void processSAPJob(CsvSAPJob source, BaseJob destination) throws IllegalAccessException, InvocationTargetException, TransformationException {
		ServiceJob serviceJob = (ServiceJob) destination.toJob(new ServiceJob());
		SAPJobTransformer transformer = new SAPJobTransformer(serviceJob, getTidalApi());
		serviceJob = transformer.transform(source);
		doCreate(serviceJob, source);
	}

	/**
	 * Process File Watcher job
	 */
	private void processFileWatcherJob(CsvFileWatcherJob source, BaseJob destination) throws IllegalAccessException, InvocationTargetException, TransformationException {
		FileWatcherJob fileWatcherJob = (FileWatcherJob) destination.toJob(new FileWatcherJob());
		FileWatcherJobTransformer transformer = new FileWatcherJobTransformer(fileWatcherJob, getTidalApi());
		fileWatcherJob = transformer.transform(source);
		doCreate(fileWatcherJob, source);
	}

	/**
	 * Process Milestone job
	 */
	private void processMilestoneJob(CsvMilestoneJob source, BaseJob destination) throws IllegalAccessException, InvocationTargetException, TransformationException {
		MilestoneJob milestoneJob = (MilestoneJob) destination.toJob(new MilestoneJob());
		MilestoneJobTransformer transformer = new MilestoneJobTransformer(milestoneJob, getTidalApi());
		milestoneJob = transformer.transform(source);
		doCreate(milestoneJob, source);
	}

	private void doCreate(BaseJob destination, BaseCsvJobObject source) {
		TesResult res = doCreate(destination);

		Integer newid = res.getResult().getTesObjectid();
		String tesresponse = res.getTesMessage();

		// We must always have a new ID or something is wrong.
		if (newid != null) {
			getDataModel().updateBaseCsvJobObjectID(source, newid);
			destination.setId(newid);
			destination.setFullpath(source.getFullPath());
			getTidalApi().getJobs().add(destination);
			getTidalApi().doProcessBaseJobMapToLower(destination);

			log.debug("JobGroupExecutor-doCreate Job [" + destination.getFullpath() + "] Type[" + destination.getClass().getSimpleName() + "] Response ID[" + newid + "][" + tesresponse + "]");
		} else {
			throw new TidalException("JobGroupExecutor-Object ID is null or missing, somethign is wrong with [" + destination.getFullpath() + "]");
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {
		// TODO Auto-generated method stub
		if (object instanceof JobGroup) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().jobGroup();
		} else if (object instanceof OSJob) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().osJob();
		} else if (object instanceof FTPJob) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().ftpJob();
		} else if (object instanceof OS400Job) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().os400();
		} else if (object instanceof ServiceJob) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().serviceJob();
		} else if (object instanceof FileWatcherJob) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().fileWatcherJob();
		} else if (object instanceof TerminatorJob) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().terminatorJob();
		} else if (object instanceof MilestoneJob) {
			return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().milestoneJob();
		} else {
			throw new RuntimeException("doCreate Unsupported Job Type: " + object.getClass().getSimpleName());
		}

	}

}
