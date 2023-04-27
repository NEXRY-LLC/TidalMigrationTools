package com.bluehouseinc.dataconverter.parsers.tivoli;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class TivoliConfigProvider extends AbstractConfigProvider {

	public TivoliConfigProvider(ConfigurationProvider provider) {
		super(provider);

	}

	@Override
	public void checkAndThrowEarly() throws TidalException {


	}

}
