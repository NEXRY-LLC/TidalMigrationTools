package com.bluehouseinc.dataconverter.parsers.bmc.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.JobData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.dataconverter.util.VariableMapUtil;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * Must rewrite this using new logic, which was known as we were waiting on
 * functionality.
 *
 * The only thing that needs fixed is that a variable having a reference to
 * variable in the same scope.
 *
 * If for example a variable has a references to both a group and global
 * variable, that is fine but if the variable has reference to a variable at
 * it's same scope (E.G Job variable referencing another job variable) that is
 * not presently supported.
 *
 * @author Brian Hayes
 *
 */
@Log4j2
public class VariableFixUtil {
	private static final Pattern pattern = Pattern.compile("%%\\w*\\.?");
	private static final List<String> globalVariables = new ArrayList<>();
	private static List<String> globalPrefixes = new ArrayList<>();
	private static VariableMapUtil varMapUtil;

	VariableFixUtil() {
	}

	public static void setVariableMapUtil(VariableMapUtil varutil) {
		varMapUtil = varutil;
	}

	public static List<String> getGlobalVariables() {
		return globalVariables;
	}

	// Not sure why I added the split of variables but there are two types.. Actual
	// variables on a job and variables are are specific to the creation of the job
	// I split it for a reason around replacing all the variables with values but we
	// are adding variables logic to the importer and need to translate that here.

	// 1. Technically speaking any job can have local variables but also reference
	// container variables.
	// 2. Right now our biggest issue is nested variables so I need to build
	// replaceable logic for when nested variable support is added to TIDAL. (Was
	// told in 6.5.11, time will tell)

	// Just adding this job to the list for reference lookup.
	// doReplaceJobName(joborfolder.getName(), lookupdata);
	// OR just use <JobName> in TIDAL and be done with it.. I know that memlib can
	// reference JobName but that might be o.k

	public static void doProcessNestedVarFix(BaseBMCJobOrFolder joborfolder, String globallist) {

		// STAGE_SUTTER_INV_FG
		/*
		 * This is not coming over correctly in TIDAL. <VARIABLE
		 * NAME="%%LOCAL_TIME_HOUR" VALUE="%%SUBSTR %%TIME 1 2" /> <VARIABLE
		 * NAME="%%LOCAL_TIME_MINUTE" VALUE="%%SUBSTR %%TIME 3 2" /> <VARIABLE
		 * NAME="%%LOCAL_TIME_SECOND" VALUE="%%SUBSTR %%TIME 5 2" />
		 */

		if (joborfolder.getName().equals("SEDGWICK_GNX_AdjusterNotes")) {
			joborfolder.getName();
		}
		if (globallist != null) {
			globalPrefixes.clear();
			globalPrefixes.addAll(Arrays.asList(globallist.split(",")));
		}

		doVariableMapping(joborfolder);

		doFixLocalVariableNames(joborfolder); // BMC can have names like %%\NAME which is their version of an variable set action in TIDAL

		// THe underlying BMC objecct data for all objects..
		JobData jobdata = joborfolder.getJobData();

		if (jobdata != null) {
			List<Field> rawfields = ObjectUtils.getAllKnownFields(jobdata).stream().filter(f -> f.getType().isInstance(new String())).collect(Collectors.toList());

			doProcessStringFields(jobdata, rawfields, joborfolder.getLocalVariables());
		}

		// Process this model object for any variables
		List<Field> fields = ObjectUtils.getAllKnownFields(joborfolder).stream().filter(f -> f.getType().isInstance(new String())).collect(Collectors.toList());

		doProcessStringFields(joborfolder, fields, joborfolder.getLocalVariables());

		// PRocess the local BMC variables to a job..
		joborfolder.getLocalVariables().forEach(f -> {
			f.setVALUE(replaceNestedVariable(f.getVALUE(), joborfolder.getLocalVariables(), true));
		});

		// PRoces teh variables specific to this job type
		joborfolder.getJobSpecificVariables().forEach(f -> {
			f.setVALUE(replaceNestedVariable(f.getVALUE(), joborfolder.getLocalVariables(), false));
		});

		joborfolder.getDoMailData().forEach(f -> {
			List<Field> rawfields = ObjectUtils.getAllKnownFields(f).stream().filter(ff -> ff.getType().isInstance(new String())).collect(Collectors.toList());
			doProcessStringFields(f, rawfields, joborfolder.getLocalVariables());
		});

		doRemoveBMCVarChrs(joborfolder); // Cleanup the %% off of our variable names

		// Do for all children.
		joborfolder.getChildren().forEach(f -> doProcessNestedVarFix((BaseBMCJobOrFolder) f, null));

		if (joborfolder.getName().equals("ARCHIVE_ALLIANZ_NNOTES_FG")) {
			// System.out.print("");
		}

	}

	static void doProcessStringFields(Object ajob, List<Field> fields, List<SetVarData> lookupdata) {
		fields.forEach(f -> {
			// do we contain our variable

			if (f.getType().isInstance(new String())) {
				if (!Modifier.isFinal(f.getModifiers())) {
					// Only care about string types.
					f.setAccessible(true);

					try {
						String data = (String) f.get(ajob);

						if (data != null && data.contains("%%")) {
							// Must have a variable.
							data = varMapUtil.getReplacementValue(data);
							data = replaceNestedVariable(data, lookupdata, false);

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

	/**
	 * Just to replace the %% that starts all variables but only the local
	 * variables.
	 *
	 * @param joborfolder
	 */
	private static void doRemoveBMCVarChrs(BaseBMCJobOrFolder joborfolder) {

		joborfolder.getLocalVariables().forEach(f -> {
			f.setNAME(f.getNAME().replace("%%", ""));
		});
	}

	private static String replaceNestedVariable(String val, List<SetVarData> lookupdata, boolean replace) {

		if (val == null) {
			return val;
		}

		if (val.contains("%%")) {
			// So we contain our variable
			// val = doVariableMapping(val);

			// List<String> inputString = Arrays.asList(val.split("%%")); // O.k How many do
			// we have?
			// Something I noticed is when there are more than one variable in a value, it's
			// terminates with a period.
			// <VARIABLE NAME="%%FTP-LPATH1" VALUE="%%SUB_REMOTE_DIR.%%SUB_REMOTE_FIL" />
			// See the dot.
			// Solved this a long time ago..

			Matcher matcher = pattern.matcher(val);
			while (matcher.find()) {
				String key = matcher.group(0);

				String tempkey = key;

				if (tempkey.endsWith(".")) {
					tempkey = tempkey.substring(0, tempkey.length() - 1);
				}

				final String keylookup = tempkey;

				SetVarData vardata = lookupdata.stream().filter(p -> p.getNAME().equals(keylookup)).findFirst().orElse(null);

				// This is the variable key without the BMC %% in it for key and values.
				String varkey = keylookup.replace("%%", "");

				if (vardata == null) { // Must be a parent or global variable

					if (isGlobal(varkey)) {
						String globalkey = keylookup.replace("%%", "$$"); // In our Importer we are going to use
																			// $$VARNAME$$ for importing.
						globalkey += "$$";
						val = val.replace(key, globalkey);

						if (!globalVariables.contains(varkey)) {
							globalVariables.add(varkey);
						}
					} else {
						val = val.replace(key, "<Group." + varkey + ">");
					}

				} else {
					if (replace) {
						val = val.replace(key, vardata.getVALUE() == null ? "" : vardata.getVALUE());
					} else {
						val = val.replace(key, "<Local." + varkey + ">");
					}
				}

			}
		}

		return val;

	}

	// If the variable is in fact a global var then set in job, otherwise it's a
	// Group variable if its not in my local variables
	// local or group. Only way to know if look up local
	private static boolean isGlobal(String val) {
		for (String f : globalPrefixes) {
			if (val.startsWith(f)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * BMC has some rules we don't need for TIDAL right now, these all indicate
	 * variables that can be modified.
	 */
	static String replaceBMCVariableInstructionKey(String input) {
		if (input == null) {
			return input;
		}
		return input.replace("%%$", "%%").replace("%%@", "%%").replace("%%\\", "%%");
	}

	/**
	 * Apply business logic to our local variables and remove variables that are mapped to blank.
	 *
	 * @param joborfolder
	 * @return the list of variables that were used and updated on this jobobject
	 */
	public static List<SetVarData> doVariableMapping(BaseBMCJobOrFolder joborfolder) {

		// Do search and replace
		//
		final List<SetVarData> newvariables = new ArrayList<>();

		joborfolder.getLocalVariables().forEach(f -> {
			// Start with the name.. If the name is in the mapping and has no value then remove it.
			// Otherwise we simply replace what we find in the name side.
			// Logically the name would only ever be keys that we want to change or remove
			String varname = f.getNAME();
			String key = varMapUtil.getReplacementValue(varname);

			if (varname.contains("%%SUB_PARM_TRACKING_KEY")) {
				varname.chars();
			}
			if (!StringUtils.isBlank(key)) {
				// Map values now.
				String val = f.getVALUE();
				if (!StringUtils.isBlank(val)) {
					if (val.contains("%%SUB_PARM_TRACKING_KEY")) {
						varname.chars();
					}
					val = varMapUtil.getReplacementValue(val);
					f.setVALUE(val);
				}
				newvariables.add(f);
			} else {
				log.debug("doVariableMapping Removing Variable[" + varname + "] Mapping To Blank Value Detected");
			}

		});

		joborfolder.getLocalVariables().clear();
		joborfolder.getLocalVariables().addAll(newvariables);
		return newvariables;

	}

	/**
	 * Just replace the %%\name for our local variables both name and values
	 *
	 * @param joborfolder
	 */
	private static void doFixLocalVariableNames(BaseBMCJobOrFolder joborfolder) {
		List<SetVarData> cleaned = new ArrayList<>();

		joborfolder.getLocalVariables().forEach(f -> {
			f.setNAME(replaceBMCVariableInstructionKey(f.getNAME()));
			f.setVALUE(replaceBMCVariableInstructionKey(f.getVALUE()));
			cleaned.add(f);
		});

		joborfolder.getLocalVariables().clear();
		joborfolder.getLocalVariables().addAll(cleaned);
	}

}
