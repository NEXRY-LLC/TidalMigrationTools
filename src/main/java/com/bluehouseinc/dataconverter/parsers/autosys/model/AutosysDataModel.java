package com.bluehouseinc.dataconverter.parsers.autosys.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyJob;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.parsers.autosys.AutoSysConfProvider;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.AutosysBaseDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types.AutosysExitCodeDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types.AutosysJobStatusDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.types.AutosysVariableDependency;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util.AutosysDependencyParserUtil;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util.AutosysJobStatus;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.expressions.ExpressionType;
import com.bluehouseinc.expressions.ExpressionUtil;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.model.dependency.job.DepLogic;
import com.bluehouseinc.tidal.api.model.dependency.job.DependentJobStatus;
import com.bluehouseinc.tidal.api.model.dependency.job.ExitCodeOperator;
import com.bluehouseinc.tidal.api.model.dependency.job.Operator;
import com.bluehouseinc.tidal.utils.DependencyBuilder;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AutosysDataModel extends BaseParserDataModel<AutosysAbstractJob,AutoSysConfProvider> implements IParserModel {

	final DependencyBuilder dependencyBuilder;

	// @Autowired
	// private AutosysDependencyParserUtil autosysDependencyParserUtil;

	public AutosysDataModel(ConfigurationProvider cfgProvider) {
		super(new AutoSysConfProvider(cfgProvider));
		this.dependencyBuilder = new DependencyBuilder();
	}



	@Override
	public BaseVariableProcessor<AutosysAbstractJob> getVariableProcessor(TidalDataModel model) {
		return new AutoSysVariableProcessor(model);
	}

	@Override
	public ITransformer<List<AutosysAbstractJob>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return new AutosysToTidalTransformer(model, this);
	}

	@Override
	public void doProcessData(List<AutosysAbstractJob> dataObjects) {


		long startTime = System.currentTimeMillis(); // debugging
		dataObjects.forEach(this::doProcessJobDeps);
		long endTime = System.currentTimeMillis(); // debugging
		long jobDependencyProcessingTime = (endTime - startTime) / 1000; // debugging
		log.info("jobDependencyProcessingTime={} seconds", jobDependencyProcessingTime); // debugging

		startTime = System.currentTimeMillis(); // debugging
		// processAutosysBoxSuccessJobDependencies(dataObjects); // FIXME: Modify this
		// method logic for faster processing
		endTime = System.currentTimeMillis(); // debugging
		long boxJobSuccessDependencyProcessingTime = (endTime - startTime) / 1000; // debugging
		log.info("boxJobSuccessDependencyProcessingTime={} seconds", boxJobSuccessDependencyProcessingTime); // debugging
	}

	public void processAutosysBoxSuccessJobDependencies(List<AutosysAbstractJob> dataObjects) {
		List<AutosysAbstractJob> autosysBoxJobs = dataObjects.stream().filter(AutosysAbstractJob::isGroup).collect(Collectors.toList());

		// TODO: Pay attention to DepLogicType (ALL/ATLEASTONE/COMPOUND) when converting
		// dependency objects
		// TODO: Logic must be changed in order for BOX job type to target yet to be
		// parsed job dependency by its (dependency's) name.
		// Therefore it is necessary to look up job from dataObjects collection (since
		// all job definitions have been processed).

	}

	// s(#1) v(#5)
	public void doProcessJobDeps(AutosysAbstractJob autosysAbstractJob) {
		BaseCsvJobObject me = this.getTidal().findFirstJobByFullPath(autosysAbstractJob.getFullPath());
		if (me == null) {
			log.error("Unable to locate job[{}]", autosysAbstractJob.getFullPath());
		}
		log.debug("[doProcessJobDeps] me={}", (me == null ? "null" : me.getFullPath()));

		doProcessAutosysBaseDependency(autosysAbstractJob, me);

		autosysAbstractJob.getChildren().forEach(job -> {
			AutosysAbstractJob currentAutosysAbstractJob = (AutosysAbstractJob) job;
			log.debug("[doProcessJobDeps] autosysAbstractJob.getName={}", autosysAbstractJob.getName());
			log.debug("[doProcessJobDeps] currentAutosysAbstractJob.getName={}", currentAutosysAbstractJob.getName());
			doProcessJobDeps((AutosysAbstractJob) job);
		});
	}

	// From here or the other places we deal with dependency we should be able to detect a FileTrigger type
	// add use that data to build a new file dependency to my targetJob.
	private void doProcessAutosysBaseDependency(final AutosysAbstractJob sourceJob, BaseCsvJobObject targetJob) {


		final String expresiondata = sourceJob.getCondition();

		if (StringUtils.isBlank(expresiondata)) {
			return; // Nothing to process we dont have any expresions to process.
		}

		/*
		 * `autosysConditionExpressionMap` holds reference to parsed
		 * compound-dependencies expression as String which is referenced by
		 * AutosysAbstractJob. Holds String value: s(#0) & (e(#1) | t(#2)) | v(#3)
		 * ...which was parsed from following original expression (AUTOSYS `condition`
		 * job property): s(jobName1) & (e(job_name2) <= 1542 | t(jobName3)) |
		 * v(variable_name) = "some_value"
		 */
		HashMap<Integer, Map<Integer, AutosysBaseDependency>> localMap = AutosysDependencyParserUtil.getMapByJobType(sourceJob);

		// This is the current job list of dependencies. From Local Map for reading, we
		// MUST have data if we have expression data.
		Map<Integer, AutosysBaseDependency> mapOfJobDep = localMap.get(sourceJob.getId());


		if (sourceJob.getName().equals("DHP_EDM_PROD_6100_020.ETS_837_ENC_OUT")) {
			sourceJob.getName();
			sourceJob.getName();
			//
		}


		if (mapOfJobDep != null) {

			if (!mapOfJobDep.isEmpty()) {

				targetJob.setCompoundDependency(expresiondata); // Set me to this so we can replace with real data later.

				// We have depenencies to work with.
				// Contains Autosys dependency ID's and we need to lookup the csv job, create
				// new dependency object and replace iD's

				// We better have an expression to work with containing ALL the ID's of our map
				// of deps.

				mapOfJobDep.entrySet().forEach(f -> {

					final AutosysAbstractJob me = sourceJob;
					
					AutosysBaseDependency autoSysBaseDepObject = f.getValue();

					String dependsOnThisJobObjectName = autoSysBaseDepObject.getDependencyName();



					log.debug("[doProcessJobDeps] looking for our dependent job=[{}] ", dependsOnThisJobObjectName);
					// We should find a job from our parent at all times.
					BaseJobOrGroupObject dependsOnThisAutoSysJob = getJobByName(dependsOnThisJobObjectName);

					if (dependsOnThisAutoSysJob == null) {
						log.info("[doProcessJobDeps] missing job in our AutoSys Data, looking for a job with this name["+dependsOnThisJobObjectName+"]");
						//throw new TidalException("[doProcessJobDeps] missing job in our AutoSys Data, looking for a job with this name["+dependsOnThisJobObjectName+"]");
					} else {
						BaseCsvJobObject dependsOnThisRealCsvJob = this.getTidal().findFirstJobByFullPath(dependsOnThisAutoSysJob.getFullPath());

						if (dependsOnThisRealCsvJob == null) {
							// Major issues, we should always find a job matching by name for Autosys.
							log.info("[doProcessJobDeps] missing dependenct job["+dependsOnThisAutoSysJob.getFullPath()+"] in TIDAL");
						} else {

							if (autoSysBaseDepObject instanceof AutosysJobStatusDependency) {

								// TODO: Need to set the actaul job status and other details, this is just a normal job dep.
								AutosysJobStatusDependency jdep = (AutosysJobStatusDependency) autoSysBaseDepObject;

								// TODO: Map from AutosysJobStatus to DependentJobStatus.COMPLETED_NORMAL
								DependentJobStatus usestatus = null;
								Operator oper = Operator.EQUAL;

								if (jdep.getStatus() == AutosysJobStatus.SUCCESS) {
									usestatus = DependentJobStatus.COMPLETED_NORMAL;
								} else if (jdep.getStatus() == AutosysJobStatus.DONE) {
									usestatus = DependentJobStatus.COMPLETED;
								}
								if (jdep.getStatus() == AutosysJobStatus.FAILURE) {
									usestatus = DependentJobStatus.COMPLETED_ABNORMAL;
								}
								if (jdep.getStatus() == AutosysJobStatus.TERMINATED) {
									usestatus = DependentJobStatus.TERMINATED;
								}
								if (jdep.getStatus() == AutosysJobStatus.NOTRUNNING) {
									usestatus = DependentJobStatus.RUNNING;
									oper = Operator.NOT_EQUAL;
								}

								// jdep.getStatus().DONE ;
								CvsDependencyJob csvjobdep = getTidal().addJobDependencyForJob(targetJob, dependsOnThisRealCsvJob, DepLogic.MATCH, oper, usestatus, null);

								// CvsDependencyJob csvjobdep = getTidal().addJobDependencyForJobCompletedNormal(targetJob,dependsOnThisRealCsvJob,null);

								String tempdata = targetJob.getCompoundDependency();

								tempdata = tempdata.replace(Integer.toString(autoSysBaseDepObject.getId()), Integer.toString(csvjobdep.getId()));

								targetJob.setCompoundDependency(tempdata);

							} else if (autoSysBaseDepObject instanceof AutosysVariableDependency) {

								AutosysVariableDependency vdep = (AutosysVariableDependency) autoSysBaseDepObject;

								String variableName = vdep.getDependencyName(); // Think this is a variable

								// TODO: Code not completed for variables.

							} else if (autoSysBaseDepObject instanceof AutosysExitCodeDependency) {

								AutosysExitCodeDependency edep = (AutosysExitCodeDependency) autoSysBaseDepObject;

								CvsDependencyJob csvjobdep = getTidal().addJobDependencyForJobCompletedNormal(targetJob, dependsOnThisRealCsvJob, null);

								csvjobdep.setExitCodeOperator(ExitCodeOperator.EQ);
								csvjobdep.setExitcodeStart(edep.getExitCode());
								csvjobdep.setExitcodeEnd(edep.getExitCode());

								String tempdata = targetJob.getCompoundDependency();

								tempdata = tempdata.replace(Integer.toString(autoSysBaseDepObject.getId()), Integer.toString(csvjobdep.getId()));

								targetJob.setCompoundDependency(tempdata);
							}

						}

					}
				});

				// Setup and register the compound deps if needed.
				registerCompoundDep(targetJob);

			} else {
				if (!StringUtils.isBlank(expresiondata)) {
					// We have an issue.. We do not have any data matching but do have an expression of data.
					// TODO: Throw exception here.
					log.info("[doProcessJobDeps] missing job in our map but has this condition={}", expresiondata);
				}
			}
		} else {
			// This job has no dependency?
			// TODO: Throw exception here.
			log.info("[doProcessJobDeps] missing job in our map but has this condition={}", expresiondata);
		}

	}

	private void registerCompoundDep(BaseCsvJobObject targetJob) {

		final String expresiondata = targetJob.getCompoundDependency();

		if (StringUtils.isBlank(expresiondata)) {
			return; // Nothing to process we dont have any expresions to process.
		}

		List<String> deplistdata = targetJob.getCompoundDependencyBuilder().getExpressionList();
		int datalen = deplistdata.size();

		if (datalen == 1) {
			// Single depedency in the expression data.
			targetJob.setDependencyOrlogic(false);
		} else {

			boolean isAllAnds = ExpressionUtil.isExpressionOfType(ExpressionType.and, targetJob.getCompoundDependencyBuilder().getExpression());
			boolean isAllOrs = ExpressionUtil.isExpressionOfType(ExpressionType.or, targetJob.getCompoundDependencyBuilder().getExpression());

			if (isAllAnds) { // single sized elements, no need to go any deeper
				// TODO: Look at refactoring this code but for now it works.
				// if the deps are all ands, then ignore it and just set the Or logic to false
				targetJob.setDependencyOrlogic(false);
			} else if (isAllOrs) {
				// if all or's then just set the Or logic to true.
				targetJob.setDependencyOrlogic(true);
			} else {
				// BUT IF WE ARE Setting up a compound dep, then lets process correctly
				// mainjob.setCompoundDependency(depbuilder.toString());

				// If we set this we ignore the two above as we are a complex type.
				targetJob.setCompoundDependencyStringFromBuilder();
				// Register the job has having compound dependencies. OR addJob to model AFTER you setup your compound dependencies.
				getTidal().registerCompoundDependencyJob(targetJob);
			}
		}
	}

	/**
	 * Locate a parent object via name, returns the first one found.
	 *
	 * @param name
	 * @return
	 */
	public AutosysAbstractJob getJobByName(String name) {
		if (name == null) {
			return null;
		}

		List<BaseJobOrGroupObject> objs = ObjectUtils.toFlatStream(this.getDataObjects()).filter(f -> f.getName().trim().toLowerCase().equals(name.trim().toLowerCase())).collect(Collectors.toList());

		if (objs.isEmpty()) {
			return null;
		}

		if (objs.size() > 1) {
			log.info("MULTIPLE[{}] JOBS WITH NAME[{}] returning last in the list", objs.size(), name);
			objs.forEach(baseJobOrGroupObject -> log.info("{}", baseJobOrGroupObject.getFullPath()));
			long count = objs.size();

			return (AutosysAbstractJob) objs.stream().skip(count - 1).findFirst().get();
		} else {
			return (AutosysAbstractJob) objs.get(0);
		}
	}

	public AutosysAbstractJob findFirstJobByFullPath(String path) {
		return ObjectUtils.toFlatStream(this.getDataObjects()).filter(f -> f.getFullPath().equals(path)).findFirst().orElse(null);
	}


}
