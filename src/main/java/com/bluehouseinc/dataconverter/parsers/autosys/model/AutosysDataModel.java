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
	public void doProcessJobDeps(AutosysAbstractJob sourcejob) {
		BaseCsvJobObject targetjob = this.getTidal().findFirstJobByFullPath(sourcejob.getFullPath());

		if (targetjob == null) {
			log.info("Unable to locate job[{}]", sourcejob.getFullPath());
			throw new TidalException("Unable to Locate Job[{" + sourcejob.getFullPath() + "}] by Path");
		}

		log.debug("[doProcessJobDeps] me={}", targetjob.getFullPath());

		if(targetjob.getName().equals("MMM_QNXT_0180_05.ETS_IN_Script_for_Provider_Sync.FT00")) {
			targetjob.getName();
		}
		AutosysDependencyParserUtil.doProcessJob(sourcejob, targetjob, getTidal(), this);

		sourcejob.getChildren().forEach(job -> {
			log.debug("[doProcessJobDeps] child={}", job.getFullPath());
			doProcessJobDeps((AutosysAbstractJob) job);

		});
	}

	

}
