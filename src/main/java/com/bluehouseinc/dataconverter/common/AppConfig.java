package com.bluehouseinc.dataconverter.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
	@Value("${configuration.path}") public String CONFIGURATION_FILE;
}
