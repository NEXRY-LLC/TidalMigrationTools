package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.math.NumberUtils;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.importers.SapDataImporter;
import com.bluehouseinc.dataconverter.importers.csv.CsvSAPData;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvFileWatcherJob;
import com.bluehouseinc.dataconverter.model.impl.CsvJobExitCode;
import com.bluehouseinc.dataconverter.model.impl.CsvJobExitCode.ExitLogic;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvJobTag;
import com.bluehouseinc.dataconverter.model.impl.CsvMilestoneJob;
import com.bluehouseinc.dataconverter.model.impl.CsvOS400;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CsvResource;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.model.impl.CsvSAPJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.CcCheck;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.CcCheckFileWriter;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspAppEndData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAgentMonitorJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAs400Job;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspFileTriggerJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspJobGroup;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspOSJOb;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSAPBwpcJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSapJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspTaskProcessJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspZosJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspJobResourceStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspNoRunStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspRunStatement;
import com.bluehouseinc.io.utils.FileMapUtil;
import com.bluehouseinc.io.utils.FileMapUtil.FileParts;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.model.TrueFalse;
import com.bluehouseinc.tidal.api.model.job.filewatcher.FileActivity;
import com.bluehouseinc.tidal.api.model.job.filewatcher.TimeUnit;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;
import com.bluehouseinc.util.APIDateUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EspToTidalTransformer2 implements ITransformer<List<EspAbstractJob>, TidalDataModel> {
	SapDataImporter SAPImporter;

	TidalDataModel datamodel;
	EspDataModel espdatamodel;
	CcCheckFileWriter ccCheckwritter;

	public EspToTidalTransformer2(TidalDataModel datamodel, EspDataModel espdatamodel) {
		this.datamodel = datamodel;
		this.espdatamodel = espdatamodel;
		this.SAPImporter = new SapDataImporter(datamodel.getCfgProvider().getProvider());
		this.ccCheckwritter = new CcCheckFileWriter(espdatamodel.getConfigeProvider().getCcodeDataFile());

	}

	@Override
	public TidalDataModel transform(List<EspAbstractJob> in) throws TransformationException {
		in.forEach(f -> doProcessObjects(f, null));
		return datamodel;
	}

	private void doProcessObjects(EspAbstractJob job, CsvJobGroup parent) {

		if (job.getFullPath().startsWith("\\DB0469A")) {
			if (job.getName().contains("DB0469A.MON")) {
				job.getName().toLowerCase();
			}
		}

		if (job.getName().equals("BSRO_FILE_HCM_EMP_ADW_COMPLETE")) {
			job.getName();
		}

		if (job instanceof EspJobGroup) {

			CsvJobGroup jobgroup = (CsvJobGroup) processJobObject(job);

			if (parent == null) { // This cant be true ever with ESP, but it's keeping with the job check... TIDAL does support this
				datamodel.addJobToModel(jobgroup);
			} else {
				parent.addChild(jobgroup);
			}

			job.getChildren().forEach(obj -> doProcessObjects((EspAbstractJob) obj, jobgroup));

		} else {

			BaseCsvJobObject newJob = processJobObject(job);

			if (newJob != null) {
				if (parent == null) {
					datamodel.addJobToModel(newJob);
				} else {
					parent.addChild(newJob);
				}
			}
		}

	}

	private BaseCsvJobObject processJobObject(EspAbstractJob job) {

		if (job == null) {
			throw new TidalException("BaseJobOrGroupObject is null");
		}

		BaseCsvJobObject newjob = null;

		if (job instanceof EspAgentMonitorJob) {
			// return null; // Not needed.
		}

		if (job instanceof EspJobGroup) {
			newjob = new CsvJobGroup();
			processJob((EspJobGroup) job, (CsvJobGroup) newjob);
		} else if (job instanceof EspSapJob) {
			newjob = new CsvSAPJob();
			processJob((EspSapJob) job, (CsvSAPJob) newjob);
		} else if (job instanceof EspSapJob) {
			newjob = new CsvSAPJob();
			processJob((EspSapJob) job, (CsvSAPJob) newjob);
		} else if (job instanceof EspSAPBwpcJob) {
			newjob = new CsvSAPJob();
			processJob((EspSAPBwpcJob) job, (CsvSAPJob) newjob);
		} else if (job instanceof EspAs400Job) {
			newjob = new CsvOS400();
			processJob((EspAs400Job) job, (CsvOS400) newjob);
		} else if (job instanceof EspFileTriggerJob) {
			newjob = new CsvFileWatcherJob();
			processJob((EspFileTriggerJob) job, (CsvFileWatcherJob) newjob);
		} else if (job instanceof EspTaskProcessJob) {
			newjob = new CsvMilestoneJob();
			processJob((EspTaskProcessJob) job, (CsvMilestoneJob) newjob);
		} else {
			newjob = new CsvOSJob();

			// Covers all our OS based Jobs
			if (job instanceof EspOSJOb) {
				processJob((EspOSJOb) job, (CsvOSJob) newjob);
			} else if (job instanceof EspZosJob) {
				processJob((EspZosJob) job, (CsvOSJob) newjob);
			} else {
				// Catch all to placeholder
				processJobPlaceHolder(job, (CsvOSJob) newjob);
			}
		}

		doSetGenericData(job, newjob);

		doProcessJobResources(job, newjob);

		doSetCalendarPlaceHolder(job, newjob);

		return newjob;
	}

	private void processJobPlaceHolder(EspAbstractJob in, CsvOSJob out) {
		out.setCommandLine("PLACEHOLDER DATA");
	}

	/**
	 * Per Customer, this job type must be manually set.
	 * 
	 * @param in
	 * @param out
	 */
	private void processJob(EspTaskProcessJob in, CsvMilestoneJob out) {
		out.setRequireOperatorRelease(true);
	}

	private void processJob(EspJobGroup in, CsvJobGroup out) {

		// Set our rerun times if we have any data.
		if (in.getRuntimes().size() > 1) {

			List<String> oftimes = new ArrayList<>();
			in.getRuntimes().forEach(t -> {
				// Change from 0100 to 01:00 because TIDAL is forever changing rules :)
				String milltime = t.replaceAll("..(?!$)", "$0:");
				oftimes.add(milltime);
			});
			// in.getRuntimes().stream().map(String::valueOf).collect(Collectors.toList());
			String starttimes = String.join(",", oftimes);
			// starttimes = starttimes.replace(".", ":");
			APIDateUtils.setRerunSameStartTimes(starttimes, out, datamodel, true);
		}

	}

	private void processJob(EspZosJob in, CsvOSJob out) {

		if (in.getFullPath().contains("PC101B")) {
			in.getFullPath();
		}

		out.setCommandLine(in.getCommandLine());
		// out.setRuntimeUser(in.getUser());
		if (StringUtils.isBlank(in.getAgent())) {
			in.setAgent("NOTSET-Z");
		} else {
			in.setAgent(in.getAgent() + "-Z");
		}

		// String extract = "RC\\((\\S+)\\).*";

		if (!in.getCcchks().isEmpty()) {
			final List<CcCheck> data = in.getCcchks();
			if (data.size() == 1) {
				CcCheck check = data.get(0);

				if (check.isSingleCheck()) {
					// Single can handle
					// String range = RegexHelper.extractFirstMatch(in.getCcchk().get(0), extract);

					CsvJobExitCode exitcode = new CsvJobExitCode();

					if (check.isOkContinue()) {
						exitcode.setExitLogic(ExitLogic.EQ);
					} else {
						exitcode.setExitLogic(ExitLogic.NE);
					}

					int exitvalue = check.getSingleReturnCode();

					if (exitvalue == 0) {
						exitcode.setExitStart(check.getSingleReturnCode());
						exitcode.setExitEnd(check.getSingleReturnCode());
					} else {
						exitcode.setExitStart(0);
						exitcode.setExitEnd(0);
						this.ccCheckwritter.processCcCheckSingleCheck(check, in);
					}

					out.setExitcode(exitcode);

				}
			} else {
				// We need to write our data to file.
				// is our list of ranges to check.
				List<CcCheck> rangedata = data.stream().filter(f -> f.isRangeCheck()).collect(Collectors.toList());
				this.ccCheckwritter.processCcChecksRanges(rangedata, in);

				// is our list of process steps to process
				List<CcCheck> stepprocessdata = data.stream().filter(f -> f.isStepProcessCheck()).collect(Collectors.toList());
				this.ccCheckwritter.processCcCheckStepProcess(stepprocessdata, in);
			}

		}

	}

	private void processJob(EspOSJOb in, CsvOSJob out) {
		out.setCommandLine(in.getCommand());
		out.setParamaters(in.getParams());

		out.setSourceProfile(true);
	}

	private void processJob(EspAs400Job in, CsvOS400 out) {

		out.setName(in.getName());
		out.setPge1JobName("*JOBD");
		out.setPge1JobDescription("*USRPRF");
		out.setPge1OutputQueue("*CURRENT");
		out.setPge2RequestDataOrCommand("*CMD");
		out.setPge2RoutingData("QCMDB");
		out.setPge3AllowDisplayByWRKSBMJOB("*YES");
		out.setPge4CopyEnvironmentVariables("*NO");
		out.setPge3InitalLibraryList("*JOBD"); // Must be set in TIDAL if these two are set.
		out.setPge1CommandToRun((in).getCommand());

		// TODO: Check handling of value for Enum as key in HashMap data structure
		// Example of JOBQ statement: JOBQ CAWAAGENT.CYBESPJOBS
		String qData = in.getOptionalStatements().get(EspAs400Job.EspAs400JobOptionalStatement.JOBQ);
		if (!StringUtils.isBlank(qData)) {
			if (qData.contains(".")) {
				String[] qd = qData.split("\\.");
				out.setPge1JobQueue(qd[0]);
				out.setPge1JobQueueLibrary(qd[1]);
			}
		}

		String agent = in.getAgent();

		String mapuser = espdatamodel.getConfigeProvider().getAS400RuntimeUserByAgentName(agent);

		CsvRuntimeUser rte = new CsvRuntimeUser(mapuser);
		rte.setPasswordForFtpOrAS400("tidal");
		this.datamodel.addRunTimeUserToJobOrGroup(out, rte);

	}

	private void processJob(EspSAPBwpcJob in, CsvSAPJob out) {
		processJob((EspSapJob) in, out);
	}

	private void processJob(EspSapJob in, CsvSAPJob out) {

		if (in.getName().contains("ZRV60SBAT_FINAL_2355")) {
			in.getName();
		}

		// All SAP jobs must have a runtime user so lets just set it to the agent name as that is manditory too.
		if (in.getSapUser() == null) {
			in.setSapUser(in.getAgent());
		}

		String uppername = in.getName().toUpperCase();
		out.setName(uppername); // Must be upper, if not already

		// This is the name of our SAP job as defined in SAP.. its either set in data or we set it to the jobname
		String sapname = in.getSapJobName();

		if (StringUtils.isBlank(sapname)) {
			out.setJobName(in.getName().toUpperCase());
		} else {
			out.setJobName(sapname.toUpperCase());
		}

		// SAPImporter.addJobName(out.getJobName());

		CsvSAPData sapdata = SAPImporter.getDataByJobName(sapname);

		if (sapdata != null) {
			// We found our matching job. Not really needed but just in case we need future processing.
			in.setSapData(sapdata);
			// This should pick them all up if they are named the same.
			// ObjectUtils.copyMatchingFields(sapdata, ps);

			out.setProgramName(in.getSapData().getProgramName());
			out.setVariant(in.getSapData().getVariant());
			out.setPdest(in.getSapData().getPdest());

			out.setPrcop(in.getSapData().getPrcop());
			out.setPlist(in.getSapData().getPlist());
			out.setPrtxt(in.getSapData().getPrtxt());
			out.setPrber(in.getSapData().getPrber());
			out.setPrintDept(in.getPrintDept());
			out.setPrintExpire(in.getPrintExpire());
			out.setPrintRecipient(in.getPrintRecipient());
			out.setPrintRows(in.getPrintRows());
			out.setPrintColumns(in.getPrintColumns());
			out.setPrintSpoolName(in.getPrintSpoolName());
			out.setJobSAPClass(in.getSapJobClass());

			out.setJobMode("RUN_COPY"); // Force to this job type
			log.debug("doHandleSAP Processing data for Job[" + in.getFullPath() + "]");
		} else {
			log.debug("doHandleSAP Missing data for Job[" + in.getFullPath() + "] setting a placeholder only job");
			out.setJobMode("MISSING DATA");

		}

		out.setJobSAPClass(in.getSapJobClass());

		String progname = in.getAbapName();

		if (!StringUtils.isBlank(progname)) {
			out.setProgramName(progname);
		}

		String varname = in.getVariant();

		if (!StringUtils.isBlank(varname)) {

			if (varname.contains("&")) {
				varname = varname.replace("&", "");
			}
			out.setVariant(varname);
		}

		String runasap = in.getStartMode();

		if (!StringUtils.isBlank(runasap)) {
			out.setSubmitASAP("true");
		}

		// out.setSubmitASAP(in.getsum);

		if (in.getSapUser() != null) {
			CsvRuntimeUser rte = new CsvRuntimeUser(in.getSapUser());
			rte.setPasswordForSAP("tidal");
			this.datamodel.addRunTimeUserToJobOrGroup(out, rte);
		} else if (in.getSapStepUser() != null) {
			CsvRuntimeUser rte = new CsvRuntimeUser(in.getSapStepUser());
			rte.setPasswordForSAP("tidal");
			this.datamodel.addRunTimeUserToJobOrGroup(out, rte);
		} else {
			// How ? Bad job.
			log.error("doHandleSAP Missing RunTime User for Job[" + in.getFullPath() + "]");
			out.setJobMode("MISSING DATA");

		}

	}

	private void processJob(EspFileTriggerJob in, CsvFileWatcherJob out) {

		if (in.getName().equals("BSRO_FILE_HCM_EMP_ADW_COMPLETE")) {
			in.getName();
		}

		String fn = in.getFileName().trim();
		String data[] = fn.split(" ");

		String file = data[0];
		String action = null;
		/// data/BFP/interfaces/input/datain/zfiari07_payment_* CREATE NOCHANGE(1)
		// [/data/BFP/interfaces/input/datain/zfiari07_payment.txt, NOTEXIST]

		boolean switchback = false;

		if (file.contains("\\")) {
			switchback = true;
			file = file.replace("\\", "/");
		}

		FileParts fileparts = FileMapUtil.splitFileNameIntoParts(file.replace("\\", "/"));

		String mask = fileparts.getName() + fileparts.getSeparator() + fileparts.getExtention();
		String directory = fileparts.getPath();

		if (switchback) {
			directory = directory.replace("/", "\\");
		}

		out.setDirectory(directory);
		out.setFilemask(mask);

		int interval = 1;

		if (data.length == 3) {

			action = data[2];

			if (action.contains("NOCHANGE")) {
				String nochangein = action.replace("NOCHANGE(", "").replace(")", "");
				interval = Integer.valueOf(nochangein);
			} else {
				action.chars();
			}

			out.setFileActivity(FileActivity.STABLE);
			out.setFileActivityInterval(interval);
			out.setFileActivityTimeUnit(TimeUnit.MINUTE);

		} else {
			if (data.length == 1) {
				action = "EXIST";
			} else {
				action = data[1]; // Default is to exist but lets check
			}

			if (action.contains("NOTEXIST")) {
				out.setFileExist(TrueFalse.NO);
			} else {
				out.setFileExist(TrueFalse.YES);
			}
		}

	}

	private void doSetGenericData(EspAbstractJob in, BaseCsvJobObject out) {

		if (in.getName().contains("SUNMAINT")) {
			in.getName().toCharArray();
		}

		out.setName(in.getName());

		if (StringUtils.isBlank(in.getAgent())) {
			in.setAgent("NOTSET-CONNECTION");
		}

		this.datamodel.addNodeToJobOrGroup(out, in.getAgent());

		String rtu = in.getUser();
		if (!StringUtils.isBlank(rtu)) {
			this.datamodel.addRunTimeUserToJobOrGroup(out, new CsvRuntimeUser(rtu));
		}

		// earlysub is the same as delaysub
		// RFCHKU00_CHECK_NUMBER
		String earlysub = in.getEarlySubmission();
		String startime = in.getDelaySubmission();

		if (StringUtils.isBlank(startime)) {
			startime = earlysub;
		}

		if (!StringUtils.isBlank(startime)) {
			startime = startime.replace(".", "").replace(":", ""); // esp uses a dot ?? This should be 0700 format
			// only.

			if (NumberUtils.isParsable(startime)) {
				out.setStartTime(startime); // Delay Sub is ESP way of saying dont run until this
			} else {
				// Not parsing likely more than just a time.
				// E.G DELAYSUB 6.30 TODAY PLUS 1 WORKDAY vs. DELAYSUB 6.30
				in.setContainsAdvancedDelaySubLogic(true);
				String stmsg = out.getNotes() + "\nDelaySubmission: " + startime;
				out.setNotes(stmsg);
				log.error("doHandleGenericData Incorrect Start Time [" + startime + "] for Job: " + in.getFullPath());
			}

		}

		// Set our notes information on our job.
		out.setNotes(String.join("\n", in.getNotesData()));

		if (in.getDueout() != null) {
			out.setEndTime(Integer.toString(in.getDueout()));
		}

		out.setMaxRunTime(in.getDueoutMaxrun());

		if (!in.getTags().isEmpty()) {
			in.getTags().forEach(t -> {
				datamodel.addJobTagToJob(out, new CsvJobTag(t));
			});
		}

	}

	private void doSetCalendarPlaceHolder(EspAbstractJob in, BaseCsvJobObject out) {
		// TODO-2: Set RUN REF to look for Job definition by jobName if SAME jobGroup!
		StringBuffer stringBuffer = new StringBuffer();

		List<EspRunStatement> runStatements = in.getStatementObject().getEspRunStatements();
		List<EspNoRunStatement> noRunStatements = in.getStatementObject().getEspNoRunStatements();

		if (runStatements != null && !in.getStatementObject().getEspRunStatements().isEmpty()) {
			runStatements.forEach(runStatement -> {
				String criteria = runStatement.getCriteria();
				if (criteria != null) {

					if (stringBuffer.toString().contains(criteria)) {
						// Do nothing, its a duplicate
					} else {
						stringBuffer.append(criteria).append("-");
					}
				}
			});

			// Cant have a NORUN without a RUN statement!!!
			/*
			 * RUN DAILY NORUN 1st JAN DAILY EXCEPT 1st JAN
			 */

			if (noRunStatements != null && !in.getStatementObject().getEspNoRunStatements().isEmpty()) {
				noRunStatements.forEach(noRunStatement -> {
					String criteria = noRunStatement.getCriteria();
					if (criteria != null) {
						if (stringBuffer.toString().contains(criteria)) {
							// Do nothing, its a duplicate
						} else {
							stringBuffer.append(" EXCEPT ").append(criteria);
						}

					}
				});
			}

			String calendarString = stringBuffer.toString();

			if (calendarString.endsWith("-")) {
				calendarString = calendarString.substring(0, calendarString.length() - 1);
			}

			// Per rules of ESP if there is no run statement then this is disabled or in TIDAL world , manual run.
			if (StringUtils.isBlank(calendarString)) {
				calendarString = "Daily"; // Set a default for all jobs.
			}

			this.datamodel.addCalendarToJobOrGroup(out, new CsvCalendar(calendarString));

		} else {
			// RUN Statement is commented out so add a ADHOC Calendar
			this.datamodel.addCalendarToJobOrGroup(out, new CsvCalendar("AdHocCalender"));
		}

	}

	private void doProcessJobResources(EspAbstractJob espAbstractJob, BaseCsvJobObject tidalNewJob) {
		List<EspJobResourceStatement> jobResources = espAbstractJob.getStatementObject().getResources();
		if (jobResources != null) {
			jobResources.forEach(resource -> {
				CsvResource csvResource = new CsvResource(resource.getResourceName(), this.datamodel.getDefaultOwner());
				csvResource.setLimit(resource.getLimit());
				this.datamodel.addResourceToJob(tidalNewJob, csvResource);
			});
		}
	}

}
