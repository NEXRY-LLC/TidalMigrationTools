package com.bluehouseinc.dataconverter.parsers.tivoli.data.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.CpuData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.TivoliCPUProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject.TaskType;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.ResourceData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.TivoliResourceProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.variable.TivoliVariableProcessor;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Component
public class TivoliJobProcessor {

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private String JOB_LINE_PATTERN = "^(\\w+)#(\\w+)";

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	Map<String, List<TivoliJobObject>> jobData = new HashMap<>();

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	TivoliCPUProcessor cpuProcessor;

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	TivoliResourceProcessor resProcessor;
	
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	TivoliVariableProcessor variableProcessor;

	public TivoliJobProcessor(TivoliCPUProcessor cpu, TivoliResourceProcessor res, TivoliVariableProcessor var) {
		this.cpuProcessor = cpu;
		this.resProcessor = res;
		this.variableProcessor = var;
	}

	public void doProcessFile(File datafile) {

		BufferedReader reader = null;

		try {
			if (datafile == null) {
				throw new TidalException("Missing Job file");
			}

			reader = new BufferedReader(new FileReader(datafile));

			String line;

			while ((line = EspFileReaderUtils.readLineTrimmed(reader)) != null) {

				line.trim();

				if (EspFileReaderUtils.skippedLine(line)) {
					continue;
				}

				if (isJobPattern(line)) {

					String groupName = RegexHelper.extractNthMatch(line, JOB_LINE_PATTERN, 0);
					String jobname = RegexHelper.extractNthMatch(line, JOB_LINE_PATTERN, 1);

					CpuData cpu = cpuProcessor.getCPUByName(groupName);

					processJobData(reader, jobname, groupName, cpu);

				}
			}
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {

			}
		}
	}

	private boolean isJobPattern(String line) {
		return RegexHelper.matchesRegexPattern(line, JOB_LINE_PATTERN);
	}

	private void processJobData(final BufferedReader reader, String jobname, String groupname, CpuData cpu) throws IOException {

		TivoliJobObject job = new TivoliJobObject();
		job.setName(jobname);

		if (this.jobData.containsKey(groupname)) {
			this.jobData.get(groupname).add(job);
		} else {
			List<TivoliJobObject> joblist = new ArrayList<>();
			joblist.add(job);
			this.jobData.put(groupname, joblist);
		}

		job.setCpuData(cpu);

		ResourceData resdata = resProcessor.getResourceInGroupByName(groupname, jobname);

		job.setResourceData(resdata);

		List<String> lines = EspFileReaderUtils.parseJobLines(reader, "", null, false, true);

		for (String dataline : lines) {

			String data[] = dataline.split(" ", 2);
			String element = data[0];
			String value = null;

			if (data.length >= 2) {
				value = data[1].trim();
			}

			value = EspFileReaderUtils.trimCharBeginOrEnd('"', value);

			switch (element) {
			case "DOCOMMAND":
				job.setDoCommand(value);
				break;
			case "DESCRIPTION":
				job.setDescription(value);
				break;
			case "STREAMLOGON":
				// Tidal does not support variables for runtime users so we will map them
				if (value.contains("^")) {
					value = value.replace("^", "");
					value = this.getVariableProcessor().getValueByName(value);
				}

				job.setStreamLogon(value);

				break;
			case "TASKTYPE":
				job.setTaskType(TaskType.valueOf(value));
				break;
			case "RECOVERY":
				job.setRecovery(value);
				break;
			case "RCCONDSUCC":
				// Use this to set exit codes
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

	public List<TivoliJobObject> getJobDataByGroupName(String group) {
		return this.jobData.get(group);
	}

	public TivoliJobObject getJobInGroupByName(String group, String name) {

		List<TivoliJobObject> jobdata = getJobDataByGroupName(group);

		if (jobdata == null) {
			return null;
		}
		return jobdata.stream().filter(f -> f.getName().trim().equalsIgnoreCase(name.trim())).findFirst().orElse(null);
	}
	
	public TivoliJobObject getFirstJobFoundByName(String name) {

		for(String key : this.jobData.keySet()) {
			TivoliJobObject isfound = getJobDataByGroupName(key).stream().filter(c -> c.getName().trim().equalsIgnoreCase(name.trim())).findFirst().orElse(null);
		
			if(isfound!=null) {
				return isfound;
			}
		}

		return null;
	}
			
			
}
