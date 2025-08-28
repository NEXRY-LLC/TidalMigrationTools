package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.api.importer.APIJobUtils;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvMilestoneJob;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl.CA7BaseJobObject;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl.CA7BaseJobObject.JobType;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;
import com.bluehouseinc.util.APIDateUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class TivoliToTidalTransformer implements ITransformer<List<CA7BaseJobObject>, TidalDataModel> {

	private TidalDataModel tidalDataModel;
	TivoliMainframeOPCDataModel tivolidatamodel;

	public TivoliToTidalTransformer(TidalDataModel tidalDataModel, TivoliMainframeOPCDataModel autosysdatamodel) {
		this.tidalDataModel = tidalDataModel;
		this.tivolidatamodel = autosysdatamodel;
	}

	@Override
	public TidalDataModel transform(List<CA7BaseJobObject> baseJobOrGroupObjects) throws TransformationException {
		baseJobOrGroupObjects.forEach(f -> doProcessApplicationObjects(f, null));
		return tidalDataModel;
	}

	private List<String> namecheck = new ArrayList<String>();

	public void doProcessApplicationObjects(CA7BaseJobObject application, CsvJobGroup parent) {

		log.debug("doProcessObjects() AutosysAbstractJob Name[{}]", application.getFullPath());

		String fullpathname = application.getFullPath();

		if (!namecheck.contains(fullpathname)) {
			namecheck.add(fullpathname);
		} else {
			// We are a duplicate
			application.setName(application.getName() + "_dup");
		}

		CsvJobGroup group = new CsvJobGroup();
		group.setName(application.getName());
		group.setOwner(getTidalDataModel().getDefaultOwner());

		// getTidalDataModel().addNodeToJobOrGroup(group, getTidalDataModel().get);

		StringBuilder cannamebuilder = new StringBuilder();
		StringBuilder desbuilder = new StringBuilder();
		String calname = application.getCalendar() == null ? "" : application.getCalendar();

		cannamebuilder.append(calname);

		CsvCalendar cal = new CsvCalendar();

		if (application.getSchedules().isEmpty()) {
			group.setCalendar(null);

		} else {

			application.getSchedules().forEach(s -> {
				String rule = s.getRuleDescription();
				rule = rule.replace("ONLY", "").replace("PERIOD", "PE").replace("MONDAY", "MO").replace("TUESDAY", "TU").replace("WEDNESDAY", "WE").replace("THURSDAY", "TH").replace("FRIDAY", "FR").replace("SATURDAY", "SA").replace("SUNDAY", "SU")
						.replace("WORKDAY", "WD").replace("LAST", "LST").replace("WEEK", "W").replace("JANUARY", "JA").replace("FEBRUARY", "FE").replace("MARCH", "MA").replace("APRIL", "AP").replace("MAY", "MAY").replace("JUNE", "JU")
						.replace("JULY", "JUL").replace("AUGUST", "AU").replace("SEPTEMBER", "SEP").replace("NOVEMBER", "NV").replace("DECEMBER", "DE").replace("ADHOC", "AH").replace("YEAR", "YR").replace("(", "").replace(")", "").replace(" ", "")
						.replace("DAY", "DY").replace("EVERY", "EV");

				cannamebuilder.append(rule);
				cannamebuilder.append("-R" + s.getRule());
				desbuilder.append(s.getRuleDescription());
				desbuilder.append(" RULE(" + s.getRule() + ")");
				desbuilder.append("\n");
			});

			cal.setCalendarName(cannamebuilder.toString());
			cal.setCalendarNotes(desbuilder.toString());

			this.getTidalDataModel().addCalendarToJobOrGroup(group, cal);
		}

		this.getTidalDataModel().addJobToModel(group);

		application.getChildren().forEach(childjob -> processBaseJobOrGroupObject((CA7BaseJobObject) childjob, group)); // Parse children

	}

	public static String truncateLines(String input, int charCount) {
		if (input == null) {
			return null;
		}

		if (charCount <= 0) {
			throw new IllegalArgumentException("Character count must be greater than 0");
		}

		return Arrays.stream(input.split("\\r?\\n")).map(line -> line.trim()) // Remove leading/trailing whitespace
				.filter(line -> !line.isEmpty()) // Skip empty lines
				.map(line -> line.length() > charCount ? line.substring(0, charCount) : line).collect(Collectors.joining(" "));
	}

	// TODO: Check here to see if we can take our file trigger job type and use it to apply to the children jobs that depende on me.

	private BaseCsvJobObject processBaseJobOrGroupObject(CA7BaseJobObject base, CsvJobGroup parent) {

		if (base == null) {
			throw new TidalException("BaseJobOrGroupObject is null");
		}

		log.debug("processBaseJobOrGroupObject() Processing Job/Group Name[{}]", base.getFullPath());

		BaseCsvJobObject baseCsvJobObject = null;

		if (base.getJobType() == JobType.CA7 || base.getJobType() == JobType.SCRIPT) {

			baseCsvJobObject = new CsvOSJob();

			String cmd = base.getCommandLineData();

			if (base.getJobType() == JobType.SCRIPT) {
				APIJobUtils.setJobCommandDetail((CsvOSJob) baseCsvJobObject, cmd, getTidalDataModel().getCfgProvider().formatJobParams());
			} else {
				((CsvOSJob) baseCsvJobObject).setCommandLine(cmd); // Dont change it. this is ZOS
			}

		} else if (base.getJobType() == JobType.END) {
			baseCsvJobObject = new CsvMilestoneJob(); //

		} else {
			throw new TidalException("Error, unknown job type for Name=[" + base.getFullPath() + "]");
		}

		doSetCommonJobInformation(base, baseCsvJobObject);

		if (parent != null) {
			parent.addChild(baseCsvJobObject);
		} else {
			this.getTidalDataModel().addJobToModel(baseCsvJobObject);
		}

		if (baseCsvJobObject.getAgentName() != null) {
			if (parent.getAgentName() == null || parent.getAgentName().equalsIgnoreCase(baseCsvJobObject.getAgentName())) {
				parent.setAgentName(baseCsvJobObject.getAgentName());
				parent.setInheritAgent(false);
				baseCsvJobObject.setInheritAgent(true);
				baseCsvJobObject.setAgentName(null);
			}
		}

		return baseCsvJobObject;
	}

	private void doSetCommonJobInformation(CA7BaseJobObject job, BaseCsvJobObject baseCsvJobObject) {

		baseCsvJobObject.setId(job.getId());
		baseCsvJobObject.setName(job.getName());
		baseCsvJobObject.setNotes(job.getDescription());

		getTidalDataModel().addOwnerToJobOrGroup(baseCsvJobObject, getTidalDataModel().getDefaultOwner());

		String rte = job.getRuntimeUserName();
		if (rte != null && !rte.isEmpty()) {
			if (rte.contains("/")) {
				String[] vals = rte.split("/");
				getTidalDataModel().addRunTimeUserToJobOrGroup(baseCsvJobObject, new CsvRuntimeUser(vals[0], vals[1]));
			} else {
				getTidalDataModel().addRunTimeUserToJobOrGroup(baseCsvJobObject, new CsvRuntimeUser(rte));
			}
		}

		if (job.getWorkstationId() == null) {
			log.warn("Agent missing from job[]" + job.getName());
		} else {
			getTidalDataModel().addNodeToJobOrGroup(baseCsvJobObject, job.getWorkstationId().trim());
		}

		if (job.getStartTime() != null) {
			String st = job.getStartTime();
			APIDateUtils.setRerunSameStartTimes(st, baseCsvJobObject, getTidalDataModel(), true);
		}

		baseCsvJobObject.setInheritCalendar(true);

	}

}
