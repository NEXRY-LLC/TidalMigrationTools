package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.impl;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysAbstractJob;
import com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.AutosysJobVisitor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class AutosysSqlAgentJob extends AutosysAbstractJob {

	String sqlAgentUserName; // the user that the agent uses to connect to the database. The user can be a Windows domain user for Windows domain authentication or a Microsoft SQL Server user for Microsoft SQL Server authentication.
	String sqlAgentDomainName; // specifies the Windows domain name when Windows authentication is used to connect to the database server.
	String sqlAgentTargetDb; // target server database
	String sqlAgentJobname; // Specifies the name for a SQL Server Agent job
	String sqlAgentServerName; // specifies the MSSQL server to start the job on

	public AutosysSqlAgentJob(String name) {
		super(name);
	}

	@Override
	public void accept(AutosysJobVisitor autosysJobVisitor, List<String> lines, List<AutosysAbstractJob> parents) {
		autosysJobVisitor.visit(this, lines, parents);
	}
}
