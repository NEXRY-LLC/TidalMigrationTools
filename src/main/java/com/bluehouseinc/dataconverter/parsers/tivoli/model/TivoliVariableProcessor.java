package com.bluehouseinc.dataconverter.parsers.tivoli.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TivoliVariableProcessor extends BaseVariableProcessor<TivoliJobObject> {
	final private static String VAR_PATTERN = ".*?(\\^.*?\\^).*?";
	final private static String DOLLAR_PATTERN = "(\\$\\{?)(\\w+)(\\}?)";
	private static final Pattern PATTERN = Pattern.compile(VAR_PATTERN);

	public TivoliVariableProcessor(TidalDataModel model) {
		super(model);
	}

	@Override
	public void processJobVariables(TivoliJobObject job) {

		// This will do all in a generic / reflection way vs individual getters.. This also covers any changes to the classes!!!
		List<Field> rawfields = ObjectUtils.getAllKnownFields(job).stream().filter(f -> f.getType().isInstance(new String())).collect(Collectors.toList());

		convertReplaceVariblesToTidal(job, rawfields);

		// Do we have a variable in our job for a file dep?
		if (job.getJobScheduleData() != null) {
			if (!StringUtils.isBlank(job.getJobScheduleData().getFileDep())) {
				String filedep = job.getJobScheduleData().getFileDep();
				filedep = convertReplaceVariblesToTidal(filedep);
				job.getJobScheduleData().setFileDep(filedep);
			}

		}

		if (job.getSchedualData() != null) {
			if (!job.getSchedualData().getFiledepData().isEmpty()) {
				final List<String> varfiledeps = new ArrayList<>();

				job.getSchedualData().getFiledepData().forEach(f -> {
					f = convertReplaceVariblesToTidal(f);
					varfiledeps.add(f);
				});
				
				job.getSchedualData().getFiledepData().clear();
				job.getSchedualData().getFiledepData().addAll(varfiledeps);
			}
		}
	}

	protected void convertReplaceVariblesToTidal(TivoliJobObject job, List<Field> fields) {

		// Now lets process these fields to change over the global variables into TIDAL variables!!!
		fields.forEach(f -> {
			if (f.getType().isInstance(new String())) {
				if (!Modifier.isFinal(f.getModifiers())) {
					// Only care about string types.
					f.setAccessible(true);

					try {
						String rawdata = (String) f.get(job);

						if (rawdata != null) {

							// Must have a variable.
							rawdata = convertReplaceVariblesToTidal(rawdata);

							f.set(job, rawdata); // Set new data
						}
					} catch (IllegalArgumentException e) {
						log.error(e.getMessage());
					} catch (IllegalAccessException e) {
						log.error(e.getMessage());
					}
				}
			}
		});
	}

	private String convertReplaceVariblesToTidal(String rawValue) {
		String newValue = rawValue;
		if (newValue != null) {
			if(newValue.contains("$")) {
				newValue = convertRegisterLocalVariablesToTidal(rawValue);
			}
			if (newValue.contains("^")) {
				newValue = convertRegisterGlobalVariablesToTidal(rawValue);
			}
		}
		return newValue;
	}

	private String convertRegisterGlobalVariablesToTidal(String expression) {

		// RegexHelper.matchesRegexPattern(GLOBAL_VARIABLE_PATTERN, expression);

		Matcher matcher = PATTERN.matcher(expression);

		while (matcher.find()) {
			String variableName = matcher.group(1);
			String tidalvar_token = variableName.replace("^", "$$");
			String tidalvarname = tidalvar_token.replace("$$", "");
			addGlobalVariable(tidalvarname);
			// MUST set these up to match what TIDAL is looking for.. E.G $$VARNAME$$
			expression = expression.replace(variableName, tidalvar_token);
		}

		return expression;
	}
	
	private String convertRegisterLocalVariablesToTidal(String data) {
		
		if( RegexHelper.matchesRegexPattern(data, DOLLAR_PATTERN)) {
			

			Pattern p = Pattern.compile(DOLLAR_PATTERN);
			Matcher m = p.matcher(data);

			while (m.find()) {
				String variableName = m.group(2);
				String repplacevariable = m.group(1)+m.group(2)+m.group(3);
				addGlobalVariable(variableName);
				//data = m.replaceAll("\\$\\$$2\\$\\$"); // Replace the pattern found with the second group with our $$ tokens for TIDAL
				data = data.replace(repplacevariable, "$$"+variableName+"$$");
				data.trim();
			}

			if(data.contains("$$$$")) {
				// Hack.. 
				data = data.replace("$$$$", "$$").replace("$$$", "$$");
			}
		}
		return data;
	}
}
