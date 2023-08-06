package com.bluehouseinc.dataconverter.parsers.tivoli;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.TivoliCPUProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.TivoliResourceProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.SchedualData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.TivoliScheduleProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.job.JobScheduleData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.variable.TivoliVariableProcessor;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TivoliParser extends AbstractParser<TivoliDataModel> {

	public TivoliParser(ConfigurationProvider cfgProvider) {
		super(new TivoliDataModel(cfgProvider));
	}

	TivoliCPUProcessor cpu_processor;
	TivoliResourceProcessor res_processor;
	TivoliVariableProcessor var_processor;
	TivoliScheduleProcessor sch_processor;
	TivoliJobProcessor job_processor;

	private File toFile(File foldername, String filename) {
		if (foldername.isDirectory()) {
			String found = Arrays.asList(foldername.list()).stream().filter(f -> f.toLowerCase().contains(filename.toLowerCase())).findFirst().orElse(null);

			if (!StringUtils.isBlank(found)) {
				return new File(foldername.getAbsolutePath() + "/" + found);
			}
		}

		return null;

	}

	@Override
	public void parseFile() throws Exception {
		// I guess we can move away from passing a single file path,
		// and instead define path key(s) inside each parser
		// Map<String, String> params = this.parseParams();
		// this.parseJobs();

		File cpu = getParserDataModel().getConfigeProvider().getCPUFile();
		File cal = getParserDataModel().getConfigeProvider().getCalendarFile();
		File job = getParserDataModel().getConfigeProvider().getJobFile();
		File par = getParserDataModel().getConfigeProvider().getParamsFile();
		File res = getParserDataModel().getConfigeProvider().getResourceFile();
		File sch = getParserDataModel().getConfigeProvider().getScheduleFile();
		File var = getParserDataModel().getConfigeProvider().getVariableFile();

		File foldername = getParserDataModel().getConfigeProvider().getFolderName();

		if (foldername != null) {
			// Try to override and find our files.
			cpu = toFile(foldername, "cpu");
			cal = toFile(foldername, "ca");
			job = toFile(foldername, "job");
			par = toFile(foldername, "parms");
			res = toFile(foldername, "resource");
			sch = toFile(foldername, "schedule");
			var = toFile(foldername, "var");
		}

		cpu_processor = new TivoliCPUProcessor();
		cpu_processor.doProcessFile(cpu);

		var_processor = new TivoliVariableProcessor();
		var_processor.doProcessFile(var);
		var_processor.getData().stream().forEach(f -> {
			getParserDataModel().getTidal().addVariable(new CsvVariable(f.getName(), f.getValue()));
		});

		res_processor = new TivoliResourceProcessor();
		res_processor.doProcessFile(res);

		job_processor = new TivoliJobProcessor(cpu_processor, res_processor);
		job_processor.doProcessFile(job);

		sch_processor = new TivoliScheduleProcessor(job_processor);
		sch_processor.doProcessFile(sch);

		sch_processor.getScheduleData().keySet().forEach(key -> {
			doProcessSchedualData(sch_processor.getScheduleData().get(key));
		});
	}

	@Override
	public IModelReport getModelReporter() {
		return null;
	}

	/*
	 * This will take the schedules of data to build out groups of data. This is the core of TWS
	 */
	private void doProcessSchedualData(List<SchedualData> data) {

		data.forEach(sched -> {
			String groupname = sched.getWorkflowName();

			if (StringUtils.isBlank(groupname)) {
				throw new TidalException("Workflow name must not be blank");
			}

			TivoliJobObject group = this.getParserDataModel().getJobByName(groupname);

			if (group == null) {
				group = new TivoliJobObject();
				group.setName(groupname);
				group.setGroupFlag(true);
				// ALl our details about our group or schedule in tivoli
				group.setSchedualData(sched);
				log.debug("Adding Group {} to model", groupname);
				this.getParserDataModel().addDataDuplicateLevelCheck(group);
			}

			doProcessJobScheduleDetails(group, sched.getJobScheduleData());
		});
	}

	private void doProcessJobScheduleDetails(TivoliJobObject group, List<JobScheduleData> jobdetails) {

		jobdetails.forEach(jobdetail -> {

			TivoliJobObject tivoli = new TivoliJobObject();

			try {

				// Skip our ID as we need that to not change.
				ObjectUtils.copyMatchingFieldsSkipField(jobdetail.getJob(), tivoli, "id");
				// All our details about this job
				tivoli.setJobScheduleData(jobdetail);
				// Add to our group
				group.addChild(tivoli);
				
				log.debug("Adding Job {} to model", tivoli.getFullPath());

			} catch (IllegalAccessException e) {
				throw new TidalException(e);
			}

		});
	}
}
