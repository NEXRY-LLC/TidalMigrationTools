package com.bluehouseinc.dataconverter.parsers.tivoli;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.JobFollows;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.SchedualData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.job.JobScheduleData;
import com.bluehouseinc.dataconverter.parsers.tivoli.model.TivoliTransformer;
import com.bluehouseinc.dataconverter.parsers.tivoli.model.TivoliVariableProcessor;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.model.dependency.job.DepLogic;
import com.bluehouseinc.tidal.api.model.dependency.job.DependentJobStatus;
import com.bluehouseinc.tidal.api.model.dependency.job.Operator;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@EqualsAndHashCode(callSuper = false)
public class TivoliDataModel extends BaseParserDataModel<TivoliJobObject, TivoliConfigProvider> implements IParserModel {

	public TivoliDataModel(ConfigurationProvider cfgProvider) {
		super(new TivoliConfigProvider(cfgProvider));
	}

	@Override
	public BaseVariableProcessor<TivoliJobObject> getVariableProcessor(TidalDataModel model) {
		return new TivoliVariableProcessor(model);
	}

	@Override
	public ITransformer<List<TivoliJobObject>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return new TivoliTransformer(model);
	}

	@Override
	public void doPostTransformJobObjects(List<TivoliJobObject> jobs) {

	}

	@Override
	public void doProcessJobDependency(List<TivoliJobObject> jobs) {
		jobs.forEach(this::doProcessJobDependency);
	}

	public void doProcessJobDependency(TivoliJobObject job) {

		if (job.getName().contains("ISBONUSL")) {
			job.getName();
		}

		BaseCsvJobObject me = getTidal().findFirstJobByFullPath(job.getFullPath());

		if (me != null) {
			SchedualData data = job.getSchedualData();
			JobScheduleData jobdata = job.getJobScheduleData();


			
			final List<JobFollows> follows = new ArrayList<>();
			final List<String> filedeps = new ArrayList<>();
			
			if (data != null) {
				if (!data.getFollows().isEmpty()) {
					follows.addAll(data.getFollows());
				}
				
				if(!data.getFiledepData().isEmpty()) {
					filedeps.addAll(data.getFiledepData());
				}
			} else if (jobdata != null) {
				if (!jobdata.getFollows().isEmpty()) {
					follows.addAll(jobdata.getFollows());
				}
				
				if(jobdata.getFileDep() != null) {
					filedeps.add(jobdata.getFileDep());
				}
			}

			if (!follows.isEmpty()) {

				follows.forEach(followsjob -> {
					BaseCsvJobObject dependsonjob = null;

					if(followsjob.isIslocal()) {
						// Local to my group so do it that way.
						 List<BaseJobOrGroupObject> childs = me.getParent().getChildren();
						
						dependsonjob = findJobInList(childs,followsjob.getJobToFollow());
					}else {
						
						BaseCsvJobObject container =  getTidal().findGroupByName(followsjob.getInContainer());
						String workflow = followsjob.getInWorkflow();

						dependsonjob = findJobInList(container.getChildren(),workflow);
						
						if(followsjob.isDependsOnGroup()) {
							// Do nothing we are depending on our group
						}else {
							// But if not on group, then a job in this group
							dependsonjob =	findJobInList(dependsonjob.getChildren(),followsjob.getJobToFollow());
						}
					}

					if (dependsonjob != null) {

						if (followsjob.isPreviousDay()) {
							addDepToModel(me, dependsonjob, 1, followsjob.isCompletedOnlyLogic());
						} else {
							addDepToModel(me, dependsonjob, null,followsjob.isCompletedOnlyLogic());
						}
					}else {
						log.info("doProcessJobDependency UNABLE TO BUILD DEPENDENCY FOR{}",followsjob);
					}
				});
			}

			// Add our file dependencies. 
			if(!filedeps.isEmpty()) {
				filedeps.forEach(f ->{
					getTidal().addFileDependencyForJob(me, f);
				});
			}
		}else {
			//TODO: ERROR here, we cant find out dep object.
			log.info("doProcessJobDependency MISSING JOB{}",job.getFullPath());
		}

		job.getChildren().forEach(c -> doProcessJobDependency((TivoliJobObject) c));
	}

	// Can be based on normal or just completed in Tivoli 
	private void addDepToModel(BaseCsvJobObject me, BaseCsvJobObject dependsonjob, Integer offset, boolean iscompletedonlylogic) {
		
		if(iscompletedonlylogic) {
			getTidal().addJobDependencyForJobCompleted(me, dependsonjob, offset);
		}else {
			getTidal().addJobDependencyForJobCompletedNormal(me, dependsonjob, offset);
		}
	}
	
	private BaseCsvJobObject findJobInList(List<BaseJobOrGroupObject> childs, String name) {
		return (BaseCsvJobObject) childs.stream().filter(child -> child.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	@Override
	public void doPostJobDependencyJobObject(List<TivoliJobObject> jobs) {
		// TODO Auto-generated method stub
		
	}
}
