package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.JobType;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = false)
public class CsvSAPJob extends BaseCsvJobObject {

	@CsvBindByName
	String groupOrdID;
	@CsvBindByName
	String detectChildTable;
	@CsvBindByName
	String jobMode = "RUN_COPY";
	@CsvBindByName
	String account;
	@CsvBindByName
	String jobName;
	@CsvBindByName
	String serverOrGroupType;
	@CsvBindByName
	String jobSAPClass;
	@CsvBindByName
	String jobCountType;
	@CsvBindByName
	Integer jobCount;
	@CsvBindByName
	String startStep;
	@CsvBindByName
	String keepJoblogOption;
	@CsvBindByName
	String overrideJoblogDefault;
	@CsvBindByName
	String jobLog;
	@CsvBindByName
	String submitASAP;
	@CsvBindByName
	String detectChildRelease;
	@CsvBindByName
	String xbpVersion;
	@CsvBindByName
	String detectOption;
	@CsvBindByName
	String incAppStat;
	@CsvBindByName
	String rerunStepNum;

	@CsvBindByName
	String programName;
	@CsvBindByName
	String variant;
	@CsvBindByName
	String pdest;
	@CsvBindByName
	String prcop;
	@CsvBindByName
	String plist;
	@CsvBindByName
	String prtxt;
	@CsvBindByName
	String prber;
	@CsvBindByName
	Integer printColumns;
	@CsvBindByName
	String printRecipient;
	@CsvBindByName
	String printDept;
	@CsvBindByName
	Integer printExpire;
	@CsvBindByName
	Integer printCopies;
	@CsvBindByName
	Integer printRows;
	@CsvBindByName
	String printFormat;
	@CsvBindByName
	String printSpoolName;
	/*
	 * %%SAPR3-GROUP_ORDID=<JobID>
	 * %%SAPR3-DETECT_CHILD_TABLE=<Group.SCHEDTAB>
	 * %%SAPR3-JOB_MODE=RUN_COPY
	 * %%SAPR3-ACCOUNT=PRODSAP
	 * %%SAPR3-JOBNAME=ysjobcan
	 * %%SAPR3-SERVER_OR_GROUP_TYPE=S
	 * %%SAPR3-JOBCLASS=C
	 * %%SAPR3-JOB_COUNT=Specific_Job
	 * %%SAPR3-JOBCOUNT=10141400
	 * %%SAPR3-START_STEP=1
	 * %%SAPR3-KEEP_JOBLOG_OPTION=S
	 * %%SAPR3-OVERRIDE_JOBLOG_DEFAULT=X
	 * %%SAPR3-JOBLOG=*SYSOUT
	 * %%SAPR3-SUBMIT_ASAP=X
	 * %%SAPR3-DETECT_CHILD_RELEASE=N
	 * %%SAPR3-XBP_VERSION=XBP30
	 * %%SAPR3-DETECT_OPTION=1
	 * %%SAPR3-INC_APP_STAT=no
	 * %%SAPR3-RERUN_STEP_NUM=1
	 */
	// String extendedInfo;

	@Override
	public JobType getType() {
		return JobType.SAPJOB;
	}

}
