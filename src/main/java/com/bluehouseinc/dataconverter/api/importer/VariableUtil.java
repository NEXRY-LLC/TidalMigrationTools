package com.bluehouseinc.dataconverter.api.importer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.BaseCvsDependency;
import com.bluehouseinc.dataconverter.model.impl.CsvActionEmail;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyFile;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.model.variable.Variable;

public abstract class VariableUtil {
	private static final Pattern pattern = Pattern.compile("\\$\\$\\w*\\$\\$");

	static void doProcessVariables(BaseCsvJobObject ajob, Collection<Variable> variables) {

		List<Field> fields = ObjectUtils.getAllKnownFields(ajob).stream().filter(f -> f.getType().isInstance(new String())).collect(Collectors.toList());

		doProcessStringFields(ajob, fields, variables);

		ajob.getVariables().forEach(var -> {

			List<Field> varfields = ObjectUtils.getAllKnownFields(var).stream().filter(o -> o.getType().isInstance(new String())).collect(Collectors.toList());

			doProcessStringFields(var, varfields, variables);
		});

		if (!ajob.getChildren().isEmpty()) {
			ajob.getChildren().forEach(c -> doProcessVariables((BaseCsvJobObject) c, variables));
		}
	}

	static void doProcessVariables(CsvActionEmail email, Collection<Variable> variables) {

		List<Field> fields = ObjectUtils.getAllKnownFields(email).stream().filter(f -> f.getType().isInstance(new String())).collect(Collectors.toList());
		doProcessStringFields(email, fields, variables);
	}

	static void doProcessVariables(BaseCvsDependency filedep, Collection<Variable> variables) {
		if (filedep instanceof CvsDependencyFile) {
			List<Field> fields = ObjectUtils.getAllKnownFields(filedep).stream().filter(f -> f.getType().isInstance(new String())).collect(Collectors.toList());
			doProcessStringFields(filedep, fields, variables);
		}
	}

	static void doProcessStringFields(Object ajob, List<Field> fields, Collection<Variable> variables) {
		fields.forEach(f -> {
			// do we contain our variable

			if (f.getType().isInstance(new String())) {
				if (!Modifier.isFinal(f.getModifiers())) {
					// Only care about string types.
					f.setAccessible(true);

					try {
						String data = (String) f.get(ajob);

						if (data != null && data.contains("$$")) {
							// Must have a variable.

							data = doProcessVarValue(data, variables);

							f.set(ajob, data); // Set new data

						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	static String doProcessVarValue(String data, Collection<Variable> variables) {

		Matcher matcher = pattern.matcher(data);
		while (matcher.find()) {
			String key = matcher.group(0);
			key = key.replace("$$", "");
			final String keylookup = key;
			Variable found = variables.stream().filter(v -> v.getName().equalsIgnoreCase(keylookup)).findFirst().orElse(null);

			if (found != null) {
				data = data.replace("$$" + key + "$$", found.getFormatedVariable());

			} else {
				// Throw error.. Variable reference missing in TIDAL
				// throw new RuntimeException("Variable[" + key + "] missing from TIDAL");
			}

		}

		return data;
	}
}
