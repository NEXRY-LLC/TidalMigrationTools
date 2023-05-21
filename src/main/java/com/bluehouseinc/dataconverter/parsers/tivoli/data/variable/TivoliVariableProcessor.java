package com.bluehouseinc.dataconverter.parsers.tivoli.data.variable;

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
public class TivoliVariableProcessor {

	private final static String VAR_PATTERN = "MAIN_TABLE\\.(.*)";

	List<VariableData> data = new ArrayList<>();;

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

				if (isVariablePattern(line)) {

					String varpattern = RegexHelper.extractFirstMatch(line, VAR_PATTERN);

					String variabledata[] = varpattern.split(" ", 2);

					VariableData variable = new VariableData();
					variable.setName(variabledata[0]);
					if (variabledata.length > 1) {
						variable.setValue(variabledata[1]);
					}

					data.add(variable);
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

	private boolean isVariablePattern(String line) {
		return RegexHelper.matchesRegexPattern(line, VAR_PATTERN);
	}

}
