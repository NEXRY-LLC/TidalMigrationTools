package com.bluehouseinc.dataconverter.parsers.opc.model;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class OPCConfigProvider extends AbstractConfigProvider {

	public OPCConfigProvider(ConfigurationProvider provider) {
		super(provider);

	}

	@Override
	public void checkAndThrowEarly() throws TidalException {
		getOPCPath();
	}

	public String getOPCPath() {
		return getProvider().getOrThrow("opc.path");
	}
}
