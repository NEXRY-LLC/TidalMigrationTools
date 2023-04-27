package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspReleaseStatement {

	String condition; // For example: RELEASE ADD(X_TEST_CYBERMATION_BFT030) COND(NOT RC(0))
	List<String> releaseJobs = new ArrayList<>(); // E.g. RELEASE (JOB1, JOB2)

}
