package com.bluehouseinc.dataconverter.util;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bluehouseinc.dataconverter.importers.AbstractCsvImporter;
import com.bluehouseinc.dataconverter.model.csv.NameNewNamePairCsvMapping;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public class VariableMapUtil {
	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private ConfigurationProvider cfgProvider;

	static final String TIDALMapVariableDataFile = "TIDAL.MapVariableDataFile";

	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private List<NameNewNamePairCsvMapping> variableReplacements;

	public VariableMapUtil(ConfigurationProvider cfgProvider) {
		this.cfgProvider = cfgProvider;
		this.variableReplacements = null;
	}

	public String getReplacementValue(String input) {
		if (input == null) {
			return input;
		}

		String variablefile = this.getCfgProvider().getConfigurations().getOrDefault(TIDALMapVariableDataFile, null);

		if (variablefile != null) {

			if (variableReplacements == null) {
				File file = new File(variablefile);
				variableReplacements = AbstractCsvImporter.fromFile(file, NameNewNamePairCsvMapping.class);
			}

			if (!variableReplacements.isEmpty()) {

				// old school
				for (NameNewNamePairCsvMapping data : variableReplacements) {
					String lookfor = data.getName();
					String replacewith = data.getNewName();

					Pattern pattern = Pattern.compile(lookfor);
					Matcher matcher = pattern.matcher(input);

					if (matcher.matches()) {

						int cnt = matcher.groupCount();

						if (cnt > 0) {
							String replace = matcher.group(1);
							log.debug("getReplacementValue INPUT[" + input + "] CONTAINS [" + replace + "] replacing with [" + replacewith + "]");
							input = input.replace(replace, replacewith);
							input.chars();

						} else {
							// Literal replace.
							input = input.replace(lookfor, replacewith);
							input.chars();
						}
					}
				}
			}

		} else {
			throw new TidalException("Missing Property:" + TIDALMapVariableDataFile);
		}

		return input;
	}
}
