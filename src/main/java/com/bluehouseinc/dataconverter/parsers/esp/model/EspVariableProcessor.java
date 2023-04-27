package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.util.ObjectUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EspVariableProcessor extends BaseVariableProcessor<EspAbstractJob> {

	private static final String GLOBAL_VARIABLE_PATTERN = ".*?\\!(\\w+).*?";
	private static final Pattern PATTERN = Pattern.compile(GLOBAL_VARIABLE_PATTERN);

	
	public EspVariableProcessor(TidalDataModel model) {
		super(model);
	}

	@Override
	public void processJobVariables(EspAbstractJob job) {

		// This will do all in a generic / reflection way vs individual getters.. This also covers any changes to the classes!!!
		List<Field> rawfields = ObjectUtils.getAllKnownFields(job).stream().filter(f -> f.getType().isInstance(new String())).collect(Collectors.toList());

		convertReplaceVariblesToTidal(job, rawfields);

	}

	
	protected void convertReplaceVariblesToTidal(EspAbstractJob job, List<Field> fields) {

		//Now lets process these fields to change over the global variables into TIDAL variables!!!
		fields.forEach(f -> {
			if (f.getType().isInstance(new String())) {
				if (!Modifier.isFinal(f.getModifiers())) {
					// Only care about string types.
					f.setAccessible(true);

					try {
						String rawdata = (String) f.get(job);

						if (rawdata != null) {

							// Must have a variable.
							rawdata = convertReplaceVariblesToTidal(rawdata, job);

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

	private String convertReplaceVariblesToTidal(String rawValue, EspAbstractJob job) {
		String newValue = rawValue;
		if (newValue != null) {
			if (newValue.contains("!")) {
				newValue = convertRegisterGlobalVariablesToTidal(rawValue);
			}
		}
		return newValue;
	}

	private String convertRegisterGlobalVariablesToTidal(String expression) {
		
		//RegexHelper.matchesRegexPattern(GLOBAL_VARIABLE_PATTERN, expression);
		
		Matcher matcher = PATTERN.matcher(expression);

		while (matcher.find()) {
			String variableName = matcher.group(1);

			addGlobalVariable(variableName);
			// MUST set these up to match what TIDAL is looking for.. E.G $$VARNAME$$
			expression =expression.replace("!"+variableName, "$$"+variableName+"$$");
		}

		return expression;
	}
}
