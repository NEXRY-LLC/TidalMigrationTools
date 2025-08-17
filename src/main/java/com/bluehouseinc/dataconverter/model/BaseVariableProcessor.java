package com.bluehouseinc.dataconverter.model;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.importers.AbstractCsvImporter;
import com.bluehouseinc.dataconverter.model.csv.NameNewNamePairCsvMapping;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public abstract class BaseVariableProcessor<E extends BaseJobOrGroupObject> {

	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private List<NameNewNamePairCsvMapping> variableReplacements;

	TidalDataModel model;

	public BaseVariableProcessor(TidalDataModel model) {
		this.model = model;
	}

	protected void addGlobalVariable(String name) {
		CsvVariable csvVariable = new CsvVariable(name);
		this.model.addVariable(csvVariable);
	}

	@SuppressWarnings("unchecked")
	public void processJob(E job) {

		//if(job.getName().contains("LAWEXP_RTI_APPMT_LX")) {
		//	job.getName();
		//}
		
		List<Field> rawfields = ObjectUtils.getAllKnownFields(job).stream().filter(f -> f.getType().isInstance(new String())).collect(Collectors.toList());

		doProcessStringFields(job, rawfields);

		processJobVariables(job);

		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(c -> processJob((E) c));
		}

	}

	public abstract void processJobVariables(E job);

	protected void doProcessStringFields(E job, List<Field> fields) {
		fields.forEach(f -> {
			// do we contain our variable

			if (f.getType().isInstance(new String())) {
				if (!Modifier.isFinal(f.getModifiers())) {
					// Only care about string types.
					f.setAccessible(true);

					try {
						String data = (String) f.get(job);

						if (data != null) {

							// Must have a variable.
							data = fromString(data);

							f.set(job, data); // Set new data

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

	/**
	 * Handle logic of mapping one value to another using java built in Regex
	 *
	 * @param input
	 * @return
	 */
	public String fromString(String input) {
		if (input == null) {
			return input;
		}

		String variablefile = this.getModel().getVariableMappingDataFile();

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
						log.debug("fromString INPUT[" + input + "] CONTAINS [" + replace + "] replacing with [" + replacewith + "]");
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

		return input;
	}
}
