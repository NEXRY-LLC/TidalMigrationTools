package com.bluehouseinc.dataconverter.parsers.tivoli.model;

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
import com.bluehouseinc.dataconverter.parsers.tivoli.TivoliConfigProvider;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.JobFollows;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.SchedualData;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TivoliDataModel extends BaseParserDataModel<TivoliObject, TivoliConfigProvider> implements IParserModel {

	public TivoliDataModel(ConfigurationProvider cfgProvider) {
		super(new TivoliConfigProvider(cfgProvider));
	}

	@Override
	public BaseVariableProcessor<TivoliObject> getVariableProcessor(TidalDataModel model) {
		return new TivoliVariableProcessor(model);
	}

	@Override
	public ITransformer<List<TivoliObject>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return new TivoliTransformer(model);
	}

	@Override
	public void doPostTransformJobObjects(List<TivoliObject> jobs) {

	}

	@Override
	public void doProcessJobDependency(List<TivoliObject> jobs) {
		jobs.forEach(this::doProcessJobDependency);
	}

	public void doProcessJobDependency(TivoliObject job) {

		if(job.getName().contains("NANOTERM")) {
			job.getName();
		}
		
		SchedualData data = job.getScheduleData();

		if (data != null) {
			String fullpath = job.getFullPath();
			final BaseCsvJobObject me = getTidal().findFirstJobByFullPath(fullpath);

			if (me == null) {
				return;
			}

			if (!data.getFiledeps().isEmpty()) {

				data.getFiledeps().forEach(f -> {
					getTidal().addFileDependencyForJob(me, f);
				});
			}

			
			List<JobFollows> followsdata = data.getFollows();
			if (!followsdata.isEmpty()) {

				if (!followsdata.isEmpty()) {
					followsdata.forEach(follows -> {
						List<BaseCsvJobObject> depjobs = new ArrayList<>();

						String ingroup = follows.getInGroup();
						String tempjob = follows.getJobToFollow();

						// AMFINAN1#BNS00A.@
						if (tempjob.contains("#")) {
							if (ingroup.equalsIgnoreCase(tempjob.split("#", 2)[0])) {
								// ingroup = thisjob.split("#",2)[0]; // Do we override or simply ignore?
								// Do nothing they are the same.
							} else {
								// SHoudl we override?
								String diffgroup = tempjob.split("#", 2)[0];
								ingroup = diffgroup;
							}

							tempjob = tempjob.split("#", 2)[1]; // Replace with the job
						} else {
							// Do we care?
							tempjob.getBytes();
						}

						boolean jobbeginswith = tempjob.contains(".@");

						if (!StringUtils.isBlank(ingroup)) {

							CsvJobGroup group = getTidal().findGroupByName(ingroup);

							if (jobbeginswith) {

								String jobstart = tempjob.replace(".@", "").trim().toLowerCase();

								List<BaseJobOrGroupObject> matches = group.getChildren().stream().filter(f -> f.getName().toLowerCase().startsWith(jobstart)).collect(Collectors.toList());

								if (!matches.isEmpty()) {
									matches.forEach(m -> {
										BaseCsvJobObject depjob = (BaseCsvJobObject) m;
										depjobs.add(depjob);
									});
								} else {
									// No matches found.
									jobstart.getBytes();
								}
							} else {

								final String finaltempjob = tempjob;
								BaseCsvJobObject deponme = (BaseCsvJobObject) group.getChildren().stream().filter(f -> f.getName().equalsIgnoreCase(finaltempjob)).findAny().orElse(null);

								if (deponme != null) {
									depjobs.add(deponme);
								} else {
									job.getName();
									// ERROR cant find a job using this name in this group
								}
							}

						} else {

							BaseCsvJobObject deponme = getTidal().findFirstJobByFullPath(tempjob);

							if (deponme != null) {
								depjobs.add(deponme);
							} else {
								job.getName();
								// ERROR cant find a job using this name in this group
							}

						}

						if (!depjobs.isEmpty()) {
							depjobs.forEach(d -> {
								getTidal().addJobDependencyForJobCompletedNormal(me, d, null);
							});

						}
					});
				} else {
					job.getName();
					// ERROR unable to find me
				}
			}

			// if(data.)

		}

		job.getChildren().forEach(c -> doProcessJobDependency((TivoliObject) c));
	}

}
