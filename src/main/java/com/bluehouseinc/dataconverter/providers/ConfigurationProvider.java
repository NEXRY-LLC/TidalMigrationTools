package com.bluehouseinc.dataconverter.providers;

import java.io.File;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.AppConfig;
import com.bluehouseinc.dataconverter.input.ConfigurationFileReader;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

@Component
public class ConfigurationProvider {
	AppConfig appCfg;
	ConfigurationFileReader reader;

	private Map<String, String> configuration;

	@Autowired
	void setAppCfg(AppConfig appCfg) {
		this.appCfg = appCfg;
	}

	@Autowired
	void setReader(ConfigurationFileReader reader) {
		this.reader = reader;
	}

	/**
	 * Reads configuration from file
	 *
	 * @return
	 */
	public Map<String, String> readConfigurationFile() {
		File f = new File(appCfg.CONFIGURATION_FILE);

		configuration = reader.readFile(f);

		String customer = configuration.get("tidal.customer");

		if (customer == null) {
			throw new TidalException("Must set the tidal.customer =  (folder) to read from");
		}

		File customerfile = new File(f.getParent() + File.separator + customer + File.separator + "application.properties");
		Map<String, String> customerconfig = reader.readFile(customerfile);

		configuration.putAll(customerconfig); // Add customer specific

		return configuration;
	}

	/**
	 * Get configuration map
	 *
	 * @return
	 */
	public Map<String, String> getConfigurations() {
		if (this.configuration == null) {
			readConfigurationFile();
		}

		return configuration;
	}

	public String get(String obj) {
		return getConfigurations().getOrDefault(obj, null);
	}

	public String getOr(String obj, String or) {

		String res = get(obj);

		if (res == null) {
			return or;
		}

		return res;
	}

	public String getOrThrow(String obj) {

		String res = get(obj);

		if (res == null) {
			throw new TidalException("Data Not Present in Config File using Key[" + obj + "]");
		}

		return res;
	}
}
