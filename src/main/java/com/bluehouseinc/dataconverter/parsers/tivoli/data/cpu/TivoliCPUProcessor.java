package com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchCalendar;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchComment;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchDString;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchEventElement;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchInvoke;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchNoScheduleAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchResumeAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchScheduleAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchSuspendAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchVsAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchWobtriggerAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Component
public class TivoliCPUProcessor {
	
	private final static String CPU_PATTERN = "CPUNAME (.*)";
	private final static String END_PATTERN = "END";
	

	List<CpuData> data = new ArrayList<>();;



	public void doProcessFile(File datafile) {



		BufferedReader reader = null;

		try {
			if (datafile == null) {
				throw new TidalException("Missing cpu file");
			}

			reader = new BufferedReader(new FileReader(datafile));

			String line;

			while ((line = EspFileReaderUtils.readLineTrimmed(reader)) != null) {

				line.trim();

				if (EspFileReaderUtils.skippedLine(line)) {
					continue;
				}



				if (isCPUPattern(line)) {

					processCPUData(reader, line);

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

	private boolean isCPUPattern(String line) {
		return RegexHelper.matchesRegexPattern(line, CPU_PATTERN);
	}

	private void processCPUData(final BufferedReader reader, String cpuline) throws IOException {

		CpuData cpu = new CpuData();
		
		String name = RegexHelper.extractFirstMatch(cpuline, CPU_PATTERN);
		
		cpu.setName(name);
		
		List<String> lines = EspFileReaderUtils.parseJobLines(reader, END_PATTERN, null);

		for (String line : lines) {

			String data[] = line.split(" ", 2);
			String element = data[0]; 
			String value =  null;
			
			if(data.length >=2) {
				value = data[1].trim(); 
			}


			switch (element) {
			case "DESCRIPTION":
				cpu.setDescription(value);
				break;
			case "OS":
				cpu.setOperatingSystem(value);
				break;
			case "NODE":
				cpu.setNodeName(value.split(" ")[0]);
				break;
			case "DOMAIN":
				cpu.setDomain(value);
				break;
			case "FOR":
				cpu.setForwhat(value);
				break;
			case "TYPE":
				cpu.setType(value);
				break;
			case "AUTOLINK":
				cpu.setAutoLink(value);
				break;
			case "IGNORE":
				cpu.setIgnore(value);
				break;
			case "BEHINDFIREWALL":
				cpu.setBehindFirewall(value);
				break;
			case "FULLSTATUS":
				cpu.setFullStatus(value);
				break;
			default:
				throw new TidalException("Unknown Data Element: " + line );
			}

		}

		data.add(cpu);
	}


	/**
	 * Returns every <J> object in the supplied collection , the Node is recursive
	 * returning all objects.
	 *
	 * @param <J>
	 * @param collection
	 * @param type
	 * @return
	 */
	public static <J extends SchAction> List<J> getActionsByType(SchEventElement element, Class<J> clazz) {
		return element.getActions().stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
	}
}
