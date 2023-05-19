package com.bluehouseinc.dataconverter.model;

import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.Data;

@Data
public abstract class AbstractConfigProvider {

	private ConfigurationProvider provider;
	private String SKIPDEPS = "tidal.skipdeps";
	
	public AbstractConfigProvider(ConfigurationProvider provider) {
		this.provider = provider;

		checkAndThrowEarly();
	}


	public abstract void checkAndThrowEarly() throws TidalException;

	public boolean skipDependencyProcessing() {
		String tf = this.getProvider().getConfigurations().getOrDefault(SKIPDEPS, "false");
		return Boolean.valueOf(tf);
	}
}
