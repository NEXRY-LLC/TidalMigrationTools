package com.bluehouseinc.dataconverter.parsers.autosys.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.parsers.autosys.AutoSysConfProvider;
import com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies.util.AutosysDependencyParserUtil;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.job.RepeatType;
import com.bluehouseinc.tidal.utils.DependencyBuilder;
import com.bluehouseinc.transform.ITransformer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AutosysDataModel extends BaseParserDataModel<AutosysAbstractJob, AutoSysConfProvider> implements IParserModel {

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
	public void doPostTransformJobObjects(List<AutosysAbstractJob> jobs) {
		// Nothing to do here.

	}

	@Override
	public void doProcessJobDependency(List<AutosysAbstractJob> jobs) {
		jobs.forEach(this::doProcessJobDeps);
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
	public void doProcessJobDeps(AutosysAbstractJob sourcejob) {
		BaseCsvJobObject targetjob = this.getTidal().findFirstJobByFullPath(sourcejob.getFullPath());

		if (targetjob == null) {
			log.info("Unable to locate job[{}]", sourcejob.getFullPath());
			//throw new TidalException("Unable to Locate Job[{" + sourcejob.getFullPath() + "}] by Path");
			return;
		}

		log.debug("[doProcessJobDeps] Job={}", targetjob.getFullPath());

		if (targetjob.getName().equals("MMM_QNXT_0180_05.ETS_IN_Script_for_Provider_Sync.FT00")) {
			targetjob.getName();
		}
		AutosysDependencyParserUtil.doProcessJob(sourcejob, targetjob, getTidal(), this);

		sourcejob.getChildren().forEach(job -> {
			log.debug("[doProcessJobDeps] Child Job={}", job.getFullPath());
			doProcessJobDeps((AutosysAbstractJob) job);

		});
	}

	@Override
	public void doPostJobDependencyJobObject(List<AutosysAbstractJob> jobs) {

		if (getConfigeProvider().setRerunOnChildDependencyJobs()) {
			getTidal().getJobOrGroups().forEach(j -> jobdoPostJobDependencyJobObject(j));

		}
	}

	private void jobdoPostJobDependencyJobObject(BaseCsvJobObject job) {

		if (!(job.getRerunLogic().getRepeatType() == RepeatType.NONE)) {
			Map<Integer, Set<BaseCsvJobObject>> downstreamLevels = getTidal().getDependencyProcessor().getDownstreamDependencyLevels(job);

			if (!downstreamLevels.isEmpty() && downstreamLevels.size() > 1) { // Level 0 is the source job itself

				// Iterate through each level (skip level 0 which is the source job)
				for (int level = 1; level < downstreamLevels.size(); level++) {
					Set<BaseCsvJobObject> jobsAtLevel = downstreamLevels.get(level);

					if (jobsAtLevel != null && !jobsAtLevel.isEmpty()) {
						log.debug("Job {} has {} downstream jobs:", job.getFullPath(), jobsAtLevel.size());

						// Process each job at this level
						for (BaseCsvJobObject downstreamJob : jobsAtLevel) {
							if (downstreamJob.getRerunLogic().getRepeatType() == RepeatType.NONE) {
								log.debug("  - {} needs to be flagged for rerun each time dependency are met", downstreamJob.getFullPath());

								// Your rerun logic here
								downstreamJob.setRerundependency(YesNoType.YES);
							}
						}
					}
				}
			}
		}

		job.getChildren().forEach(c -> jobdoPostJobDependencyJobObject((BaseCsvJobObject) c));
	}
}
