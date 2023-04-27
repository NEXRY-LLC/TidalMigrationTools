package com.bluehouseinc.dataconverter.parsers.autosys;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class AutoSysConfProvider extends AbstractConfigProvider {



	public AutoSysConfProvider(ConfigurationProvider provider) {
		super(provider);
	}

	public Path getAutoSysPath() {
		String filePath = getProvider().getOrThrow("autosys.path");
		
		if(filePath == null) {
			return null;
		}
		
		return Paths.get(filePath);
	}

	@Override
	public void checkAndThrowEarly() throws TidalException {
		getAutoSysPath();
	}


	public boolean useGroupContainer() {
		String tf = getProvider().getConfigurations().getOrDefault("AUTOSYS.UseGroupContainer", "false");

		return Boolean.valueOf(tf);
	}


	public boolean clearBoxConditions() {
		String tf = getProvider().getConfigurations().getOrDefault("AUTOSYS.ClearBoxConditions", "false");
		return Boolean.valueOf(tf);
	}


	public boolean disableCaryOverOnRerun() {
		String tf = getProvider().getConfigurations().getOrDefault("AUTOSYS.DisableCaryOverOnRerun", "false");
		return Boolean.valueOf(tf);
	}


	public String offSetTimeStart() {
		return getProvider().getConfigurations().getOrDefault("AUTOSYS.OffSetTimeStart", "00:00");
	}

	public String offSetTimeEnd() {
		return getProvider().getConfigurations().getOrDefault("AUTOSYS.OffSetTimeEnd", "00:00");
	}

	public String offSetCalendarPrefix() {
		return getProvider().getConfigurations().getOrDefault("AUTOSYS.OffSetCalendarPrefix", "");
	}

	public Integer offSetCalendarDays() {
		return Integer.valueOf( getProvider().getConfigurations().getOrDefault("AUTOSYS.OffSetCalendarDays", "0"));
	}


}
