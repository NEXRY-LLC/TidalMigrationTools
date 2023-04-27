package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspExitCodeStatement {

	// TODO-5: Check is this ESP statement necessary for conversion into TIDAL both on JobGroup and Job definition level!
	// Examples for marking which range of values will have certain EspExitCodeStatementJobCompletionStatus as outcome
	// EXITCODE 0-1 FAILURE
	// EXITCODE 2 SUCCESS
	// EXITCODE 3-9099 FAILURE
	String range;
	EspExitCodeStatementJobCompletionStatus statementJobCompletionStatus;

	public EspExitCodeStatement(String range, EspExitCodeStatementJobCompletionStatus statementJobCompletionStatus) {
		this.range = range;
		this.statementJobCompletionStatus = statementJobCompletionStatus;
	}

	public enum EspExitCodeStatementJobCompletionStatus {
		SUCCESS, FAILURE
	}
}
