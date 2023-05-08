package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitor;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspAfterStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspExitCodeStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspJobResourceStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspNoRunStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspNotWithStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspPrereqStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspReleaseStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspRunStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public abstract class EspAbstractJob extends BaseJobOrGroupObject implements EspJob {

	boolean containsIfLogic = false;
	boolean containsAdvancedDelaySubLogic = false;
	boolean containsAdvancedDueOutLogic = false;

	public abstract EspJobType getJobType();
	
	String externalAppID; //EXTERNAL APPLID(JOBNAME) this job must wait on this other one. 
	String agent;// at the workload object or the application level
	// TODO: How should CCCHK statement be converted into TIDAL? Is there any adequate definition/implementation which already exists in TIDAL?
	String comment; // Comment inside of Job definition which is later used for storing in TIDAL as Notes for same Job Definition (JIRA Task already created)
	String abandonSubmission;
	String delaySubmission;
	String reDelaySubmission;
	String dueout; // use it for SLAs in TIDAL if and only if necessary for conversion
	String earlySubmission;
	String lateSubmission;
	String espStatement;
	String espNoMsg;
	String invokeObject;
	String exitCode;
	List<String> ccchk = new ArrayList<>(); // Specify the action taken if a job, step, procstep or program produces a specified condition code.
	//CCCHK RC(1) OK CONTINUE

	List<String> noteData = new ArrayList<>();

	// String notedata;
	String options;
	String pid;
	int relDelay;
	String startMode;
	String transferCodeType;
	EspPrereqStatement prerequisite; // similar to INCOND in BMC; Specifies which jobs need to be completed in order for current job to start
	String user;
	String tag;
	List<String> notifyList = new ArrayList<>();
	List<EspJobResourceStatement> resources = new ArrayList<>();
	List<EspAfterStatement> espAfterStatements = new ArrayList<>();
	List<EspExitCodeStatement> exitCodeStatements = new ArrayList<>();
	List<EspNotWithStatement> espNotWithStatements = new ArrayList<>();
	List<EspRunStatement> espRunStatements = new ArrayList<>();
	List<EspNoRunStatement> espNoRunStatements = new ArrayList<>(); // (partially) overrides runFrequency
	List<EspReleaseStatement> espReleasedJobDependencies = new ArrayList<>(); // similar to OUTCOND in BMC. E.g., RELEASE(JOB1_NAME, JOB2_NAME)



	public EspAbstractJob(String name) {
		this.name = name;
	}

	@Override
	public boolean isGroup() {
		return false;
	}

	@Override
	public void processData(EspJobVisitor espJobVisitor, List<String> lines) {
		espJobVisitor.doProcess(this, lines);
	}
}
