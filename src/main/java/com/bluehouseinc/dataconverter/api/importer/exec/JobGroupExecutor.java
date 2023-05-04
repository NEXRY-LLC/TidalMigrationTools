package com.bluehouseinc.dataconverter.api.importer.exec;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.APIJobUtils;
import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.api.importer.transformers.FTPJobTransformer;
import com.bluehouseinc.dataconverter.api.importer.transformers.FileWatcherJobTransformer;
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
import com.bluehouseinc.dataconverter.model.impl.CsvOS400;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CsvOwner;
import com.bluehouseinc.dataconverter.model.impl.CsvPeopleSoftJob;
import com.bluehouseinc.dataconverter.model.impl.CsvSAPJob;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.agentlist.AgentList;
import com.bluehouseinc.tidal.api.model.businessunit.BusinessUnit;
import com.bluehouseinc.tidal.api.model.calendar.Calendar;
import com.bluehouseinc.tidal.api.model.job.BaseJob;
import com.bluehouseinc.tidal.api.model.job.CarryOverType;
import com.bluehouseinc.tidal.api.model.job.DepLogicType;
import com.bluehouseinc.tidal.api.model.job.Job;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.filewatcher.FileWatcherJob;
import com.bluehouseinc.tidal.api.model.job.ftp.FTPJob;
import com.bluehouseinc.tidal.api.model.job.group.JobGroup;
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
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.TransformationException;

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

		doProcessJob(ajob, bar);

		if (!ajob.getChildren().isEmpty()) {
			ajob.getChildren().forEach(f -> doProcessJobThreads((BaseCsvJobObject) f, bar));
		}
	}

	/**
	 * Can be jobs or groups
	 *
	 * @param source
	 */
	private void doProcessJob(BaseCsvJobObject source, ProgressBar bar) {

		String path = source.getFullPath();
		log.debug("doProcessJobs Job[" + path + "] Type[" + source.getType().toString() + "]");

		BaseJob destination = getTidalApi().findJobByPathToLower(path); // Do we exist in the system already?

		boolean update = false;

		if (destination == null) {
			destination = new Job();

			destination.setName(source.getName());

			if (destination.getName().contains("AHW_QNXT_0300__CreateMemberRelation")) {
				source.getName();
			}

			APIJobUtils.setJobDefaults(destination);

			String agentname = source.getAgentName();

			String agentlistname = source.getAgentListName();

			if (agentname == null && agentlistname == null) {
				if (source.getParentId() == null) {
					destination.setInheritagent(YesNoType.NO);
				} else {
					destination.setInheritagent(YesNoType.YES); //
				}
			} else {

				if (!StringUtils.isBlank(agentlistname)) {
					// We are a agent list.
					AgentList aglist = getTidalApi().getAgentListtByName(agentlistname);

					if (aglist == null) {
						throw new RuntimeException("doProcessJobs Agent List[" + agentlistname + "]is missing from system.");
					} else {
						destination.setAgentlistid(aglist.getId());
						destination.setAgentlistname(aglist.getName());
						destination.setInheritagent(YesNoType.NO); //

					}
				} else if (!StringUtils.isBlank(agentname)) {

					Node agent = getTidalApi().getAgentByName(source.getAgentName());

					if (agent == null) {
						throw new RuntimeException("doProcessJobs Agent / Adapter [" + agentname + "]is missing from system.");
					} else {
						destination.setAgentid(agent.getId());
						destination.setInheritagent(YesNoType.NO); //
					}
				}

			}

			if (source.getRuntimeUser() != null) {
				String rtename = source.getRuntimeUser().getRunTimeUserName();
				String rtedomain = source.getRuntimeUser().getRunTimeUserDomain();
				Users rte = getTidalApi().getUserByAccountNameAndDomain(rtename,rtedomain);

				if (rte != null) {
					destination.setRuntimeusername(rtename);
					destination.setRunuserid(rte.getId());
				}
			}

			CsvOwner own = source.getOwner();

			if (own == null) {
				source.setOwner(getDataModel().getDefaultOwner());
			}

			String ownername = source.getOwner().getOwnerName();
			Owner owner = getTidalApi().getOwnerByName(ownername);
			
			if(owner == null) {
				 owner =getTidalApi().getOwnerByName(getDataModel().getDefaultOwner().getOwnerName());
				destination.setOwner(owner);
			}else {
				destination.setOwner(owner);
			}

			if (source.getParentId() == null) {
				destination.setInheritcalendar(YesNoType.NO); // No top level can ever inher
			} else if (source.getInheritCalendar()) {
				destination.setInheritcalendar(YesNoType.YES);
			} else {
				destination.setInheritcalendar(YesNoType.NO);

				CsvCalendar csvcal = source.getCalendar();

				if (csvcal != null) {
					String calname = csvcal.getCalendarName().trim();
					Calendar existingcalendar = getTidalApi().getCalenderByName(calname);
					BaseCsvJobObject parent = (BaseCsvJobObject) source.getParent();

					// We have an existing calendar so reference that one.
					if (existingcalendar != null) {
						destination.setCalendarid(existingcalendar.getId());
						destination.setCalendarname(existingcalendar.getName());
						destination.setInheritcalendar(YesNoType.NO);
						destination.setCalendaroffset(source.getCalendarOffset());
					} else {
						// Technically we can't get here.. if we have a calendar listed and we can't
						// find it in our system we have a major issue.
						throw new TidalException("Unable to locate Calendar [" + calname + "] for Job["+source.getFullPath()+"]");
					}

					if (parent != null) {
						CsvCalendar parentcal = parent.getCalendar();

						if (parentcal != null) {
							if (!StringUtils.isBlank(parentcal.getCalendarName())) {
								String parentcalname = parentcal.getCalendarName();
								if (calname.equalsIgnoreCase(parentcalname)) {
									destination.setCalendarid(null);
									destination.setCalendarname(null);
									destination.setInheritcalendar(YesNoType.YES); // Matches parent to just inherit.
								}
							}
						}
					}

				}
			}

			APIJobUtils.setStartEndTime(destination, source);
			APIJobUtils.setRerunFrequency(destination, source);


			if (source.getRequireOperatorRelease() != null && source.getRequireOperatorRelease()) {
				destination.setWaitOperator(YesNoType.YES);
			}

			destination.setFullpath(source.getFullPath());

			if (source.getParentId() != null) {
				destination.setParentid(source.getParentId());
			}

			if (source.getAgentName() == null && source.getParentId() != null) {
				destination.setInheritagent(YesNoType.NO);
			}

			destination.setNotes(source.getNotes());

			destination.setAllowunscheduled(YesNoType.YES);
			destination.setAllowrerun(YesNoType.YES);

			if (!source.getVariables().isEmpty()) {
				JobVariableRows rows = new JobVariableRows();

				for (CsvVariable f : source.getVariables()) {
					rows.getRow().add(new JobVariable(f.getVarName(), f.getVarValue()));
				}

				destination.setVariablesFromVariableString(rows);
			}

			if (source.getTimeZone() != null) {
				BusinessUnit bu = getTidalApi().getBusinessUnitByName(source.getTimeZone().getName());
				destination.setBusinessunitid(bu.getId());
			}

			if (source.getDependencyOrlogic()) {
				destination.setDependencylogic(DepLogicType.ATLEASTONE);
			} else {
				destination.setDependencylogic(DepLogicType.ALL);
			}

			if (source.getOperatorRelease()) {
				destination.setWaitOperator(YesNoType.YES);
			}

			if(source.getJobClass() != null) {
				String jobclassname = source.getJobClass().getName();

				if(!StringUtils.isBlank(source.getJobClass().getName())) {
					JobClass jobclass = getTidalApi().getJobClassByName(jobclassname);

					if(jobclass == null) {
						throw new TidalException("Defined Job Class Not Found in TIDAL");
					}else {
						destination.setJobclassid(jobclass.getId());
					}
				}
			}

			if(source.getDisableCarryOver()) {
				destination.setDisablecarryover(CarryOverType.TRUE);
			}

		} else {
			update = true;
			if (source.getName().trim().equals("YFTXLDRE")) {
				source.getName();
			}
			int existingID = destination.getId();
			// Set correct ID on dep object is existing job in system.
			getDataModel().updateBaseCsvJobObjectID(source, existingID);
			log.debug("doCreate Skipping Job [" + destination.getFullpath() + "] Type[" + destination.getClass().getSimpleName() + "]");
			return;
		}

		try {
			if (source instanceof CsvJobGroup) {

				// if (ajob.getChildren().isEmpty()) {
				// return; // Do not process empty groups.
				// }

				CsvJobGroup agroup = (CsvJobGroup) source;
				JobGroup grp = (JobGroup) destination.toJob(new JobGroup());
				grp.setType(JobType.GROUP);
				if (update) {

				} else {
					doCreate(grp, agroup, getDataModel());
				}

				// Recurse
				// agroup.getChildren().stream().forEach(f -> doProcessJobs((BaseCsvJobObject)
				// f, model, bar));
			}

			if (source instanceof CsvOSJob) {

				if (update) {

				} else {
					OSJob osj = (OSJob) destination.toJob(new OSJob());

					OSJobTransformer t = new OSJobTransformer(osj, getTidalApi());
					try {
						osj = t.transform((CsvOSJob) source);

					} catch (TransformationException e) {
						log.error(e);
						throw new TidalException(e);
					}

					doCreate(osj, source, getDataModel());

				}

			}

			if (source instanceof CsvFtpJob) {
				if (update) {

				} else {
					FTPJob osj = (FTPJob) destination.toJob(new FTPJob());

					FTPJobTransformer t = new FTPJobTransformer(osj, getTidalApi());

					try {
						osj = t.transform((CsvFtpJob) source);

					} catch (TransformationException e) {
						log.error(e);
						throw new TidalException(e);
					}

					doCreate(osj, source, getDataModel());
				}

			}

			if (source instanceof CsvOS400) {
				if (update) {

				} else {
					OS400Job osj = (OS400Job) destination.toJob(new OS400Job());

					OS400JobTransformer t = new OS400JobTransformer(osj, getTidalApi());

					try {
						osj = t.transform((CsvOS400) source);

					} catch (TransformationException e) {
						log.error(e);
						throw new TidalException(e);
					}

					doCreate(osj, source, getDataModel());
				}

			}

			if (source instanceof CsvPeopleSoftJob) {
				if (update) {

				} else {
					ServiceJob osj = (ServiceJob) destination.toJob(new ServiceJob());

					PeopleSoftJobTransformer t = new PeopleSoftJobTransformer(osj, getTidalApi());

					try {
						osj = t.transform((CsvPeopleSoftJob) source);

					} catch (TransformationException e) {
						log.error(e);
						throw new TidalException(e);
					}

					doCreate(osj, source, getDataModel());
				}

			}

			if (source instanceof CsvSAPJob) {
				if (update) {

				} else {
					ServiceJob osj = (ServiceJob) destination.toJob(new ServiceJob());

					SAPJobTransformer t = new SAPJobTransformer(osj, getTidalApi());

					try {
						osj = t.transform((CsvSAPJob) source);

					} catch (TransformationException e) {
						log.error(e);
						throw new TidalException(e);
					}

					doCreate(osj, source, getDataModel());
				}

			}

			if (source instanceof CsvFileWatcherJob) {
				if (update) {

				} else {
					FileWatcherJob osj = (FileWatcherJob) destination.toJob(new FileWatcherJob());

					FileWatcherJobTransformer t = new FileWatcherJobTransformer(osj, getTidalApi());

					try {
						osj = t.transform((CsvFileWatcherJob) source);

					} catch (TransformationException e) {
						log.error(e);
						throw new TidalException(e);
					}

					doCreate(osj, source, getDataModel());
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			log.error(e);
		} finally {
			bar.step();
		}

	}

	private void doCreate(BaseJob destination, BaseCsvJobObject source, TidalDataModel model) {

		Integer newid = null;
		String tesresponse = null;
		log.debug("doCreate Creating Job [" + destination.getFullpath() + "] Type[" + destination.getClass().getSimpleName() + "]");
		if (destination instanceof JobGroup) {
			TesResult res = getTidalApi().getSession().getServiceFactory().jobGroup().create((JobGroup) destination);
			newid = res.getResult().getTesObjectid();
			tesresponse = res.getTesMessage();
		} else if (destination instanceof OSJob) {
			TesResult res = getTidalApi().getSession().getServiceFactory().osJob().create((OSJob) destination);
			newid = res.getResult().getTesObjectid();
			tesresponse = res.getTesMessage();
		} else if (destination instanceof FTPJob) {
			TesResult res = getTidalApi().getSession().getServiceFactory().ftpJob().create((FTPJob) destination);
			newid = res.getResult().getTesObjectid();
			tesresponse = res.getTesMessage();
		} else if (destination instanceof OS400Job) {
			TesResult res = getTidalApi().getSession().getServiceFactory().os400().create((OS400Job) destination);
			newid = res.getResult().getTesObjectid();
			tesresponse = res.getTesMessage();
		} else if (destination instanceof ServiceJob) {
			TesResult res = getTidalApi().getSession().getServiceFactory().serviceJob().create((ServiceJob) destination);
			newid = res.getResult().getTesObjectid();
			tesresponse = res.getTesMessage();
		} else if (destination instanceof FileWatcherJob) {
			TesResult res = getTidalApi().getSession().getServiceFactory().fileWatcherJob().create((FileWatcherJob) destination);
			newid = res.getResult().getTesObjectid();
			tesresponse = res.getTesMessage();
		} else if (destination instanceof TerminatorJob) {
			TesResult res = getTidalApi().getSession().getServiceFactory().terminatorJob().create((TerminatorJob) destination);
			newid = res.getResult().getTesObjectid();
			tesresponse = res.getTesMessage();
		} else {
			throw new RuntimeException("doCreate Unsupported Job Type");
		}

		// We must always have a new ID or something is wrong.
		if (newid != null) {
			model.updateBaseCsvJobObjectID(source, newid);
			destination.setId(newid);
			destination.setFullpath(source.getFullPath());
			getTidalApi().getJobs().add(destination);
			getTidalApi().doProcessBaseJobMapToLower(destination);

			log.debug("doCreate Job [" + destination.getFullpath() + "] Type[" + destination.getClass().getSimpleName() + "] Response ID[" + newid + "][" + tesresponse + "]");
		} else {
			throw new TidalException("Object ID is null or missing, somethign is wrong with [" + destination.getFullpath() + "]");
		}

	}

}
