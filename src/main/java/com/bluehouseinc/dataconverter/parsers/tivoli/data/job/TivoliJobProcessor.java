package com.bluehouseinc.dataconverter.parsers.tivoli.data.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.CpuData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.TivoliCPUProcessor;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject.TaskType;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.ResourceData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.TivoliResourceProcessor;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.Data;

@Data
@Component
public class TivoliJobProcessor {

	private String JOB_LINE_PATTERN = "^(\\w+)#(\\w+)";

	List<TivoliJobObject> data = new ArrayList<>();;

	TivoliCPUProcessor cpuProcessor;
	TivoliResourceProcessor resProcessor;

	public TivoliJobProcessor(TivoliCPUProcessor cpu, TivoliResourceProcessor res) {
		this.cpuProcessor = cpu;
		this.resProcessor = res;
	}

	public void doProcessFile(File datafile) {

		BufferedReader reader = null;

		try {
			if (datafile == null) {
				throw new TidalException("Missing Job file");
			}

			reader = new BufferedReader(new FileReader(datafile));

			String line;

			TivoliJobObject currentGroup = null;

			while ((line = EspFileReaderUtils.readLineTrimmed(reader)) != null) {

				line.trim();

				if (EspFileReaderUtils.skippedLine(line)) {
					continue;
				}

				if (isJobPattern(line)) {

					String groupName = RegexHelper.extractNthMatch(line, JOB_LINE_PATTERN, 0);
					String jobname = RegexHelper.extractNthMatch(line, JOB_LINE_PATTERN, 1);

					CpuData cpu = cpuProcessor.getCPUByName(groupName);

					if (currentGroup == null || !Objects.equals(groupName, currentGroup.getName())) {
						currentGroup = new TivoliJobObject();
						currentGroup.setName(groupName);
						currentGroup.setGroupFlag(true);
						currentGroup.setCpuData(cpu);
						this.data.add(currentGroup);
						// currentGroup.setResourceData(resdata);
					}

					processJobData(reader, jobname, currentGroup, cpu);

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

	private void processJobData(final BufferedReader reader, String jobname, TivoliJobObject currentGroup, CpuData cpu) throws IOException {

		TivoliJobObject job = new TivoliJobObject();
		job.setName(jobname);

		currentGroup.addChild(job);
		
		job.setCpuData(cpu);

		ResourceData resdata = resProcessor.getResourceByGroupName(currentGroup.getName(), jobname);

		job.setResourceData(resdata);


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
				// Tidal does not support variables for runtime users so we will map them
				job.setStreamLogon(value.replace("^", ""));
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
