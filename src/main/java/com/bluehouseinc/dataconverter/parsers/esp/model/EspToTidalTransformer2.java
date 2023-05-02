package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.csv.io.CsvExporter;
import com.bluehouseinc.dataconverter.importers.SapDataImporter;
import com.bluehouseinc.dataconverter.importers.csv.CsvSAPData;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvFileWatcherJob;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvOS400;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CsvResource;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.model.impl.CsvSAPJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAgentMonitorJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAs400Job;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspFileTriggerJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspJobGroup;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspOSJOb;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSAPBwpcJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSapJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspZosJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspJobResourceStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspNoRunStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspRunStatement;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.model.TrueFalse;
import com.bluehouseinc.tidal.api.model.job.filewatcher.FileActivity;
import com.bluehouseinc.tidal.api.model.job.filewatcher.TimeUnit;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;
import com.bluehouseinc.util.APIDateUtils;
import com.opencsv.bean.CsvToBean;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.thoughtworks.xstream.io.path.Path;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EspToTidalTransformer2 implements ITransformer<List<EspAbstractJob>, TidalDataModel> {
	SapDataImporter SAPImporter;

	TidalDataModel datamodel;

	public EspToTidalTransformer2(TidalDataModel datamodel) {
		this.datamodel = datamodel;
		this.SAPImporter = new SapDataImporter(datamodel.getCfgProvider().getProvider());
	}

	@Override
	public TidalDataModel transform(List<EspAbstractJob> in) throws TransformationException {
		in.forEach(f -> doProcessObjects(f, null));

//		List<String> programNames = SAPImporter.getJobNames();
//
//		String dosetreporting = "bfusa/SAPActualData.txt";
//
//		File file = new File(SAPImporter.getSapDataFile());
//
//		CsvToBean<CsvSAPData> saplargedata = SAPImporter.fromFile(file, CsvSAPData.class);
//
//		List<CsvSAPData> actualSAP = new ArrayList<>();
//
//		saplargedata.forEach(f -> {
//
//			if (programNames.contains(f.getJobName())) {
//
//				if (!actualSAP.contains(f)) {
//					actualSAP.add(f);
//					log.info("Found SAP Program Name[{}]", f);
//				}
//
//			}
//		});
//
//		try {
//
//			CsvExporter.WriteToFile(dosetreporting, actualSAP);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		return datamodel;
	}

	private void doProcessObjects(EspAbstractJob job, CsvJobGroup parent) {

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
					datamodel.addJobToModel((BaseCsvJobObject) newJob);
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

	private void processJob(EspJobGroup in, CsvJobGroup out) {

		// Set our rerun times if we have any data.
		if (in.getRuntimes().size() > 1) {
			List<String> oftimes = in.getRuntimes().stream().map(String::valueOf).collect(Collectors.toList());
			String starttimes = String.join(",", oftimes);
			APIDateUtils.setRerunSameStartTimes(starttimes, out, datamodel, true);
		}
	}

	private void processJob(EspZosJob in, CsvOSJob out) {
		out.setCommandLine(in.getName());

		// TODO: We need to figure out the default connections for all the ZOS Jobs
		in.setAgent("AgentZOS");

	}

	private void processJob(EspOSJOb in, CsvOSJob out) {
		out.setCommandLine(in.getCommand());
		out.setParamaters(in.getParams());
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

	}

	private void processJob(EspSAPBwpcJob in, CsvSAPJob out) {
		processJob((EspSapJob) in, out);
	}

	private void processJob(EspSapJob in, CsvSAPJob out) {

		out.setName(in.getName().toUpperCase()); // Must be upper

		out.setProgramName(in.getAbapName());

		String sapname = in.getOptionalStatements().get(EspSapJob.EspSapJobOptionalStatement.SAPJOBNAME);

		if (StringUtils.isBlank(sapname)) {
			out.setJobName(in.getName().toUpperCase());
		} else {
			out.setJobName(sapname.toUpperCase());
		}

		//SAPImporter.addJobName(out.getJobName());

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
			out.setJobMode("RUN_COPY"); // Force to this job type
			log.debug("doHandleSAP Processing data for Job[" + in.getFullPath() + "]");
		} else {
			log.debug("doHandleSAP Missing data for Job[" + in.getFullPath() + "] setting a placeholder only job");
			out.setJobMode("MISSING DATA");

		}

		String jobclass = in.getOptionalStatements().get(EspSapJob.EspSapJobOptionalStatement.SAPJOBCLASS);
		out.setJobSAPClass(jobclass);

		// out.setJobMode("LINK ME");

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

		String fn = in.getFileName().trim();
		String data[] = fn.split(" ");

		String file = data[0];
		String action = null;
		/// data/BFP/interfaces/input/datain/zfiari07_payment_* CREATE NOCHANGE(1)
		// [/data/BFP/interfaces/input/datain/zfiari07_payment.txt, NOTEXIST]
		java.nio.file.Path p = Paths.get(file.replace("\\", "/"));

		String mask = p.getFileName().toString();

		if (p.getParent() != null) {
			String path = p.getParent().toString();
			out.setDirectory(path);
		} else {
			p.toAbsolutePath();
		}

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

		out.setName(in.getName());

		if (StringUtils.isBlank(in.getAgent())) {
			//FIXME: This should only be set for ZoS Job type. 
			in.setAgent("AgentZOS");
		}

		this.datamodel.addNodeToJobOrGroup(out, in.getAgent());

		String rtu = in.getUser();
		if (!StringUtils.isBlank(rtu)) {
			this.datamodel.addRunTimeUserToJobOrGroup(out, new CsvRuntimeUser(rtu));
		}

		String delaysub = in.getDelaySubmission();

		if (!StringUtils.isBlank(delaysub)) {
			delaysub = delaysub.replace(".", "").replace(":", ""); // esp uses a dot ?? This should be 0700 format
			// only.

			if (NumberUtils.isParsable(delaysub)) {
				out.setStartTime(delaysub); // Delay Sub is ESP way of saying dont run until this
			} else {
				// Not parsing likely more than just a time. 
				// E.G   DELAYSUB 6.30 TODAY PLUS 1 WORKDAY vs.   DELAYSUB 6.30
				in.setContainsAdvancedDelaySubLogic(true);
				String stmsg = out.getNotes() + "\nDelaySubmission: " + delaysub;
				out.setNotes(stmsg);
				log.error("doHandleGenericData Incorrect Start Time [" + delaysub + "] for Job: " + in.getFullPath());
			}

		}

		// Set our notes information on our job.
		out.setNotes(String.join("\n", in.getNoteData()));

		if (in.getDueout() != null) {
			String endtime = in.getDueout().replace("EXEC ", "").replace(".", "").replace(":", "");

			if (NumberUtils.isParsable(endtime)) {
				out.setEndTime(endtime); // Delay Sub is ESP way of saying dont run until this
			} else {
				// Not parsing likely more than just a time. 
				// E.G     DUEOUT EXEC NOW PLUS 4 HOURS vs  DUEOUT EXEC 10AM
				in.setContainsAdvancedDueOutLogic(true);
				String stmsg = out.getNotes() + "\nEndTime: " + endtime;
				out.setNotes(stmsg);
				log.error("doHandleGenericData Incorrect End Time [" + endtime + "] for Job: " + in.getFullPath());
			}

			//
			// String delsubdata = APIDateUtils.convertToTidalMiltary(deldata, ':');
			// String endtime = RegexHelper.extractFirstMatch(delsubdata, "(\\d{2}:\\d{2})");
			// out.setEndTime(endtime);
		}

	}

	private void doSetCalendarPlaceHolder(EspAbstractJob in, BaseCsvJobObject out) {
		// TODO-2: Set RUN REF to look for Job definition by jobName if SAME jobGroup!
		StringBuffer stringBuffer = new StringBuffer();

		List<EspRunStatement> runStatements = in.getEspRunStatements();
		List<EspNoRunStatement> noRunStatements = in.getEspNoRunStatements();

		if (runStatements != null && !in.getEspRunStatements().isEmpty()) {
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
		}
		/*
		 * RUN DAILY NORUN 1st JAN DAILY EXCEPT 1st JAN
		 */

		if (noRunStatements != null && !in.getEspNoRunStatements().isEmpty()) {
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

		if (StringUtils.isBlank(calendarString)) {
			calendarString = "Daily"; // Set a default for all jobs.
		}

		this.datamodel.addCalendarToJobOrGroup(out, new CsvCalendar(calendarString));
	}

	private void doProcessJobResources(EspAbstractJob espAbstractJob, BaseCsvJobObject tidalNewJob) {
		List<EspJobResourceStatement> jobResources = espAbstractJob.getResources();
		if (jobResources != null) {
			jobResources.forEach(resource -> {
				CsvResource csvResource = new CsvResource(resource.getResourceName(), this.datamodel.getDefaultOwner());
				csvResource.setLimit(resource.getLimit());
				this.datamodel.addResourceToJob(tidalNewJob, csvResource);
			});
		}
	}

}
