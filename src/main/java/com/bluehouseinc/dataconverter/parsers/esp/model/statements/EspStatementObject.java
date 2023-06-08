package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class EspStatementObject {

	EspPrereqStatement prerequisite; // similar to INCOND in BMC; Specifies which jobs need to be completed in order for current job to start

	List<EspJobResourceStatement> resources = new ArrayList<>();
	List<EspAfterStatement> espAfterStatements = new ArrayList<>();
	List<EspExitCodeStatement> exitCodeStatements = new ArrayList<>();
	List<EspNotWithStatement> espNotWithStatements = new ArrayList<>();
	List<EspRunStatement> espRunStatements = new ArrayList<>();
	List<EspNoRunStatement> espNoRunStatements = new ArrayList<>(); // (partially) overrides runFrequency
	List<EspReleaseStatement> espReleasedJobDependencies = new ArrayList<>(); // similar to OUTCOND in BMC. E.g., RELEASE(JOB1_NAME, JOB2_NAME)


}
