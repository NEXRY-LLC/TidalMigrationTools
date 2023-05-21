package com.bluehouseinc.dataconverter.parsers.tivoli.data.variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.Data;

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
