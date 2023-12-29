package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspJobGroup;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspOSJOb;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspAfterStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspPrereqStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspReleaseStatement;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.model.dependency.job.DepLogic;
import com.bluehouseinc.tidal.api.model.dependency.job.DependentJobStatus;
import com.bluehouseinc.tidal.api.model.dependency.job.Operator;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class DependencyGraphMapper {

	private TidalDataModel datamodel;
	private EspConfigProvider cfgProvider;
	private EspDataModel espmodel;


	public DependencyGraphMapper(EspConfigProvider cfgProvider, TidalDataModel datamodel, EspDataModel espmodel) {
		this.cfgProvider = cfgProvider;
		this.datamodel = datamodel;
		this.espmodel = espmodel;
	}

	public void doProcessJobDepsForJob(EspAbstractJob esp) {

		if (esp.getFullPath().startsWith("\\JOBOSS01\\MTIC_LOADBAT")) {
			esp.getName();
		}

		BaseCsvJobObject me = this.getDatamodel().findFirstJobByFullPath(esp.getFullPath());

		// This job must exists in the system
		if (me == null) {
			// log.error("Unable to locate job[" + esp.getFullPath() + "]");
			throw new RuntimeException("DependencyGraphMapper - Unable to locate job[" + esp.getFullPath() + "]");
		}

		setupSleepDependency();
		
		doHandleExternalAppIDLogic(me, esp);

		doHandlePrereq(me, esp);

		doHandleReleaseStatements(me, esp);

		doHandleNotWithStatements(me, esp);

		doHandleAfterStatements(me, esp);

	}

	private void doHandlePrereq(BaseCsvJobObject me, EspAbstractJob esp) {
		EspPrereqStatement espPrereqStatement = esp.getStatementObject().getPrerequisite();

		if (espPrereqStatement == null) {
			return;
		}
		String depjobName = espPrereqStatement.getFullName();
		// OUr scope is in our group, so we just need to look up and find another job in
		// our group that matches.

		BaseJobOrGroupObject foundDepjob = esp.getParent().getChildren().stream().filter(job -> depjobName.equals(job.getName())).findFirst().orElse(null);
		// This is not fast code but ESP is small..
		// We are finding our esp version based on name, then we need to find Csv
		// version to setup dependency using full path.

		if (foundDepjob != null) {
			BaseCsvJobObject jobDependency = this.getDatamodel().findFirstJobByFullPath(foundDepjob.getFullPath());
			log.debug("DependencyGraphMapper.doHandlePrereq Registering Dependency for Job[" + me.getFullPath() + "] that depends on [" + jobDependency.getFullPath() + "]");
			this.getDatamodel().addJobDependencyForJobCompletedNormal(me, jobDependency, null);

			handleSleepjob(me, jobDependency);
		} else {
			log.error("DependencyGraphMapper.doHandlePrereq ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] that depends on [" + depjobName + "]");
		}
	}

	private void doHandleReleaseStatements(BaseCsvJobObject me, EspAbstractJob esp) {

		List<EspReleaseStatement> espReleaseStatements = esp.getStatementObject().getEspReleasedJobDependencies();

		if (espReleaseStatements != null && !espReleaseStatements.isEmpty()) { // traverse all jobs which need to wait for CURRENT job to complete

			// For every one of these listed means that these jobs need to depend on me
			espReleaseStatements.forEach(espReleaseStatement -> {

				if (!espReleaseStatement.getReleaseJobs().isEmpty()) {

					List<String> releasedJobNames = espReleaseStatement.getReleaseJobs();

					List<BaseJobOrGroupObject> childjobs = new ArrayList<>();

					// Find jobs in our group that I have set a release add statement.. E.G these
					// must wait on me to complete
					if (esp instanceof EspJobGroup) {
						// Now that we put deps on groups, we need to check if we are a group vs job.
						childjobs = esp.getChildren();
					} else {
						childjobs = esp.getParent().getChildren();
					}

					List<BaseJobOrGroupObject> jobsToBeReleased = childjobs.stream().filter(job -> releasedJobNames.contains(job.getName())).collect(Collectors.toList());

					// This is the list of jobs that should be dependent on me to completed.
					jobsToBeReleased.forEach(dependsOnMe -> {

						BaseCsvJobObject dependsOnMeDep = this.getDatamodel().findFirstJobByFullPath(dependsOnMe.getFullPath());

						if (dependsOnMeDep != null) {
							log.debug("DependencyGraphMapper.doHandleReleaseStatements Registering Dependency for Job[" + me.getFullPath() + "] This job depends on me [" + dependsOnMeDep.getFullPath() + "]");

							// We are assuming that if any data is listed its abnormal.
							// Current examples are like this: RELEASE ADD(CDSTAT.BOM1_FOR_F191_CFSCD(A))
							if (StringUtils.isBlank(espReleaseStatement.getCondition())) {
								this.getDatamodel().addJobDependencyForJob(dependsOnMeDep, me, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_NORMAL, null);
							} else {
								this.getDatamodel().addJobDependencyForJob(dependsOnMeDep, me, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_ABNORMAL, null);
							}

							handleSleepjob(dependsOnMeDep, me);

						} else {
							log.error("DependencyGraphMapper.doHandlePrereq ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + dependsOnMe.getFullPath() + "]");
						}

					});
				}
			});
		}
	}

	private void doHandleNotWithStatements(BaseCsvJobObject me, final EspAbstractJob esp) {

		String dotDashMatch = "XXXXXXXXXXXXXXXXXXXX\\..+|XXXXXXXXXXXXXXXXXXXX";
		String dashDotDashMatch = "XXXXXXXXXXXXXXXXXXXX.*";

		// traversing all jobs which must NOT be active in order for CURRENT job to run
		esp.getStatementObject().getEspNotWithStatements().forEach(statement -> {

			final String lookfor = statement.getJobName().trim();
			String ingroup = statement.getJobGroupName();

			List<EspAbstractJob> mydeps = new ArrayList<>();

			if (ingroup != null && !lookfor.contains("-")) {
				// NOTWITH (MTIC_LOADBAT/WGBOSS01)

				List<EspJobGroup> groups = this.getEspmodel().getObjectsByType(EspJobGroup.class);

				EspJobGroup foundgroup = groups.stream().filter(f -> f.getName().equalsIgnoreCase(ingroup)).findFirst().orElse(null);

				if (foundgroup != null) {
					final String findme = lookfor;
					EspAbstractJob foundjob = (EspAbstractJob) foundgroup.getChildren().stream().filter(c -> c.getName().equalsIgnoreCase(findme)).findFirst().orElse(null);
					mydeps.add(foundjob);
				} else {
					log.error("DependencyGraphMapper.doHandleNotWithStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate Job[" + lookfor + "] in group[" + ingroup + "]");
				}

			} else if (lookfor.contains("-")) {
				String tempregex = dotDashMatch;
				String startswith = lookfor;

				if (lookfor.contains("-.-")) {
					tempregex = dashDotDashMatch;
					startswith = lookfor.replace("-.-", "");
				} else if (lookfor.contains(".-")) {
					tempregex = dotDashMatch;
					startswith = lookfor.replace(".-", "");
				} else {
					// Someting new.
					startswith = lookfor.replace("-", "");
				}

				final String regextofind = tempregex.replace("XXXXXXXXXXXXXXXXXXXX", startswith);

				List<EspAbstractJob> data = this.getEspmodel().getBaseObjectsNameBeginsWith(startswith);

				// ONLY JOBS
				List<EspAbstractJob> matchingdata = data.stream().filter(f -> !f.isGroup() && RegexHelper.matchesRegexPattern(f.getName(), regextofind)).collect(Collectors.toList());

				if (!matchingdata.isEmpty()) {
					mydeps.addAll(matchingdata);
				} else {
					log.error("DependencyGraphMapper.doHandleNotWithStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate Jobs starting with [" + startswith + "]");
				}
			} else {

				// No group, just jobs so this should be my jobs in my group. not any other location

				final EspJobGroup myparent = (EspJobGroup) esp.getParent();

				// ONLY jobs.
				EspAbstractJob foundjob = (EspAbstractJob) myparent.getChildren().stream().filter(c -> c.getName().equalsIgnoreCase(lookfor)).findFirst().orElse(null);

				if (foundjob == null) {
					foundjob = this.getEspmodel().getBaseObjectByName(lookfor);
				}

				if (foundjob != null) {
					mydeps.add(foundjob);
				} else {
					log.error("DependencyGraphMapper.doHandleNotWithStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + lookfor + "]");
				}

			}

			mydeps.forEach(mydep -> {

				BaseCsvJobObject jobDependency = this.getDatamodel().findFirstJobByFullPath(mydep.getFullPath());

				if (jobDependency != null) {
					log.debug("DependencyGraphMapper.doHandleNotWithStatements Registering Dependency for Job[" + me.getFullPath() + "] the ohter job must not be active [" + jobDependency.getFullPath() + "]");

					this.getDatamodel().addJobDependencyForJob(me, jobDependency, DepLogic.MATCH, Operator.NOT_EQUAL, DependentJobStatus.ACTIVE, null);

					handleSleepjob(me, jobDependency);

				} else {
					log.error("DependencyGraphMapper.doHandleNotWithStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + mydep.getFullPath() + "]");
				}

			});

		});

	}

	private void doHandleAfterStatements(BaseCsvJobObject me, EspAbstractJob esp) {

		if (me.getName().contains("BRMDLYOFF.AKNITS")) {
			me.getName();
		}

		List<EspAfterStatement> espAfterStatements = esp.getStatementObject().getEspAfterStatements();

		if (espAfterStatements != null && !espAfterStatements.isEmpty()) {

			espAfterStatements.forEach(jobDepName -> {
				if (esp.getParent() == null) {
					return;
				}
				BaseJobOrGroupObject matchingEspJob = esp.getParent().getChildren().stream().filter(job -> job.getName().equals(jobDepName.getJobName())).findFirst().orElse(null);
				if (matchingEspJob != null) {
					BaseCsvJobObject jobDependency = this.getDatamodel().findFirstJobByFullPath(matchingEspJob.getFullPath());
					// status does NOT matter, since this job depends on the OTHER job completing
					// (NO matter what status) -> setting status to NORMAL
					// this.getTidal().addJobDependencyForJobCompletedNormal(me, jobDependency,
					// null);
					if (jobDependency != null) {
						log.debug("DependencyGraphMapper.doHandleAfterStatements Registering Dependency for Job[" + me.getFullPath() + "] this job must be completed [" + jobDependency.getFullPath() + "]");

						this.getDatamodel().addJobDependencyForJob(jobDependency, me, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED, null);

						handleSleepjob(jobDependency, me);

					} else {
						log.error("DependencyGraphMapper.doHandleAfterStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + matchingEspJob.getFullPath() + "]");
					}
				}
			});

		}
	}

	private void doHandleExternalAppIDLogic(BaseCsvJobObject me, EspAbstractJob esp) {

		if (esp.getName().contains("AIPPD040")) {
			esp.getName();
		}
		esp.getExternalApplicationDep().forEach(f -> {
			BaseCsvJobObject jobDependency = null;
			final String externgroup = f.getExternAppID();
			final String externjob = f.getExternJobName();

			if (!StringUtils.isBlank(externgroup)) {
				// Must be a group at all times.
				CsvJobGroup jobgroup = (CsvJobGroup) this.getDatamodel().findGroupByName(externgroup);

				jobDependency = jobgroup;

				if (jobDependency == null) {
					log.error("DependencyGraphMapper.doHandleExternalAppIDLogic Missing Group[" + externgroup + "] Reference for Job[" + me.getFullPath() + "]");
					return;
				}

				if (!StringUtils.isBlank(externjob)) {
					// We have a job to depend on

					BaseCsvJobObject jobdepingroup = (BaseCsvJobObject) jobgroup.getChildren().stream().filter(child -> child.getName().equalsIgnoreCase(externjob)).findFirst().orElse(null);

					if (jobdepingroup != null) {
						jobDependency = jobdepingroup;
					} else {
						log.error("DependencyGraphMapper.doHandleExternalAppIDLogic Missing Job[" + externgroup + "]  In Group Reference for Job[" + me.getFullPath() + "]");
						return;
					}

				} else {
					// Customer requested a report showing all groups that are dependent on other groups.

					if (!esp.getChildren().isEmpty()) {
						// We are a group that is dependent on a group.
						esp.getGroupsToDependOn().add(jobgroup.getFullPath());
						// System.out.println(esp.getFullPath() + " Depends On Group " + jobgroup.getFullPath());
					}
				}
			} else {
				if (!StringUtils.isBlank(externjob)) {
					// No APPID set so this is only to find a job , any job that matches this name.

					BaseCsvJobObject jobinagroup = this.getDatamodel().findFirstJobByName(externjob);

					if (jobinagroup != null) {
						jobDependency = jobinagroup;
					} else {
						log.error("DependencyGraphMapper.doHandleExternalAppIDLogic Missing Job[" + jobinagroup + "] No APPID REF");
						return;
					}
				}
			}

			// status does NOT matter, since this job depends on the OTHER job completing
			// (NO matter what status) -> setting status to NORMAL
			// this.getTidal().addJobDependencyForJobCompletedNormal(me, jobDependency,
			// null);
			if (jobDependency != null) {
				log.debug("DependencyGraphMapper.doHandleAfterStatements Registering Dependency for Job[" + me.getFullPath() + "] this job must be completed [" + jobDependency.getFullPath() + "]");

				this.getDatamodel().addJobDependencyForJob(me, jobDependency, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_NORMAL, null);

				handleSleepjob(me, jobDependency);
			}

		});

	}

	private void handleSleepjob(BaseCsvJobObject currentjob, BaseCsvJobObject targetjob) {

		// We are adding this job to the mix and registarting the target, so we need to exclude this to prevent a loop.
		if (currentjob.getName().startsWith("RELDELAY-")) {
			return;
		}

		// The current job depends on a job that has a sleep job registered, then add that to the dependency too.
		EspOSJOb sleepjob = sleepJobKeyMap.getOrDefault(targetjob.getFullPath(), null);

		if (sleepjob != null) {
			// Add our sleep job to our dep for completed.

			BaseCsvJobObject sleepdepson = this.getDatamodel().findFirstJobByFullPath(sleepjob.getFullPath());

			if (sleepdepson != null) {
				log.debug("DependencyGraphMapper.handleSleepjob Current Job[" + currentjob.getFullPath() + "] Depends on Target Job[" + targetjob.getFullPath() + "] Has a Dependency on RELDELAY Sleep Job[" + sleepdepson.getFullPath() + "]");

				this.getDatamodel().addJobDependencyForJob(currentjob, sleepdepson, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED, null);

			}else {
				log.error("DependencyGraphMapper.handleSleepjob Sleep Job[" + sleepjob.getFullPath() + "] Depends on Target Job[" + targetjob.getFullPath() + "] Unable to locate Sleep Job");

			}
		}

	}

	
	/*
	 * We need to add a depenency to each job that has a sleep job.
	 */
	public void setupSleepDependency() {
		sleepJobKeyMap.keySet().forEach(espkey -> {

			EspOSJOb espsleep = sleepJobKeyMap.get(espkey);

			BaseCsvJobObject keyjob = this.getDatamodel().findFirstJobByFullPath(espkey);
			BaseCsvJobObject mysleepjob = this.getDatamodel().findFirstJobByFullPath(espsleep.getFullPath());

			log.debug("DependencyGraphMapper.setupSleepDependency Registering Dependency for RELDELAY Sleep Job[" + mysleepjob.getFullPath() + "] depends on [" + keyjob.getFullPath() + "]");

			this.getDatamodel().addJobDependencyForJob(mysleepjob, keyjob, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED, null);

		});
	}

	private static Map<String, EspOSJOb> sleepJobKeyMap = new HashMap<>();

	
	/**
	 * We need to register our sleep jobs to our actual job so we can build a dependency for them where Sleep Job dependes on Job
	 * and when another job depends on a job that now has a sleep job, add it to be dependent on the sleep job too.
	 * 
	 * @param job
	 * @param ref
	 */
	public static void registerSleepJobToMap(EspAbstractJob job, EspOSJOb sleepjob) {
		sleepJobKeyMap.put(job.getFullPath(), sleepjob);
		log.debug("DependencyGraphMapper.registerSleepJobToMap Registering RELDELAY Sleep Job[" + sleepjob.getFullPath() + "] for Job[" + job.getFullPath() + "]");

	}

}
