package com.bluehouseinc.dataconverter.parsers.tivoli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.CpuData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.TivoliCPUProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject.TaskType;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.ResourceData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.TivoliResourceProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.SchedualData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.TivoliScheduleProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.variable.TivoliVariableProcessor;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

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
		if(foldername.isDirectory()) {
			String found = Arrays.asList(foldername.list()).stream().filter(f -> f.toLowerCase().contains(filename.toLowerCase())).findFirst().orElse(null);
		
		
			if(!StringUtils.isBlank(found)) {
				return new File(foldername.getAbsolutePath()+"/"+found);
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

		if(foldername!= null) {
			// Try to override and find our files. 
			cpu = toFile(foldername,"cpu");
			cal = toFile(foldername,"ca"); 
			job = toFile(foldername,"job"); 
			par = toFile(foldername,"parms"); 
			res = toFile(foldername,"resource"); 
			sch = toFile(foldername,"schedule"); 
			var = toFile(foldername,"var"); 
		}

		cpu_processor = new TivoliCPUProcessor();
		cpu_processor.doProcessFile(cpu);
		// cpu_processor.getData().forEach(System.out::println);

		var_processor = new TivoliVariableProcessor();
		var_processor.doProcessFile(var);
		var_processor.getData().stream().forEach(f ->{
			getParserDataModel().getTidal().addVariable(new CsvVariable(f.getName(), f.getValue()));
		});

		
		res_processor = new TivoliResourceProcessor();
		res_processor.doProcessFile(res);
		
		sch_processor = new TivoliScheduleProcessor();
		sch_processor.doProcessFile(sch);

		job_processor = new TivoliJobProcessor(cpu_processor, res_processor);
		job_processor.doProcessFile(job);
		
		
		job_processor.getData().size();
	}


	@Override
	public IModelReport getModelReporter() {
		return null;
	}

}
