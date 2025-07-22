package com.bluehouseinc.dataconverter.parsers.bmc;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCDataModel;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.model.JobRunTimeInfo;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCCommandLineJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSimpleFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSmartFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSubFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.reporters.BMCReporters;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.CaptureData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.ControlResourceData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DEFTABLE;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoActionData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoCondData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoForceJobData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoMailData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoSetVarData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoShoutData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoSysoutData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.InConditionData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.JobData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.JobTagData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.OnData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.OutConditionData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.QuantitativeResourceData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.ShoutData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SimpleFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SmartFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SubFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.TagData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.WorkspaceData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.modified.RuleBasedCalendar;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.util.APIDateUtils;

import lombok.extern.log4j.Log4j2;

/*
 * This is basically a local dependency in smart folders , it's a condition that is set and other jobs in the sub folder
 * / smart folder
 * reference this in a In Condition.
 * <DOCOND NAME="ZIP_SENTRY_NURSENOTES_DOCS_FG-TO-FTP_PUT_SENTRY_NURSENOTES_DOCS_FG" ODATE="ODAT" SIGN="-" />
 * RULE_BASED_CALENDAR - In Smart folders and subfolders
 * MEMLIB = working directory
 */
@Log4j2
public class BMCParser extends AbstractParser<BMCDataModel> {

	private boolean groupByDataCenter = false;
	private boolean groupByApplication = true;
	private boolean processPreandPost = false;
	private boolean processDisabledJobs = true;

	public BMCParser(ConfigurationProvider cfgProvider) {
		super(new BMCDataModel(cfgProvider));
		groupByDataCenter = this.getParserDataModel().getConfigeProvider().groupByDataCenter();
		groupByApplication = this.getParserDataModel().getConfigeProvider().groupByApplication();
		processPreandPost = this.getParserDataModel().getConfigeProvider().processPreandPost();
		processDisabledJobs = this.getParserDataModel().getConfigeProvider().processDisabledJobs();

		if (groupByDataCenter) {
			groupByApplication = false; // Just in case two positives default is app so if someone set dc then false
										// this.
		}
	}

	private DEFTABLE data;

	@Override
	public void parseFile() throws Exception {
		this.parseXmlFile(this.getParserDataModel().getConfigeProvider().getBMCFilePath());
	}

	public TidalDataModel parseXmlFile(File file) throws Exception {

		JAXBContext jc = JAXBContext.newInstance(DEFTABLE.class);

		XMLInputFactory xif = XMLInputFactory.newInstance();// .newFactory();
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, true);

		// File file = new File(filePath);
		log.info("Processing File[" + file.getAbsolutePath() + "]");

		if (file.exists()) {
			XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(file));
			Unmarshaller unmarshaller = jc.createUnmarshaller();

			this.data = (DEFTABLE) unmarshaller.unmarshal(xsr);

			BMCDataModel model = this.getParserDataModel();
			this.data.getWORKSPACEOrFOLDEROrSCHEDTABLE().stream().forEach(f -> onElementExplored(model, f, null));

			return this.getParserDataModel().convertToDomainDataModel();

		} else {
			throw new TidalException("File [" + file.getAbsolutePath() + "] Not Found");
		}

	}

	public void onElementExplored(BMCDataModel model, JAXBElement<?> element, BaseBMCJobOrFolder ajob) {

		if (ajob != null) {
			String name = ajob.getName() == null ? "" : ajob.getName();

			if (name.contains("ARCHIVE_SUTTER_EOR_FG")) {
				name.getBytes();
			}

		}

		Object value = ((JAXBElement<?>) element).getValue();
		if (value instanceof SimpleFolder) {
			SimpleFolder simplefolder = (SimpleFolder) value;
			onSimpleFolder(model, simplefolder);

		} else if (value instanceof SmartFolder) {
			SmartFolder smartfolder = (SmartFolder) value;
			onSmartFolder(model, smartfolder, ajob);

		} else if (value instanceof SubFolder) {
			SubFolder subfolder = (SubFolder) value;

			onSubFolderFolder(model, subfolder, ajob);
		} else if (value instanceof JobData) {
			JobData job = (JobData) value;
			doProcessJob(model, ajob, job);

		} else if (value instanceof TagData) {
			TagData tag = (TagData) value;
			doProcessData(model, ajob, tag);

		} else if (value instanceof JobTagData) {
			JobTagData jobtag = (JobTagData) value;
			doProcessData(model, ajob, jobtag);

		} else if (value instanceof InConditionData) {
			InConditionData incon = (InConditionData) value;
			doProcessData(model, ajob, incon);

		} else if (value instanceof OutConditionData) {
			OutConditionData outcon = (OutConditionData) value;
			doProcessData(model, ajob, outcon);

		} else if (value instanceof OnData) {
			OnData ondata = (OnData) value;
			doProcessData(model, ajob, ondata);

		} else if (value instanceof ShoutData) {
			ShoutData shoutdata = (ShoutData) value;
			doProcessData(model, ajob, shoutdata);

		} else if (value instanceof SetVarData) {
			SetVarData vardata = (SetVarData) value;

			doProcessData(model, ajob, vardata);
		} else if (value instanceof DoMailData) {
			DoMailData data = (DoMailData) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof CaptureData) {
			CaptureData data = (CaptureData) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof QuantitativeResourceData) {
			QuantitativeResourceData data = (QuantitativeResourceData) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof DoForceJobData) {
			DoForceJobData data = (DoForceJobData) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof DoCondData) {
			DoCondData data = (DoCondData) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof DoActionData) {
			DoActionData data = (DoActionData) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof DoShoutData) {
			DoShoutData data = (DoShoutData) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof ControlResourceData) {
			ControlResourceData data = (ControlResourceData) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof DoSysoutData) {
			DoSysoutData data = (DoSysoutData) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof RuleBasedCalendar) {
			RuleBasedCalendar data = (RuleBasedCalendar) value;
			doProcessData(model, ajob, data);

		} else if (value instanceof DoSetVarData) {
			DoSetVarData data = (DoSetVarData) value;
			doProcessData(model, ajob, data);
		} else if (value instanceof WorkspaceData) {
			WorkspaceData data = (WorkspaceData) value;
			// doProcessData(model, ajob, data);
		} else {
			// throw new RuntimeException("Not Supported " + ((JAXBElement<?>) element).getDeclaredType().getName());
			System.out.print("Not Supported " + ((JAXBElement<?>) element).getDeclaredType().getName() + "\n");
		}

	}

	public void onSimpleFolder(BMCDataModel model, SimpleFolder folder) {

		String foldername = "";

		if (folder.getFOLDERNAME() == null) {
			foldername = folder.getTABLENAME().trim(); // README: BMC allows for spaces in xml??
		} else {
			foldername = folder.getFOLDERNAME().trim();
		}

		Objects.requireNonNull(foldername, "Folder Name value cannot be null");

		if (!processDisabledJobs) {
			if (StringUtils.isBlank(folder.getFOLDERORDERMETHOD())) {
				log.debug(String.format("onSimpleFolder Skipping Folder[%s]", foldername));
				//return;
			}
		}

		BMCSimpleFolder simplefolder = new BMCSimpleFolder();
		simplefolder.setBmcJobType(BMCJobTypes.SIMPLEFOLDER);

		// childgroup.setContainer(folder.get);

		JobData fake = new JobData();

		try {

			ObjectUtils.copyMatchingFields(folder, fake);
			JobRunTimeInfo runinfo = new JobRunTimeInfo(fake);
			simplefolder.setRunTimeInfo(runinfo);
			simplefolder.setJobData(fake);

		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		simplefolder.setName(foldername);
		simplefolder.setFolder(folder);

		if (groupByApplication) {

			String container = folder.getFOLDERNAME() == null ? folder.getTABLENAME().trim() : folder.getFOLDERNAME().trim();

			if (this.containers.containsKey(container)) {
				this.containers.get(container).addChild(simplefolder);
			} else {
				BMCSimpleFolder bmccontainer = new BMCSimpleFolder();
				bmccontainer.setBmcJobType(BMCJobTypes.SIMPLEFOLDER);
				bmccontainer.setFolder(folder);
				bmccontainer.setJobData(fake);
				bmccontainer.setRunTimeInfo(simplefolder.getRunTimeInfo());
				bmccontainer.setName(container);
				bmccontainer.addChild(simplefolder);
				this.containers.put(container, bmccontainer);
				this.getParserDataModel().addDataDuplicateLevelCheck(bmccontainer, true); // Add our object
			}

		} else if (groupByDataCenter) {
			String container = folder.getDATACENTER().trim();

			if (this.containers.containsKey(container)) {
				this.containers.get(container).addChild(simplefolder);
			} else {
				BMCSimpleFolder bmccontainer = new BMCSimpleFolder();
				bmccontainer.setBmcJobType(BMCJobTypes.SIMPLEFOLDER);
				bmccontainer.setFolder(folder);
				bmccontainer.setJobData(fake);
				bmccontainer.setRunTimeInfo(simplefolder.getRunTimeInfo());
				bmccontainer.setName(container);
				bmccontainer.addChild(simplefolder);
				this.containers.put(container, bmccontainer);
				this.getParserDataModel().addDataDuplicateLevelCheck(bmccontainer, true); // Add our object
			}
		} else {
			this.getParserDataModel().addDataDuplicateLevelCheck(simplefolder, false); // Add our object
		}

		log.debug(String.format("Processing Simple Folder[%s]", simplefolder.getFullPath()));

		folder.getJOBOrADDITIONALFOLDERDETAILS().forEach(f -> onElementExplored(model, f, simplefolder));

	}

	private Map<String, BaseBMCJobOrFolder> containers = new HashMap<>();

	public void onSmartFolder(BMCDataModel model, SmartFolder folder, BaseBMCJobOrFolder group) {

		String foldername = "";

		if (folder.getFOLDERNAME() == null) {
			foldername = folder.getTABLENAME().trim(); // README: BMC allows for spaces in xml??
		} else {
			foldername = folder.getFOLDERNAME().trim();
		}

		Objects.requireNonNull(foldername, "Folder Name value cannot be null");

		if (!processDisabledJobs) {
			String dateinpast = folder.getACTIVETILL();
			if (dateinpast != null) {
				if (APIDateUtils.isInPast(dateinpast)) {
					log.debug(String.format("onSmartFolder Skipping Disabled SmartFolder[%s][%s]", foldername, dateinpast));
					return;
				}
			}
		}
		
		BMCSmartFolder smartfolder = new BMCSmartFolder();
		smartfolder.setBmcJobType(BMCJobTypes.SMARTFOLDER);

		JobData fake = new JobData();

		try {
			// Most elements are the same on both.
			ObjectUtils.copyMatchingFields(folder, fake);
			JobRunTimeInfo runinfo = new JobRunTimeInfo(fake);
			smartfolder.setRunTimeInfo(runinfo);
			smartfolder.setJobData(fake);
			// model.addRunTimeUserToJobOrGroup(childgroup, fake.getRUNAS() == null ? null :
			// new CsvRuntimeUser(fake.getRUNAS()));

		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		smartfolder.setName(foldername);

		smartfolder.setFolder(folder);

		if (group != null) {
			group.addChild(smartfolder);
		} else {
			if (groupByApplication) {

				String container = folder.getAPPLICATION().trim();

				if (this.containers.containsKey(container)) {
					this.containers.get(container).addChild(smartfolder);
				} else {
					BMCSmartFolder bmccontainer = new BMCSmartFolder();
					bmccontainer.setBmcJobType(BMCJobTypes.SMARTFOLDER);
					bmccontainer.setFolder(folder);
					bmccontainer.setJobData(fake);
					bmccontainer.setRunTimeInfo(smartfolder.getRunTimeInfo());
					bmccontainer.setName(container);
					bmccontainer.addChild(smartfolder);
					this.containers.put(container, bmccontainer);
					this.getParserDataModel().addDataDuplicateLevelCheck(bmccontainer, true); // Add our object
				}

			} else if (groupByDataCenter) {

				String container = folder.getDATACENTER().trim();

				if (this.containers.containsKey(container)) {
					this.containers.get(container).addChild(smartfolder);
				} else {
					BMCSmartFolder bmccontainer = new BMCSmartFolder();
					bmccontainer.setBmcJobType(BMCJobTypes.SMARTFOLDER);
					bmccontainer.setFolder(folder);
					bmccontainer.setJobData(fake);
					bmccontainer.setRunTimeInfo(smartfolder.getRunTimeInfo());
					bmccontainer.setName(container);
					bmccontainer.addChild(smartfolder);
					this.containers.put(container, bmccontainer);
					this.getParserDataModel().addDataDuplicateLevelCheck(bmccontainer, true); // Add our object
				}
			} else {
				this.getParserDataModel().addDataDuplicateLevelCheck(smartfolder, false); // Add our object
			}
			// this.getParserDataModel().addDataDuplicateLevelCheck(childgroup, false);
		}

		log.debug(String.format("Processing Smart Folder[%s]", smartfolder.getFullPath()));

		// model.addRunTimeUserToJobOrGroup(childgroup, folder.getRUNAS() == null ? null
		// : new CsvRuntimeUser(folder.getRUNAS()));

		folder.getSUBFOLDEROrJOBOrADDITIONALFOLDERDETAILS().forEach(f -> onElementExplored(model, f, smartfolder));

	}

	public void onSubFolderFolder(BMCDataModel model, SubFolder folder, BaseBMCJobOrFolder group) {

		String foldername = "";

		if (folder.getJOBNAME() == null) {
			foldername = "UNDEFINDED NAME"; // README: BMC allows for spaces in xml??
		} else {
			foldername = folder.getJOBNAME().trim();
		}

//		if (!processDisabledJobs) {
//			long howmany = folder.getAUTOEDIT2OrAUTOEDITOrVARIABLE().stream().filter(f -> RuleBasedCalendar.class.isInstance(f.getValue())).count();
//			if (howmany == 0) {
//				log.debug(String.format("onSubFolderFolder Skipping Folder[%s]", foldername));
//				return;
//			}
//		}

		if (!processDisabledJobs) {
			String dateinpast = folder.getACTIVETILL();
			if (dateinpast != null) {
				if (APIDateUtils.isInPast(dateinpast)) {
					log.debug(String.format("onSubFolderFolder Skipping Disabled SubFolder[%s][%s]", foldername, dateinpast));
					return;
				}
			}
		}

		BMCSubFolder childgroup = new BMCSubFolder();
		childgroup.setBmcJobType(BMCJobTypes.SUBFOLDER);

		JobData fake = new JobData();

		try {
			// Most elements are the same on both.
			ObjectUtils.copyMatchingFields(folder, fake);
			JobRunTimeInfo runinfo = new JobRunTimeInfo(fake);
			childgroup.setRunTimeInfo(runinfo);
			childgroup.setJobData(fake);
			// model.addRunTimeUserToJobOrGroup(group, fake.getRUNAS() == null ? null : new
			// CsvRuntimeUser(fake.getRUNAS()));

		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		childgroup.setName(foldername);
		childgroup.setFolder(folder);

		if (group != null) {
			group.addChild(childgroup);
		} else {
			this.getParserDataModel().addDataDuplicateLevelCheck(childgroup, false);
		}

		log.debug(String.format("Processing Sub Folder[%s]", childgroup.getFullPath()));

		// model.addRunTimeUserToJobOrGroup(childgroup, folder.getRUNAS() == null ? null
		// : new CsvRuntimeUser(folder.getRUNAS()));

		folder.getAUTOEDIT2OrAUTOEDITOrVARIABLE().forEach(f -> onElementExplored(model, f, childgroup));

	}

	public void doProcessJob(BMCDataModel model, BaseBMCJobOrFolder group, JobData ajob) {

		String jobname = ajob.getJOBNAME(); // Default

		if (model.getConfigeProvider().useMemNameOnNullJobName()) {
			jobname = ajob.getMEMNAME();
		}

		Objects.requireNonNull(jobname, "Job Name value cannot be null");

		if (jobname.equalsIgnoreCase("EH084D")) {
			System.out.println();
		}

		if (!processDisabledJobs) {
			String dateinpast = ajob.getACTIVETILL();
			if (dateinpast != null) {
				if (APIDateUtils.isInPast(dateinpast)) {
					log.debug(String.format("doProcessJob Skipping Disabled Job[%s][%s]", jobname, dateinpast));
					return;
				}
			}
		}

		String jobtype = ajob.getAPPLTYPE();
		// FIXME: Hack for CVS all the jobs are mainframe so there is no type defined.
		jobtype = model.getConfigeProvider().forceOSJobType() != null ? model.getConfigeProvider().forceOSJobType() : ajob.getAPPLTYPE();

		Objects.requireNonNull(jobtype, "getAPPLTYPE value cannot be null");

		BMCJobTypes mytype = BMCJobTypes.valueOf(jobtype);

		BaseBMCJobOrFolder job = BMCJobTypes.getJobByType(mytype);

		JobRunTimeInfo runinfo = new JobRunTimeInfo(ajob);

		job.setRunTimeInfo(runinfo);

		if (mytype == BMCJobTypes.SAP) {
			job.setName(jobname.toUpperCase()); // SAP Always upper
		} else {
			job.setName(jobname);
		}

		job.setJobData(ajob);

		group.addChild(job);

		log.debug(String.format("Processing Job[%s] In Group[%s]", job.getFullPath(), group.getFullPath()));

		model.addJobByType(mytype, job);

		ajob.getAUTOEDIT2OrAUTOEDITOrVARIABLE().forEach(f -> onElementExplored(model, f, job));

	}

	/**
	 * Handle BMC In Conditions, this is BMC's way of saying I am dependent on this
	 * condition being present. WHich is set by a job/group setting an outcondition.
	 *
	 * @param model
	 * @param job
	 * @param data
	 */
	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, InConditionData data) {
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
		job.getInConditionData().add(data);
		// model.doRegisterData(data, job);
	}

	/**
	 * BMC Out Conditions, is the way for BMC to notify other jobs that this one is
	 * done
	 *
	 * @param model
	 * @param job
	 * @param data
	 */
	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, OutConditionData data) {
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
		job.getOutConditionData().add(data);
		// model.doRegisterData(data, job);
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, OnData data) {
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
		job.getOnData().add(data);
		data.getDOOrDOTABLEOrDOGROUP().forEach(f -> onElementExplored(model, f, job));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, ShoutData data) {
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
		job.getShoutData().add(data);
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, TagData data) {
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
		job.getTagData().add(data);
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, JobTagData data) {
		log.info(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
		job.getJobTagData().add(data);
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, SetVarData data) {
		SetVarData processed = null;

		if (StringUtils.isBlank(job.getName())) {
			System.out.println();
		}
		if (job.getName().equals("sv2wlts1")) {
			job.getName();
		}

		if (processPreandPost) {
			if (data.getNAME().equals("%%PRECMD")) {
				if (!StringUtils.isBlank(data.getVALUE())) {
					doPreCommandProcessing(job, data.getVALUE());
				}
			} else if (data.getNAME().equals("%%POSTCMD")) {
				if (!StringUtils.isBlank(data.getVALUE())) {
					doPostCommandProcessing(job, data.getVALUE());
				}
			} else {
				processed = data;
			}

		} else {
			if (data.getNAME().equals("%%PRECMD") || data.getNAME().equals("%%POSTCMD")) {
				// Do Nothing.. We arent processing them.
			} else {
				processed = data;
			}
		}

		if (processed != null) {
			job.addSetVarData(processed);

			log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), processed, job.getClass().getName()));

		}

	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, DoMailData data) {
		job.getDoMailData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, CaptureData data) {
		job.getCaptureData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, QuantitativeResourceData data) {
		job.getQuantitativeResourceData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, DoCondData data) {
		job.getDoCondData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, DoForceJobData data) {
		job.getDoForceJobData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, DoActionData data) {
		job.getDoActionData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, DoShoutData data) {
		job.getDoShoutData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, ControlResourceData data) {
		job.getControlResourceData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, DoSysoutData data) {
		job.getDoSysoutData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, RuleBasedCalendar data) {
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
		job.getRuleBasedCalendar().add(data);
	}

	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, DoSetVarData data) {
		job.getDoSetVarData().add(data);
		log.debug(String.format("Processing Job[%s] with Data[%s] Class[%s]", job.getFullPath(), data, job.getClass().getName()));
	}

	private BMCCommandLineJob doPreCommandProcessing(BaseBMCJobOrFolder job, String command) {
		BMCCommandLineJob precli = null;

		if (job.getName().equals("tastcatt_PRECMD")) {
			job.getName();
		}

		if (!StringUtils.isBlank(command)) {

			precli = new BMCCommandLineJob();

			List<InConditionData> incondata = job.getInConditionData();
			List<InConditionData> incondatacopy = incondata.stream().collect(Collectors.toList());

			precli.setInConditionData(incondatacopy); // Be Dep on t

			// precli.setJobData(job.getJobData());

			JobData predata = new JobData();

			try {
				ObjectUtils.copyMatchingFields(job.getJobData(), predata);
				precli.setJobData(predata);
			} catch (IllegalAccessException e) {
				log.error(e);
			}

			precli.setCommandLine(command);
			precli.setRunTimeInfo(job.getRunTimeInfo());

			String jobname = job.getName() + "_PRECMD";
			precli.setName(jobname);

			String conditionname = job.getFullPath().replace("\\", "PRE");

			InConditionData in = new InConditionData();
			in.setANDOR("A");
			in.setODATE("ODAT");
			in.setNAME(conditionname);

			OutConditionData out = new OutConditionData();
			out.setNAME(conditionname);
			out.setODATE("ODAT");
			out.setSIGN("+");

			precli.getOutConditionData().add(out); // Add a OUT COND to our PRE job so that our job can link up
			job.getInConditionData().add(in); // our job has linked using the incon

			job.getParent().addChild(precli);

			log.debug("Processing Pre Command[" + precli.getFullPath() + "]");

		}

		return precli;
	}

	private BMCCommandLineJob doPostCommandProcessing(BaseBMCJobOrFolder job, String command) {

		BMCCommandLineJob postcli = null;

		if (!StringUtils.isBlank(command)) {

			postcli = new BMCCommandLineJob();

			JobData postdata = new JobData();

			try {
				ObjectUtils.copyMatchingFields(job.getJobData(), postdata);
				postcli.setJobData(postdata);
			} catch (IllegalAccessException e) {
				log.error(e);
			}

			postcli.setRunTimeInfo(job.getRunTimeInfo());
			postcli.setCommandLine(command);

			String jobname = job.getName() + "_POSTCMD";
			postcli.setName(jobname);

			String conditionname = job.getFullPath().replace("\\", "POST");

			InConditionData in = new InConditionData();
			in.setANDOR("A");
			in.setODATE("ODAT");
			in.setNAME(conditionname);

			OutConditionData out = new OutConditionData();
			out.setNAME(conditionname);
			out.setODATE("ODAT");
			out.setSIGN("+");

			job.getOutConditionData().add(out); // Add a OUTCON so that our POST INCOND can linkup
			postcli.getInConditionData().add(in); // Make the post cli dependent on the job.

			job.getParent().addChild(postcli);

			log.debug("Processing Post Command[" + postcli.getFullPath() + "]");

		}

		return postcli;
	}

	@Override
	public IModelReport getModelReporter() {
		return new BMCReporters();
	}

}
