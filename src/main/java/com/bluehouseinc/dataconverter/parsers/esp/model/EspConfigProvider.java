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
		return getProvider().get("esp.path");
	}

	public String getEspEventDataFile() {
		return getProvider().get(DATAFILE);
	}

	public String getFilesToSkip() {
		return getProvider().getOr("esp.skipfiles","");
	}
	
	public String getFilesToInclude() {
		return getProvider().getOr("esp.includefiles","");
	}
	
	public String getZosLibPath() {
		return getProvider().get("ESP.LIB.PATH");
	}
	
	public String getZosLibDefaultAgent() {
		return getProvider().get("ESP.AGENT.NAME");
	}
	
	
	public String getZosLibDefaultRuntimeUser() {
		return getProvider().get("ESP.RUNTIME.USER");
	}
	
	
	public String getEspMailListDataPath() {
		return getProvider().get("esp.mail.datafile");
	}
	
	public String getEspMailSubject() {
		return getProvider().get("esp.mail.subject");
	}

	public String getEspMailBody() {
		return getProvider().get("esp.mail.body");
	}

}
