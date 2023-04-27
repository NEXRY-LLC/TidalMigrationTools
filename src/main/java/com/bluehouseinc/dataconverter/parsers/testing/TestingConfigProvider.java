package com.bluehouseinc.dataconverter.parsers.testing;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class TestingConfigProvider extends AbstractConfigProvider {

	public TestingConfigProvider(ConfigurationProvider provider) {
		super(provider);
	}

	@Override
	public void checkAndThrowEarly() throws TidalException {


	}

}
