package com.bluehouseinc.dataconverter.parsers.esp.model;


import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

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
		return getProvider().getOr("esp.skipfiles",null);
	}
	
	public String getFilesToInclude() {
		return getProvider().getOr("esp.includefiles",null);
	}
	
	public String getZosLibPath() {
		return getProvider().get("esp.lib.path");
	}
	
	public String getZosLibDefaultAgent() {
		return getProvider().get("esp.agent.name");
	}
	
	
	public String getZosLibDefaultRuntimeUser() {
		return getProvider().get("esp.zos.runtime.user");
	}
	
	public String getAS400DefaultRuntimeUser() {
		return getProvider().get("esp.as400.runtime.user");
	}
	
	public String getAS400RuntimeUserAgentMapData() {
		return getProvider().get("esp.as400.runtime.agentmap");
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

	
	public String getAS400RuntimeUserByAgentName(String node) {

		String mapdata = getAS400RuntimeUserAgentMapData();
		
		if(StringUtils.isBlank(node) || StringUtils.isBlank(mapdata)) {
			return getAS400DefaultRuntimeUser();
		}
		
		
		String newruntime =  ObjectUtils.replaceWithData(node,mapdata,true);
		
		if(StringUtils.isBlank(newruntime)){
			return getAS400DefaultRuntimeUser();
		}
		
		return newruntime;
	}
	
}
