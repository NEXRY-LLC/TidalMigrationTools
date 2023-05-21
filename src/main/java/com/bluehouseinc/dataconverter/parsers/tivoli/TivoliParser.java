package com.bluehouseinc.dataconverter.parsers.tivoli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.ResourceData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.TivoliResourceProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.variable.TivoliVariableProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.model.TivoliDataModel;
import com.bluehouseinc.dataconverter.parsers.tivoli.model.TivoliObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.model.TivoliObject.TaskType;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class TivoliParser extends AbstractParser<TivoliDataModel> {

	public TivoliParser(ConfigurationProvider cfgProvider) {
		super(new TivoliDataModel(cfgProvider));
	}

	private String JOB_LINE_PATTERN = "^(\\w+)#(\\w+)";

	TivoliCPUProcessor cpu_processor;
	TivoliResourceProcessor res_processor;

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

		cpu_processor = new TivoliCPUProcessor();
		cpu_processor.doProcessFile(cpu);
		// cpu_processor.getData().forEach(System.out::println);

		TivoliVariableProcessor var_processor = new TivoliVariableProcessor();
		var_processor.doProcessFile(var);

		//this.getParserDataModel().getTidal().addv
		// var_processor.getData().forEach(System.out::println);

		res_processor = new TivoliResourceProcessor();
		res_processor.doProcessFile(res);

		parseJobs(job);
	}

	private void parseJobs(File jobfile) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(jobfile));
		String line;

		// List<TivoliObject> data = new ArrayList<>();

		TivoliObject currentGroup = null;
		while ((line = reader.readLine()) != null) {
			if (line.matches(JOB_LINE_PATTERN)) {
				String groupName = RegexHelper.extractNthMatch(line, JOB_LINE_PATTERN, 0);
				String jobname = RegexHelper.extractNthMatch(line, JOB_LINE_PATTERN, 1);

				if (currentGroup == null || !Objects.equals(groupName, currentGroup.getName())) {
					currentGroup = new TivoliObject();
					currentGroup.setName(groupName);
					currentGroup.setGroupFlag(true);
					CpuData cpu = cpu_processor.getCPUByName(groupName);
					currentGroup.setCpuData(cpu);
					this.getParserDataModel().addDataDuplicateLevelCheck(currentGroup);
				}

				TivoliObject job = new TivoliObject();
				job.setName(jobname);

				currentGroup.addChild(job);

				ResourceData resdata = res_processor.getResourceByGroupName(groupName, jobname);

				if (resdata != null) {
					job.setResourceData(resdata);
				}
				
				List<String> lines = EspFileReaderUtils.parseJobLines(reader, "", null, false);

				for (String dataline : lines) {

					String data[] = dataline.split(" ", 2);
					String element = data[0];
					String value = null;

					if (data.length >= 2) {
						value = data[1].trim();
					}

					switch (element) {
					case "DOCOMMAND":
						job.setDoCommand(value);
						break;
					case "DESCRIPTION":
						job.setDescription(value);
						break;
					case "STREAMLOGON":
						job.setStreamLogon(value);
						break;
					case "TASKTYPE":
						job.setTaskType(TaskType.valueOf(value));
						break;
					case "RECOVERY":
						job.setRecovery(value);
						break;
					case "RCCONDSUCC":
						job.setReturnCodeSucess(value);
						break;
					case "SCRIPTNAME":
						job.setScriptName(value);
						break;
					case "AFTER":
						job.setAfterJob(value);
						break;
					case "ABENDPROMPT":
						job.setAbendPrompt(value);
						break;
					default:
						throw new TidalException("Unknown Data Element: " + dataline);
					}

				}

			}
		}

		reader.close();
	}

	@Override
	public IModelReport getModelReporter() {
		return null;
	}

}
