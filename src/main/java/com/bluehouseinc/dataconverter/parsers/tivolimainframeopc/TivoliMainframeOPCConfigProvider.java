package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

/**
 * Configuration provider for Tivoli Mainframe OPC parser.
 * Manages configuration properties and settings for the parser.
 */

public class TivoliMainframeOPCConfigProvider extends AbstractConfigProvider {

    public TivoliMainframeOPCConfigProvider(ConfigurationProvider provider) {
		super(provider);
	}

    /**
     * Get the library path for ASYS workstation jobs
     */
    public String getTivoliLibPath() {
    	return getProvider().getOrThrow("tivoli.lib.path");
    }

    public String getCA7FilePath() {
    	return getProvider().getOrThrow("tivoli.ca7.file.path");
    }

	@Override
	public void checkAndThrowEarly() throws TidalException {
		// TODO Auto-generated method stub

	}
}
