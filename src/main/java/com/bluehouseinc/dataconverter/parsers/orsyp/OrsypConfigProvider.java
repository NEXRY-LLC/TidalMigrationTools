package com.bluehouseinc.dataconverter.parsers.orsyp;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class OrsypConfigProvider extends AbstractConfigProvider {

	public OrsypConfigProvider(ConfigurationProvider provider) {
		super(provider);
	}

	@Override
	public void checkAndThrowEarly() throws TidalException {
		getOrsypPath();
	}

	public String getOrsypPath() {
		return getProvider().getOrThrow("orsyp.path");
	}
}
