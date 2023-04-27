package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.importers.csv.CsvSAPData;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class BMCSAPJob extends BaseBMCJobOrFolder {

	List<SetVarData> unusedVarData = new ArrayList<>();

	String groupOrdID;
	String detectChildTable;
	SAPJobModes jobMode;
	String account;
	String jobName;
	String serverOrGroupType;
	String jobSAPClass;
	String jobCountType;
	Integer jobCount;
	String startStep;
	String keepJoblogOption;
	String overrideJoblogDefault;
	String jobLog;
	String submitASAP;
	String detectChildRelease;
	String xbpVersion;
	String detectOption;
	String incAppStat;
	String rerunStepNum;

	CsvSAPData sapData;

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.SAP;
	}

	@Override
	public String getPlaceHolderData() {
		StringBuilder b = new StringBuilder();

		this.unusedVarData.stream().forEach(data -> {

			b.append(data.getNAME());
			b.append("=");
			b.append(data.getVALUE());
			b.append("\n");
		});

		return b.toString();
	}

	@Override
	public void doProcesJobSpecificVarData(SetVarData data) {

		if (data.getNAME().equalsIgnoreCase("%%SAPR3-GROUP_ORDID")) {
			this.groupOrdID = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-DETECT_CHILD_TABLE")) {
			this.detectChildTable = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-JOB_MODE")) {
			SAPJobModes modes = SAPJobModes.valueOf(data.getVALUE());
			this.jobMode = modes;
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-ACCOUNT")) {
			this.account = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-JOBNAME")) {
			this.jobName = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-SERVER_OR_GROUP_TYPE")) {
			this.serverOrGroupType = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-JOBCLASS")) {
			this.jobSAPClass = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-JOB_COUNT")) {
			this.jobCountType = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-JOBCOUNT")) {
			try {
				this.jobCount = Integer.valueOf(data.getVALUE());
			} catch (NumberFormatException e) {
				// eat

			}
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-START_STEP")) {
			this.startStep = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-KEEP_JOBLOG_OPTION")) {
			this.keepJoblogOption = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-OVERRIDE_JOBLOG_DEFAULT")) {
			this.overrideJoblogDefault = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-JOBLOG")) {
			this.jobLog = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-SUBMIT_ASAP")) {
			this.submitASAP = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-DETECT_CHILD_RELEASE")) {
			this.detectChildRelease = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-XBP_VERSION")) {
			this.xbpVersion = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-DETECT_OPTION")) {
			this.detectOption = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-INC_APP_STAT")) {
			this.incAppStat = data.getVALUE();
		} else if (data.getNAME().equalsIgnoreCase("%%SAPR3-RERUN_STEP_NUM")) {
			this.rerunStepNum = data.getVALUE();
		} else {
			this.unusedVarData.add(data);
		}

	}

	@Override
	public String getVarPrefix() {
		return "%%SAPR3";
	}

	/*
	 * <VARIABLE NAME="%%SAPR3-GROUP_ORDID" VALUE="%%GROUP_ORDID" />
	 * <VARIABLE NAME="%%SAPR3-DETECT_CHILD_TABLE" VALUE="%%SCHEDTAB" />
	 * <VARIABLE NAME="%%SAPR3-JOB_MODE" VALUE="RUN_COPY" />
	 * <VARIABLE NAME="%%SAPR3-ACCOUNT" VALUE="PRODSAP" />
	 * <VARIABLE NAME="%%SAPR3-JOBNAME" VALUE="ymmcbid1" />
	 * <VARIABLE NAME="%%SAPR3-SERVER_OR_GROUP_TYPE" VALUE="S" />
	 * <VARIABLE NAME="%%SAPR3-JOBCLASS" VALUE="C" />
	 * <VARIABLE NAME="%%SAPR3-JOB_COUNT" VALUE="Specific_Job" />
	 * <VARIABLE NAME="%%SAPR3-JOBCOUNT" VALUE="11465900" />
	 * <VARIABLE NAME="%%SAPR3-START_STEP" VALUE="1" />
	 * <VARIABLE NAME="%%SAPR3-KEEP_JOBLOG_OPTION" VALUE="S" />
	 * <VARIABLE NAME="%%SAPR3-OVERRIDE_JOBLOG_DEFAULT" VALUE="X" />
	 * <VARIABLE NAME="%%SAPR3-JOBLOG" VALUE="*SYSOUT" />
	 * <VARIABLE NAME="%%SAPR3-SUBMIT_ASAP" VALUE="X" />
	 * <VARIABLE NAME="%%SAPR3-DETECT_CHILD_RELEASE" VALUE="N" />
	 * <VARIABLE NAME="%%SAPR3-XBP_VERSION" VALUE="XBP30" />
	 * <VARIABLE NAME="%%SAPR3-DETECT_OPTION" VALUE="1" />
	 * <VARIABLE NAME="%%SAPR3-INC_APP_STAT" VALUE="no" />
	 * <VARIABLE NAME="%%SAPR3-RERUN_STEP_NUM" VALUE="1" />
	 */

	public enum SAPJobModes {
		RUN_COPY, PC_RUN_ORG, CREATE;
	}
}
