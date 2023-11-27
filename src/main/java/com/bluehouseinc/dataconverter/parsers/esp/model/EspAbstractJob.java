package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspJobVisitor;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspExternalApplicationData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspLIEData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspLISData;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspStatementObject;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public abstract class EspAbstractJob extends BaseJobOrGroupObject implements EspJob {

	boolean containsIfLogic = false;
	boolean containsIfLogicCleaned = false;
	boolean containsAdvancedDelaySubLogic = false;
	boolean containsAdvancedDueOutLogic = false;
	boolean containsRequestAttribute = false;
	boolean containsScopeAttribute = false;
	boolean containsREALNOWInEarlySub = false;
	boolean containsRELDELAY= false;
	
	List<String> groupsToDependOn = new ArrayList<>();
	
	public abstract EspJobType getJobType();
	
	EspStatementObject statementObject = new EspStatementObject();
	
	List<EspLIEData> LieData = new ArrayList<>();
	List<EspLISData> LisData = new ArrayList<>();
	List<EspExternalApplicationData> externalApplicationDep = new ArrayList<>();
	
	//String externalAppID; //EXTERNAL APPLID(JOBNAME) this job must wait on this other one. 
	String agent;// at the workload object or the application level
	// TODO: How should CCCHK statement be converted into TIDAL? Is there any adequate definition/implementation which already exists in TIDAL?
	String comment; // Comment inside of Job definition which is later used for storing in TIDAL as Notes for same Job Definition (JIRA Task already created)
	String abandonSubmission;
	String delaySubmission;
	//String reDelaySubmission;
	Integer dueout; // use it for SLAs in TIDAL if and only if necessary for conversion
	Integer dueoutMaxrun;
	String earlySubmission;
	String lateSubmission;
	String espStatement;
	String espNoMsg;
	String invokeObject;
	//String exitCode;
	//CCCHK RC(1) OK CONTINUE

	List<String> notesData = new ArrayList<>();

	// String notedata;
	String options;
	String pid;
	int relDelay;
	String startMode;
	String transferCodeType;
	String user;
	List<String> tags = new ArrayList<>();
	List<String> notifyList = new ArrayList<>();
	String jobAttributes;

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
