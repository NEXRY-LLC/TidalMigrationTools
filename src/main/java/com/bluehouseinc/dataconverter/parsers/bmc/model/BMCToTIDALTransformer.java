package com.bluehouseinc.dataconverter.parsers.bmc.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bluehouseinc.dataconverter.api.importer.APIJobUtils;
import com.bluehouseinc.dataconverter.importers.SapDataImporter;
import com.bluehouseinc.dataconverter.importers.csv.CsvSAPData;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvFtpJob;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CsvPeopleSoftJob;
import com.bluehouseinc.dataconverter.model.impl.CsvResource;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.model.impl.CsvSAPJob;
import com.bluehouseinc.dataconverter.model.impl.CsvTimeZone;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCCommandLineJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCFileTransferJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOS400Job;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCPeopleSoftJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSAPJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSimpleFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSmartFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSubFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.model.xml.AccountParser;
import com.bluehouseinc.dataconverter.parsers.bmc.transformers.BMCOS400JobTransformer;
import com.bluehouseinc.dataconverter.parsers.bmc.util.DateUtil;
import com.bluehouseinc.dataconverter.parsers.bmc.util.FTPUtil;
import com.bluehouseinc.dataconverter.util.VariableMapUtil;
import com.bluehouseinc.tidal.api.model.job.RepeatType;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;
import com.bluehouseinc.util.APIDateUtils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public class BMCToTIDALTransformer implements ITransformer<List<BaseBMCJobOrFolder>, TidalDataModel> {

	SapDataImporter SAPImporter ;
	AccountParser parser;

	// boolean USENEWDEPCODE = false;

	TidalDataModel tidalDataModel;
	BMCOS400JobTransformer trans400;
	BMCDataModel bmcDataModel;
	// Map<String, List<BaseBMCJobOrFolder>> OutStringToFullPathList = new HashedMap<>();

	private Set<String> duplicateJobNameValidation = new HashSet<>();
	DependencyGraphMapper depgraphmap;

	// Integer containerLen;

	public BMCToTIDALTransformer(TidalDataModel datamodel,BMCDataModel bmcdatamodel, DependencyGraphMapper depgraph) {
		this.bmcDataModel = bmcdatamodel;
		this.tidalDataModel = datamodel;
		trans400 = new BMCOS400JobTransformer(datamodel);
		this.depgraphmap = depgraph;
		this.SAPImporter = new SapDataImporter(bmcdatamodel.getConfigeProvider().getProvider());
		// USENEWDEPCODE = Boolean.valueOf(this.cfgProvider.getConfiguration().getOrDefault("tidal.dep.newcode", "false"));
	}

	@Override
	public TidalDataModel transform(List<BaseBMCJobOrFolder> in) throws TransformationException {

		String accountfolder = this.getBmcDataModel().getConfigeProvider().getAccountFolder();

		if (accountfolder == null) {
			//throw new TidalException("Missing property " + BMC_AccountFileFolder);
		}else {
			this.parser = new AccountParser(accountfolder);
		}


		in.forEach(f -> doProcessDupJobNameFix(f)); // Must fix before dep processing as we lookup
													// by fullpath()

		String glbnames = this.getBmcDataModel().getConfigeProvider().getBMCGlobalPrefixes();

		VariableFixUtil.setVariableMapUtil(new VariableMapUtil(getBmcDataModel().getConfigeProvider().getProvider()));
		// Fix Nested Variables and other variable related items
		in.forEach(f -> VariableFixUtil.doProcessNestedVarFix(f, glbnames));

		VariableFixUtil.getGlobalVariables().forEach(f -> {
			this.getTidalDataModel().addVariable(new CsvVariable(f));
		});

		// in.forEach(f -> VariableFixUtil.doSetVariableNames(f)); //

		in.forEach(f -> doProcessJobs(f, null));

		return getTidalDataModel();
	}

	public void doProcessJobs(BaseBMCJobOrFolder base, CsvJobGroup parent) {

		if (base.getName().equalsIgnoreCase("YFBCSPS1_DEM")) {
			base.getName();
		}

		if (getIsFolderType(base)) {

			if (!base.getChildren().isEmpty()) {

				CsvJobGroup group = new CsvJobGroup();

				group.setName(base.getName());
				setBaseNameInfo(base, group);
				setRunTimeInfo(base, group);
				setBasicInfo(base, group);
				setStartAndEndTime(base, group);
				setRunTimeUserInfo(base, group);
				setVariables(base, group);
				doProcessResources(base, group);

				if (base instanceof BMCSimpleFolder) {
					getTidalDataModel().addCalendarToJobOrGroup(group, new CsvCalendar("Daily"));
				}

				if (parent != null) {
					parent.addChild(group);
				} else {
					getTidalDataModel().addJobToModel(group);
				}

				// if (USENEWDEPCODE) {
				// // this.depthread.doRegisterJobDepGraph(group, base);
				this.depgraphmap.doProcessJobDepGraph(group, base);
				// // DependencyGraph.registerJobDepGraph(base, group);
				// } else {
				// doProcessJobDepGraph(group, base);
				// }

				log.debug("Processing Group Name[" + group.getFullPath() + "]");
				base.getChildren().forEach(f -> doProcessJobs((BaseBMCJobOrFolder) f, group)); // Parse and parse
			}

		} else

		{
			BaseCsvJobObject newjob = null;

			if (base instanceof BMCCommandLineJob) {
				newjob = doHandleCommandLine((BMCCommandLineJob) base);
			} else if (base instanceof BMCFileTransferJob) {
				newjob = doHandleMultiFTPCommand((BMCFileTransferJob) base);
			} else if (base instanceof BMCOS400Job) {
				newjob = doHandleOS400MultiCommands((BMCOS400Job) base);
			} else if (base instanceof BMCPeopleSoftJob) {
				newjob = doHandlePeopleSoft((BMCPeopleSoftJob) base);
			} else if (base instanceof BMCSAPJob) {
				newjob = doHandleSAP((BMCSAPJob) base);
			} else {
				newjob = doHandleCatchAllJobs(base);
			}

			setBaseNameInfo(base, newjob);
			setBasicInfo(base, newjob);
			setStartAndEndTime(base, newjob);
			setRunTimeInfo(base, newjob);
			setRunTimeUserInfo(base, newjob);
			setVariables(base, newjob);
			doProcessResources(base, newjob);

			if (parent != null) {
				parent.addChild(newjob);
			} else {
				getTidalDataModel().addJobToModel(newjob);
			}

			// if (USENEWDEPCODE) {
			// // DependencyGraph.registerJobDepGraph(base, newjob);
			// // this.depthread.doRegisterJobDepGraph(newjob, base);
			this.depgraphmap.doProcessJobDepGraph(newjob, base);
			// } else {
			// doProcessJobDepGraph(newjob, base);
			// }

			log.debug("Processing Job Name[" + newjob.getFullPath() + "]");

		}
	}

	void doProcessDupJobNameFix(BaseBMCJobOrFolder job) {
		String key = job.getFullPath();

		if (this.duplicateJobNameValidation.contains(key)) {
			String newname = job.getName() + "-DUP" + job.getId();
			log.warn("Duplicate Job Name[" + job.getName() + "] found in Group[" + key + "] Renaming To [" + newname + "]");
			job.setName(newname);
		} else {
			this.duplicateJobNameValidation.add(key);
		}

		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(f -> doProcessDupJobNameFix((BaseBMCJobOrFolder) f));
		}
	}

	// public void doProcessJobDeps(BaseBMCJobOrFolder base) {
	//
	// if (base.getName().contains("DEVDLY")) {
	// log.debug("");
	// }
	//
	// BaseCsvJobObject me = this.getTidalDataModel().findFirstJobByFullPath(base.getFullPath());
	//
	// if (me == null) {
	// // Ignore group with no children or parents.. We know we did not process them.
	// if (base.getChildren().isEmpty() && getIsFolderType(base)) {
	// // Ignore.
	// } else {
	// log.warn("ERROR Unable to locate Job[" + base.getFullPath() + "]");
	// }
	// return;
	// }
	//
	// base.getInConditionData().forEach(incon -> {
	//
	// String key = incon.getNAME();
	//
	// List<BaseBMCJobOrFolder> depsonjobs = OutStringToFullPathList.get(key);
	//
	// if (depsonjobs == null) {
	// log.warn("ERROR Unable to locate any outconditions that match our incondition[" + key + "] for job[" +
	// base.getFullPath()
	// + "]");
	// } else {
	// depsonjobs.forEach(dep -> {
	//
	// BaseCsvJobObject depsonme = this.getTidalDataModel().findFirstJobByFullPath(dep.getFullPath());
	//
	// if (depsonme == null) {
	// log.warn("ERROR Unable to locate Depent Job[" + dep.getFullPath() + "] For Job[" + me.getFullPath() + "]");
	// } else {
	// // Check to make sure we are not dependent on the same job.
	//
	// if (me.getId() == depsonme.getId()) {
	// // log.debug("ERROR DEP LOOP Job[" + dep.getFullPath() + "] For Job[" +depsonme.getFullPath() + "]");
	//
	// } else {
	// this.getTidalDataModel().addJobDependencyForJobCompletedNormal(me, depsonme);
	// }
	// }
	//
	// });
	// }
	// });
	//
	// if (!base.getChildren().isEmpty()) {
	// base.getChildren().forEach(f -> doProcessJobDeps((BaseBMCJobOrFolder) f));
	// }
	// }

	private void setBaseNameInfo(BaseBMCJobOrFolder bmc, BaseCsvJobObject tidal) {
		tidal.setName(bmc.getName());
	}

	private void setBasicInfo(BaseBMCJobOrFolder bmc, BaseCsvJobObject tidal) {
		getTidalDataModel().addOwnerToJobOrGroup(tidal, getTidalDataModel().getDefaultOwner());
		tidal.setNotes(bmc.getPlaceHolderData());
	}

	private void setRunTimeUserInfo(BaseBMCJobOrFolder bmc, BaseCsvJobObject tidal) {

		String runas = bmc.getJobData().getRUNAS();

		if (!StringUtils.isBlank(runas)) {
			CsvRuntimeUser rte = new CsvRuntimeUser(runas);

			if (bmc instanceof BMCPeopleSoftJob) {
				rte.setPasswordForPeopleSoft("tidal");
			} else if (bmc instanceof BMCOS400Job) {
				rte.setPasswordForFtpOrAS400("tidal");
			} else if (bmc instanceof BMCFileTransferJob) {
				rte.setPasswordForFtpOrAS400("tidal");
			} else if (bmc instanceof BMCSAPJob) {
				rte.setPasswordForSAP("tidal");
			} else {
				rte.setPasswordForFtpOrAS400("tidal");
			}

			getTidalDataModel().addRunTimeUserToJobOrGroup(tidal, rte);
		} else {
			// log.debug("TYPE[" + bmc.getBMCJobType().name() + "], JOB[" +
			// bmc.getFullPath() + "] Missing RunTime User");
		}
	}

	private void setAgentInfo(BaseBMCJobOrFolder bmc, BaseCsvJobObject tidal) {
		/*
		 * So in BMC like jobs you can have duplicate names!!
		 */
		String node = bmc.getNodeName();
		if (node != null) {
			if (getBmcDataModel().getConfigeProvider().appendJobTypeToAgentName()) {
				node = node.toUpperCase() + "_" + bmc.getBMCJobType().toString();
			}
			getTidalDataModel().addNodeToJobOrGroup(tidal, node.toUpperCase());
		}

	}

	private void setRunTimeInfo(BaseBMCJobOrFolder bmc, BaseCsvJobObject tidal) {
		setAgentInfo(bmc, tidal);
		// FIXME : Locate rerun logic and confirm this job and other. scnazd00
		String name = bmc.getName();

		if (name.equalsIgnoreCase("ALLIANZ_NNOTES_FG") || name.equalsIgnoreCase("yfcfvsb2") || name.equalsIgnoreCase("scepm009_01_jan")) {
			name.getBytes();
		}

		String calname = DateUtil.getMonthDaysFromString(bmc);

		if (calname != null) {
			calname = calname.replace(",", "-");

			if (calname.equals("*")) {
				tidal.setInheritCalendar(true);
			} else {
				getTidalDataModel().addCalendarToJobOrGroup(tidal, new CsvCalendar(calname));
			}
		}

		if (bmc.getRunTimeInfo().isRerunningJob()) {

			boolean convertrerun = getBmcDataModel().getConfigeProvider().convertTimeSeqToTidalRerun();

			switch (bmc.getRunTimeInfo().getRerunType()) {

			case SpecificTimes:
				// Need to add the : to the time.. WHY Does TIDAL not follow basic time ,
				String millhackfix = APIDateUtils.convertToTidalMiltary(bmc.getRunTimeInfo().getStartTimeSeqence(), ',');

				APIDateUtils.setRerunSameStartTimes(millhackfix, tidal, getTidalDataModel(), convertrerun);

				break;
			case Interval:
				Integer intv = bmc.getRunTimeInfo().getRerunInterval();

				if (intv != null) {
					if (intv == 0) {
						// We have an odd issue with types here.. this is pure hackathon
						String sq = bmc.getRunTimeInfo().getStartTimeSeqence();
						if (sq != null) {
							millhackfix = APIDateUtils.convertToTidalMiltary(bmc.getRunTimeInfo().getStartTimeSeqence(), ',');
							APIDateUtils.setRerunSameStartTimes(millhackfix, tidal, getTidalDataModel(), false);
							break;
						}
					} else {
						tidal.getRerunLogic().setRepeatEvery(intv);
						int repeatcount = bmc.getJobData().getMAXRERUN() == 0 ? 0 : bmc.getJobData().getMAXRERUN();
						if (repeatcount > 0) {
							tidal.getRerunLogic().setRepeatMaxTimes(repeatcount);
						}
						break;
					}
				}

				break;
			case IntervalSequence:

				intv = bmc.getRunTimeInfo().getRerunIntervalSequence();

				if (intv != null) {
					tidal.getRerunLogic().setRepeatType(RepeatType.SAME);
					tidal.getRerunLogic().setRepeatEvery(intv);

					int repeatcount = bmc.getJobData().getMAXRERUN() == 0 ? 1 : bmc.getJobData().getMAXRERUN();
					tidal.getRerunLogic().setRepeatMaxTimes(repeatcount);

				}
				break;
			case NULL:
				break;
			default:

				break;

			}

		}

		if (bmc.getRunTimeInfo().isOperatorRelease()) {
			tidal.setOperatorRelease(true);
		}
	}

	private void setStartAndEndTime(BaseBMCJobOrFolder bmc, BaseCsvJobObject tidal) {

		if (!StringUtils.isBlank(bmc.getRunTimeInfo().getStartTime())) {
			tidal.setStartTime(bmc.getRunTimeInfo().getStartTime());
		}

		if (!StringUtils.isBlank(bmc.getRunTimeInfo().getEndTime())) {
			tidal.setEndTime(bmc.getRunTimeInfo().getEndTime());
		}

		// Setup TimeZones that exists.
		if (!StringUtils.isBlank(bmc.getJobData().getTIMEZONE())) {
			String tz = bmc.getJobData().getTIMEZONE();
			CsvTimeZone tidaltz = new CsvTimeZone();
			tidaltz.setName(tz);
			getTidalDataModel().addTimeZoneToJob(tidal, tidaltz);
		}

	}

	private BaseCsvJobObject doHandleMultiFTPCommand(BMCFileTransferJob ftp) {

		if (ftp.getName().equals("FTP_PUT_ALLIANZ_NNOTES_FG")) {
			ftp.getName();
		}

		FTPUtil ftputil = new FTPUtil(parser);

		List<CsvFtpJob> jobs = ftputil.setupFTPJob(ftp, this.getTidalDataModel());

		if (jobs.size() == 1) {
			return jobs.get(0);
		} else {
			CsvJobGroup group = new CsvJobGroup();
			ftp.setName(ftp.getName() + "-FTPMULTIPSTEP");

			setBaseNameInfo(ftp, group);
			setBasicInfo(ftp, group);
			setRunTimeInfo(ftp, group);
			setStartAndEndTime(ftp, group);
			setRunTimeUserInfo(ftp, group);
			doProcessResources(ftp, group);
			setAgentInfo(ftp, group);

			jobs.forEach(f -> {

				setBasicInfo(ftp, f);
				setRunTimeInfo(ftp, f);
				setStartAndEndTime(ftp, f);
				setRunTimeUserInfo(ftp, f);
				doProcessResources(ftp, group);
				group.addChild(f);
			});

			return group;
		}

	}

	/**
	 *
	 * @param osj
	 */
	private BaseCsvJobObject doHandleOS400MultiCommands(BMCOS400Job osj) {
		BaseCsvJobObject base = null;

		try {

			base = trans400.transform(osj);

			doSetOS400Data(osj, base);

		} catch (TransformationException e) {
			throw new RuntimeException(e);
		}

		return base;
	}

	private void doSetOS400Data(BMCOS400Job osj, BaseCsvJobObject base) {

		setBasicInfo(osj, base);
		setRunTimeInfo(osj, base);
		setStartAndEndTime(osj, base);
		setRunTimeUserInfo(osj, base);
		doProcessResources(osj, base);

		if (!base.getChildren().isEmpty()) {
			base.getChildren().forEach(f -> doSetOS400Data(osj, (BaseCsvJobObject) f));
		}

	}

	private BaseCsvJobObject doHandlePeopleSoft(BMCPeopleSoftJob osj) {
		CsvPeopleSoftJob ps = new CsvPeopleSoftJob();

		ps.setExtendedInfo(osj.getExtenedInfo());

		return ps;
	}



	private BaseCsvJobObject doHandleSAP(BMCSAPJob bmc) {
		CsvSAPJob ps = new CsvSAPJob();

		if (bmc.getName().equalsIgnoreCase("YFBCSPS1_0100")) {
			bmc.getName();
		}
		// bmc.setName(bmc.getName().toUpperCase()); // Must be uppdate
		ps.setName(bmc.getName().toUpperCase()); // Must be upper

		if (StringUtils.isBlank(bmc.getJobName())) {
			ps.setJobName(bmc.getName().toUpperCase());
		} else {
			ps.setJobName(bmc.getJobName().toUpperCase());
		}
		ps.setGroupOrdID(bmc.getGroupOrdID());
		ps.setDetectChildTable(bmc.getDetectChildRelease());
		ps.setJobMode(bmc.getJobMode().name());
		ps.setAccount(bmc.getAccount());
		ps.setServerOrGroupType(bmc.getServerOrGroupType());
		ps.setJobSAPClass(bmc.getJobSAPClass());
		ps.setJobCountType(bmc.getJobCountType());
		ps.setJobCount(bmc.getJobCount());
		ps.setStartStep(bmc.getStartStep());
		ps.setKeepJoblogOption(bmc.getKeepJoblogOption());
		ps.setOverrideJoblogDefault(bmc.getOverrideJoblogDefault());
		ps.setJobLog(bmc.getJobLog());
		ps.setSubmitASAP(bmc.getSubmitASAP());
		ps.setDetectChildRelease(bmc.getDetectChildRelease());
		ps.setXbpVersion(bmc.getXbpVersion());
		ps.setDetectOption(bmc.getDetectOption());
		ps.setIncAppStat(bmc.getIncAppStat());
		ps.setRerunStepNum(bmc.getRerunStepNum());

		//String datafile = this.getBmcDataModel().getConfigeProvider().getSapDataFile();

//		if (datafile != null) {
//
//			if (SAPDATALIST == null) {
//				File file = new File(datafile);
//				log.debug("doHandleSAP Reading Data [" + datafile + "]");
//				SAPDATALIST = AbstractCsvImporter.fromFile(file, CsvSAPData.class);
//			}


			CsvSAPData sapdata = SAPImporter.getDataByJobName(ps.getJobName());

			if (sapdata != null) {
				// We found our matching job. Not really needed but just in case we need future processing.
				bmc.setSapData(sapdata);
				// This should pick them all up if they are named the same.
				// ObjectUtils.copyMatchingFields(sapdata, ps);

				ps.setProgramName(bmc.getSapData().getProgramName());
				ps.setVariant(bmc.getSapData().getVariant());
				ps.setPdest(bmc.getSapData().getPdest());

				ps.setPrcop(bmc.getSapData().getPrcop());
				ps.setPlist(bmc.getSapData().getPlist());
				ps.setPrtxt(bmc.getSapData().getPrtxt());
				ps.setPrber(bmc.getSapData().getPrber());

				log.debug("doHandleSAP Processing data for [" + ps.getFullPath() + "]");
			} else {
				log.debug("doHandleSAP Missing data for [" + ps.getFullPath() + "] setting a placeholder only job");
				ps.setJobMode("MISSING DATA");

			}
//		} else {
//			log.debug("doHandleSAP Skipping No data File Loaded for [" + ps.getFullPath() + "] setting a placeholder only job");
//			// ps.setExtendedInfo(osj.getExtenedInfo());
//		}

		// ps.setExtendedInfo(osj.getExtenedInfo());

		if (ps.getAccount() != null) {
			CsvRuntimeUser rte = new CsvRuntimeUser(ps.getAccount());
			rte.setPasswordForSAP("tidal");
			getTidalDataModel().addRunTimeUserToJobOrGroup(ps, rte);
		}

		return ps;
	}

	private BaseCsvJobObject doHandleCommandLine(BMCCommandLineJob osj) {
		CsvOSJob dest = new CsvOSJob();
		setJobCommandDetail(dest, osj);
		return dest;
	}

	private BaseCsvJobObject doHandleCatchAllJobs(BaseBMCJobOrFolder job) {
		CsvOSJob dest = new CsvOSJob();
		dest.setCommandLine("PLACEHOLDER");
		job.setName(job.getBMCJobType().name() + "-PLACEHOLDER-" + job.getName());
		dest.setNotes(job.getPlaceHolderData());
		dest.setName(job.getName());

		return dest;

	}

	public static void setJobCommandDetail(CsvOSJob dest, BMCCommandLineJob cmdjob) {

		String cmd = cmdjob.getCommandLine();

		if (StringUtils.isBlank(cmd)) {
			cmdjob.setName(cmdjob.getName() + "-CMDERROR");
			dest.setName(cmdjob.getName());
			// log.debug("Missing command for JOB[" + dest.getName() + "], setting
			dest.setCommandLine("XXXX");
			return;
		}

		// cmd = BMCDataUtil.replaceVariables(cmdjob.getCommandLine(),
		// cmdjob.getSetVarData(), "null");

		// replacevariables above returns "null" for null so we need to remove those.
		if (cmd.contains("\"null\"")) {
			cmd = cmd.replace("\"null\"", "");
		}

		APIJobUtils.setJobCommandDetail(dest, cmd);

		dest.setWorkingDirectory(cmdjob.getWorkingDir());

	}

	private boolean getIsFolderType(BaseBMCJobOrFolder base) {

		if ((base instanceof BMCSimpleFolder) || (base instanceof BMCSmartFolder) || (base instanceof BMCSubFolder)) {
			return true;
		}
		return false;
	}

	private void setVariables(BaseBMCJobOrFolder bmc, BaseCsvJobObject tidal) {

		if (bmc.getLocalVariables().isEmpty()) {
			return;
		}

		bmc.getLocalVariables().forEach(f -> {
			CsvVariable var = new CsvVariable(f.getNAME());
			var.setVarValue(f.getVALUE());
			tidal.getVariables().add(var);
		});
	}

	private void doProcessResources(BaseBMCJobOrFolder bmc, BaseCsvJobObject tidal) {
		// This job is missing its resources.
		if (bmc.getName().equals("sinar001")) {
			bmc.getName();
		}

		if (this.getBmcDataModel().getConfigeProvider().includeQauntitativeResources()) {
			final String QUANT_CTL_PRE = this.getBmcDataModel().getConfigeProvider().qauntitativeResourcePrefix();
			final Integer QANT_TCL_CNT = this.getBmcDataModel().getConfigeProvider().qauntitativeResourceCount();

			bmc.getQuantitativeResourceData().forEach(f -> {
				String key = QUANT_CTL_PRE + f.getNAME().trim();
				CsvResource res = new CsvResource(key, tidal.getOwner());
				res.setLimit(QANT_TCL_CNT);
				getTidalDataModel().addResourceToJob(tidal, res);
			});

			final String CTL_PRE = this.getBmcDataModel().getConfigeProvider().controlResourcePrefix();
			final Integer TCL_CNT = this.getBmcDataModel().getConfigeProvider().controlResourceCount();

			if (this.getBmcDataModel().getConfigeProvider().includeControlResources()) {
				bmc.getControlResourceData().forEach(f -> {
					String key = CTL_PRE + f.getNAME().trim();
					CsvResource res = new CsvResource(key, tidal.getOwner());
					res.setLimit(TCL_CNT);
					getTidalDataModel().addResourceToJob(tidal, res);
				});
			}
		}

		if (bmc.getName().equals("sinar001")) {
			bmc.getName();
		}
	}

	// Map<String, List<BaseCsvJobObject>> OutConditionDataMap = new ConcurrentHashMap<>();
	// Map<String, BaseCsvJobObject> JobDepMapping = new ConcurrentHashMap<>();
	//
	// private void doProcessJobDepGraph(BaseCsvJobObject job, BaseBMCJobOrFolder bmc) {
	// String jobkey = bmc.getFullPath();
	//
	// if (!JobDepMapping.containsKey(jobkey)) {
	// JobDepMapping.put(jobkey, job);
	// }
	//
	// // REG our OUTCON lookup map to simplify out dep graph building.
	// bmc.getOutConditionData().forEach(f -> {
	// if (f.getSIGN().equals("+")) { // If it is to add the OUTCON ONLY
	// String key = f.getNAME(); // My OutConditionName;
	//
	// if (OutConditionDataMap.containsKey(key)) {
	// OutConditionDataMap.get(key).add(job);
	// } else {
	// List<BaseCsvJobObject> j = new ArrayList<>();
	// j.add(job);
	// OutConditionDataMap.put(key, j);
	// }
	// }
	// });
	//
	// }
	//
	// public void doProcessJobDeps(BaseBMCJobOrFolder base) {
	//
	// if (base.getName().contains("DEVDLY")) {
	// log.debug("");
	// }
	//
	// // BaseCsvJobObject me = getDatamodel().findFirstJobByFullPath(base.getFullPath());
	//
	// BaseCsvJobObject me = JobDepMapping.get(base.getFullPath());
	//
	// if (me == null) {
	// // Ignore group with no children or parents.. We know we did not process them.
	// if (base.getChildren().isEmpty() && getIsFolderType(base)) {
	// // Ignore.
	// } else {
	// log.error("ERROR Unable to locate Job[" + base.getFullPath() + "]\n");
	// }
	// return;
	// }
	//
	// base.getInConditionData().forEach(incon -> {
	//
	// String key = incon.getNAME();
	//
	// List<BaseCsvJobObject> depsonjobs = OutConditionDataMap.get(key);
	//
	// if (depsonjobs == null) {
	// log.error("ERROR Unable to locate any outconditions that match our incondition[" + key + "] for job[" + base.getFullPath() + "]");
	// } else {
	// depsonjobs.forEach(dep -> {
	//
	// Integer dateOffset = null;
	// if (incon.getODATE().equals("ODAT")) {
	// // Do nothing , default dep
	// } else if (incon.getODATE().equals("PREV")) {
	// dateOffset = 1;
	// } else if (incon.getODATE().equals("****")) {
	// dateOffset = 1;
	// } else {
	// log.error("doProcessJobDeps Job[" + me.getFullPath() + "] that depends on [" + dep.getFullPath() + "] ODATE is UNKNOWN["
	// + incon.getODATE() + "]");
	// }
	//
	// if (me.getId() == dep.getId()) {
	// // log.debug("ERROR DEP LOOP Job[" + dep.getFullPath() + "] For Job[" +depsonme.getFullPath() + "]");
	//
	// } else {
	// log.debug("Registering Dependency for Job[" + me.getFullPath() + "] that depends on [" + dep.getFullPath() + "]");
	// getDatamodel().addJobDependencyForJobCompletedNormal(me, dep, dateOffset);
	// }
	//
	// });
	// }
	// });
	//
	// if (!base.getChildren().isEmpty()) {
	// base.getChildren().forEach(f -> doProcessJobDeps((BaseBMCJobOrFolder) f));
	// }
	// }
}
