package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspJobGroup;
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

		if (esp.getName().contains("ZOP_RE_BRU_ZASP_ORDER_LIST_CREATE")) {
			esp.getName();
		}
		if (esp.getName().contains("SUNMAINT")) {
			esp.getName();
		}


		BaseCsvJobObject me = this.getDatamodel().findFirstJobByFullPath(esp.getFullPath());

		// This job must exists in the system
		if (me == null) {
			// log.error("Unable to locate job[" + esp.getFullPath() + "]");
			throw new RuntimeException("Unable to locate job[" + esp.getFullPath() + "]");
		}

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
			log.debug("doHandlePrereq Registering Dependency for Job[" + me.getFullPath() + "] that depends on [" + jobDependency.getFullPath() + "]");
			this.getDatamodel().addJobDependencyForJobCompletedNormal(me, jobDependency, null);
		} else {
			log.debug("doHandlePrereq ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] that depends on [" + depjobName + "]");
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
					if(esp instanceof EspJobGroup) {
						// Now that we put deps on groups, we need to check if we are a group vs job.
						childjobs = esp.getChildren();
					}else {
						childjobs = esp.getParent().getChildren();
					}
					
					List<BaseJobOrGroupObject> jobsToBeReleased = childjobs.stream().filter(job -> releasedJobNames.contains(job.getName())).collect(Collectors.toList());

					// This is the list of jobs that should be dependent on me to completed.
					jobsToBeReleased.forEach(dependsOnMe -> {

						BaseCsvJobObject dependsOnMeDep = this.getDatamodel().findFirstJobByFullPath(dependsOnMe.getFullPath());

						if (dependsOnMeDep != null) {
							log.debug("doHandleReleaseStatements Registering Dependency for Job[" + me.getFullPath() + "] This job depends on me [" + dependsOnMeDep.getFullPath() + "]");

							// We are assuming that if any data is listed its abnormal.
							// Current examples are like this: RELEASE ADD(CDSTAT.BOM1_FOR_F191_CFSCD(A))
							if (StringUtils.isBlank(espReleaseStatement.getCondition())) {
								this.getDatamodel().addJobDependencyForJob(dependsOnMeDep, me, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_NORMAL, null);
							} else {
								this.getDatamodel().addJobDependencyForJob(dependsOnMeDep, me, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_ABNORMAL, null);
							}
						} else {
							log.debug("doHandlePrereq ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + dependsOnMe.getFullPath() + "]");
						}

					});
				}
			});
		}
	}

	private void doHandleNotWithStatements(BaseCsvJobObject me, EspAbstractJob esp) {

		// traversing all jobs which must NOT be active in order for CURRENT job to run
		esp.getStatementObject().getEspNotWithStatements().forEach(statement -> {

			String lookfor = statement.getJobName().trim();

			List<EspAbstractJob> mydeps = new ArrayList<>();

			if (lookfor.contains(".-")) {
				List<EspAbstractJob> founddeps = this.getEspmodel().getBaseObjectsNameBeginsWith(lookfor.replace(".-", ""));

				if (!founddeps.isEmpty()) {
					mydeps.addAll(founddeps);
				} else {
					log.debug("doHandleNotWithStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + lookfor + "]");
				}

			} else {
				EspAbstractJob mydep = this.getEspmodel().getBaseObjectByName(lookfor);
				if (mydep != null) {
					mydeps.add(mydep);
				} else {
					log.debug("doHandleNotWithStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + lookfor + "]");
				}
			}

			mydeps.forEach(mydep -> {

				BaseCsvJobObject jobDependency = this.getDatamodel().findFirstJobByFullPath(mydep.getFullPath());

				if (jobDependency != null) {
					log.debug("doHandleNotWithStatements Registering Dependency for Job[" + me.getFullPath() + "] the ohter job must not be active [" + jobDependency.getFullPath() + "]");

					this.getDatamodel().addJobDependencyForJob(me, jobDependency, DepLogic.MATCH, Operator.NOT_EQUAL, DependentJobStatus.ACTIVE, null);

				} else {
					throw new TidalException("doHandleNotWithStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + mydep.getFullPath() + "]");
				}

			});

		});

	}

	private void doHandleAfterStatements(BaseCsvJobObject me, EspAbstractJob esp) {

		List<EspAfterStatement> espAfterStatements = esp.getStatementObject().getEspAfterStatements();

		if (espAfterStatements != null && !espAfterStatements.isEmpty()) {

			espAfterStatements.forEach(jobDepName -> {
				BaseJobOrGroupObject matchingEspJob = esp.getParent().getChildren().stream().filter(job -> job.getName().equals(jobDepName.getJobName())).findFirst().orElse(null);
				if (matchingEspJob != null) {
					BaseCsvJobObject jobDependency = this.getDatamodel().findFirstJobByFullPath(matchingEspJob.getFullPath());
					// status does NOT matter, since this job depends on the OTHER job completing
					// (NO matter what status) -> setting status to NORMAL
					// this.getTidal().addJobDependencyForJobCompletedNormal(me, jobDependency,
					// null);
					if (jobDependency != null) {
						log.debug("doHandleAfterStatements Registering Dependency for Job[" + me.getFullPath() + "] this job must be completed [" + jobDependency.getFullPath() + "]");

						this.getDatamodel().addJobDependencyForJob(jobDependency, me, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED, null);

					} else {
						log.debug("doHandleAfterStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + matchingEspJob.getFullPath() + "]");
					}
				}
			});

		}
	}

	// FIXME: This code is broken becuase of ESP changes.
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
					log.info("doHandleExternalAppIDLogic Missing Group[" + externgroup + "] Reference for Job[" + me.getFullPath() + "]");
					return;
				}
				
				
				if (!StringUtils.isBlank(externjob)) {
					// We have a job to depend on

					BaseCsvJobObject jobdepingroup = (BaseCsvJobObject) jobgroup.getChildren().stream().filter(child -> child.getName().equalsIgnoreCase(externjob)).findFirst().orElse(null);

					if (jobdepingroup != null) {
						jobDependency = jobdepingroup;
					}else {
						log.error("doHandleExternalAppIDLogic Missing Job[" + externgroup + "]  In Group Reference for Job[" + me.getFullPath() + "]");
						return;
					}
					
				}
			}else {
				if (!StringUtils.isBlank(externjob)) {
					// No APPID set so this is only to find a job , any job that matches this name.
				
					BaseCsvJobObject jobinagroup  = this.getDatamodel().findFirstJobByName(externjob);
					
					if (jobinagroup != null) {
						jobDependency = jobinagroup;
					}else {
						log.error("doHandleExternalAppIDLogic Missing Job[" + jobinagroup + "] No APPID REF");
						return;
					}
				}
			}
			
			
			// status does NOT matter, since this job depends on the OTHER job completing
			// (NO matter what status) -> setting status to NORMAL
			// this.getTidal().addJobDependencyForJobCompletedNormal(me, jobDependency,
			// null);
			if (jobDependency != null) {
				log.debug("doHandleAfterStatements Registering Dependency for Job[" + me.getFullPath() + "] this job must be completed [" + jobDependency.getFullPath() + "]");

				this.getDatamodel().addJobDependencyForJob(me, jobDependency,DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_NORMAL, null);

			}

		});

	}
}
