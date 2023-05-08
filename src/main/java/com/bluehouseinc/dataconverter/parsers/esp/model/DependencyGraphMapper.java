package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.util.List;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspAfterStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspNotWithStatement;
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

	public void doProcessJobDeps(EspAbstractJob esp) {

		if (esp.getName().contains("MTIC_LOADBAT")) {
			esp.getName();
		}

		BaseCsvJobObject me = this.getDatamodel().findFirstJobByFullPath(esp.getFullPath());

		// This job must exists in the system
		if (me == null) {
			// log.error("Unable to locate job[" + esp.getFullPath() + "]");
			throw new RuntimeException("Unable to locate job[" + esp.getFullPath() + "]");
		}

		doHandleExternalAppID(me,esp);
		
		doHandlePrereq(me, esp);

		doHandleReleaseStatements(me, esp);

		doHandleNotWithStatements(me, esp);

		doHandleAfterStatements(me, esp);
	}

	private void doHandlePrereq(BaseCsvJobObject me, EspAbstractJob esp) {
		EspPrereqStatement espPrereqStatement = esp.getPrerequisite();

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
		List<EspReleaseStatement> espReleaseStatements = esp.getEspReleasedJobDependencies();

		if (espReleaseStatements != null && !espReleaseStatements.isEmpty()) { // traverse all jobs which need to wait for CURRENT job to complete

			// For every one of these listed means that these jobs need to depend on me
			espReleaseStatements.forEach(espReleaseStatement -> {

				if (!espReleaseStatement.getReleaseJobs().isEmpty()) {

					List<String> releasedJobNames = espReleaseStatement.getReleaseJobs();

					// Find jobs in our group that I have set a release add statement.. E.G these
					// must wait on me to complete
					List<BaseJobOrGroupObject> jobsToBeReleased = esp.getParent().getChildren().stream().filter(job -> releasedJobNames.contains(job.getName())).collect(Collectors.toList());

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
		esp.getEspNotWithStatements().forEach(statement -> {

			String lookfor = statement.getJobName().trim();

			EspAbstractJob mydep = this.getEspmodel().getBaseObjectByName(lookfor);

			if (mydep != null) {
				BaseCsvJobObject jobDependency = this.getDatamodel().findFirstJobByFullPath(mydep.getFullPath());

				if (jobDependency != null) {
					log.debug("doHandleNotWithStatements Registering Dependency for Job[" + me.getFullPath() + "] the ohter job must not be active [" + jobDependency.getFullPath() + "]");

					this.getDatamodel().addJobDependencyForJob(me, jobDependency, DepLogic.MATCH, Operator.NOT_EQUAL, DependentJobStatus.ACTIVE, null);

				} else {
					throw new TidalException("doHandleNotWithStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + mydep.getFullPath() + "]");
				}

			} else {
				log.debug("doHandleNotWithStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + lookfor + "]");
			}

		});

	}

	private void doHandleAfterStatements(BaseCsvJobObject me, EspAbstractJob esp) {

		List<EspAfterStatement> espAfterStatements = esp.getEspAfterStatements();

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

	private void doHandleExternalAppID(BaseCsvJobObject me, EspAbstractJob esp) {

		String externananme = esp.getExternalAppID();

		if (!StringUtils.isBlank(externananme)) {
			BaseCsvJobObject jobDependency = this.getDatamodel().findFirstJobByFullPath(externananme);
			// status does NOT matter, since this job depends on the OTHER job completing
			// (NO matter what status) -> setting status to NORMAL
			// this.getTidal().addJobDependencyForJobCompletedNormal(me, jobDependency,
			// null);
			if (jobDependency != null) {
				log.debug("doHandleAfterStatements Registering Dependency for Job[" + me.getFullPath() + "] this job must be completed [" + jobDependency.getFullPath() + "]");

				this.getDatamodel().addJobDependencyForJob(jobDependency, me, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_NORMAL, null);

			} else {
				log.debug("doHandleAfterStatements ERROR Unable to set Dependency for Job[" + me.getFullPath() + "] unable to locate [" + externananme + "]");
			}
		}
	}
}
