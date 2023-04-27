package com.bluehouseinc.dataconverter.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class ConfigurationFileReader {

	/**
	 * Reads properties file from path
	 *
	 * @param filePath
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, String> readFile(File file) {
		final Properties props = new Properties();
		try {
			props.load(new FileInputStream(file));

			return (Map) props;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
