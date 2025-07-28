package com.bluehouseinc.dataconverter.model;

import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.Data;

@Data
public abstract class AbstractConfigProvider {

	private ConfigurationProvider provider;
	private String SKIPDEPS = "tidal.skipdeps";
	private String SKIPEMPTYGROUPS = "tidal.skip.empty.groups";
	private String APPEND_JOB_TYPE = "tidal.agent.appendjobtype";
	private String TIDAL_CONTYPE = "tidal.job.concurrenttype";
	private String TIDAL_ADD_AGENTS_ON_IMPORT = "tidal.add.agents.on.import";
	private String TIDAL_TIMEOUT_OPTION = "tidal.job.timeoutoption";
	private String TIDAL_DEP_LOOP_CHECK = "tidal.check.dependency.loop";
	private String TIDAL_SAVE_OUT_OPTION = "tidal.job.save.output.option";
	private String TIDAL_FORMAT_PARAMS = "tidal.job.format.params";
	
	public AbstractConfigProvider(ConfigurationProvider provider) {
		this.provider = provider;

		checkAndThrowEarly();
	}

	public abstract void checkAndThrowEarly() throws TidalException;

	public boolean skipDependencyProcessing() {
		String tf = this.getProvider().getConfigurations().getOrDefault(SKIPDEPS, "false");
		return Boolean.valueOf(tf);
	}

	public boolean appendJobTypeToAgentName() {
		String tf = this.getProvider().getConfigurations().getOrDefault(APPEND_JOB_TYPE, "false");
		return Boolean.valueOf(tf);
	}

	public String getTidalConcurrentTypes() {
		return this.getProvider().getConfigurations().getOrDefault(TIDAL_CONTYPE, "RUNANYWAY");
	}

	public boolean getTidalAddAgentsOnImport() {
		String tf = this.getProvider().getConfigurations().getOrDefault(TIDAL_ADD_AGENTS_ON_IMPORT, "false");
		return Boolean.valueOf(tf);
	}

	public boolean isSkipEmptyGroups() {
		String tf = this.getProvider().getConfigurations().getOrDefault(SKIPEMPTYGROUPS, "true");
		return Boolean.valueOf(tf);
	}

	public String getTidalJobTimeOutOption() {
		return this.getProvider().getConfigurations().getOrDefault(TIDAL_TIMEOUT_OPTION, "DONOTTIMEOUT");
	}

	public boolean checkDependencyLoop() {
		String tf = this.getProvider().getConfigurations().getOrDefault(TIDAL_DEP_LOOP_CHECK, "true");
		return Boolean.valueOf(tf);
	}

	public String getJobSaveOutputOption() {
		return this.getProvider().getConfigurations().getOrDefault(TIDAL_SAVE_OUT_OPTION, "APPEND");
	}

	public boolean formatJobParams() {
		String tf = this.getProvider().getConfigurations().getOrDefault(TIDAL_FORMAT_PARAMS, "true");
		return Boolean.valueOf(tf);
	}
	
	public boolean bfusaFixSqlplus() {
		String tf = getProvider().getOr("bfusa.fix.sqlplus", "false");
		return Boolean.parseBoolean(tf);
	}
}
