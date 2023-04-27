package com.bluehouseinc.dataconverter.parsers.esp.model;


import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class EspConfigProvider extends AbstractConfigProvider {
	private static String DATAFILE = "esp.event.datafile";

	public EspConfigProvider(ConfigurationProvider provider) {
		super(provider);

	}

	@Override
	public void checkAndThrowEarly() throws TidalException {
		getEspDataPath();
		getEspEventDataFile();
	}

	public String getEspDataPath() {
		return getProvider().getOrThrow("esp.path");
	}

	public String getEspEventDataFile() {
		return getProvider().getOrThrow(DATAFILE);
	}
	
	public String getFilesToSkip() {
		return getProvider().getOrThrow("esp.skipfiles");
	}
}
