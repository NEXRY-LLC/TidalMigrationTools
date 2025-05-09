package com.bluehouseinc.dataconverter.parsers.bmcmainframe;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class BMCMainFrameConfigProvider extends AbstractConfigProvider {

	public BMCMainFrameConfigProvider(ConfigurationProvider provider) {
		super(provider);
	}

	@Override
	public void checkAndThrowEarly() throws TidalException {
		// TODO Auto-generated method stub
		
	}

}
