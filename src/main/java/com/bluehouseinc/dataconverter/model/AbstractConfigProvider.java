package com.bluehouseinc.dataconverter.model;

import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.Data;

@Data
public abstract class AbstractConfigProvider {

	private ConfigurationProvider provider;
	private String SKIPDEPS = "tidal.skipdeps";
	private String APPEND_JOB_TYPE = "tidal.agent.appendjobtype";
	private String TIDAL_ConcurrentType ="tidal.job.ConcurrentType";
	
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
	
	public String getTidalConcurrentType() {
		return this.getProvider().getConfigurations().getOrDefault(TIDAL_ConcurrentType, "RUNANYWAY");
	}
}
