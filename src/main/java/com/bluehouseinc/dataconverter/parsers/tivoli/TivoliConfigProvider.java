package com.bluehouseinc.dataconverter.parsers.tivoli;

import java.io.File;
import java.nio.file.Paths;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class TivoliConfigProvider extends AbstractConfigProvider {
	private static String JOB_FILE = "tivoli.job.file";
	private static String VAR_FILE = "tivoli.var.file";
	private static String SCD_FILE = "tivoli.schedule.file";
	private static String RES_FILE = "tivoli.resource.file";
	private static String CAL_FILE = "tivoli.ca.file";
	private static String PAR_FILE = "tivoli.params.file";
	private static String CPU_FILE = "tivoli.cpu.file";

	public TivoliConfigProvider(ConfigurationProvider provider) {
		super(provider);

	}

	@Override
	public void checkAndThrowEarly() throws TidalException {

	}

	public File getJobFile() {
		return doGetFile(getProvider().getOrThrow(JOB_FILE));
	}

	public File getVariableFile() {
		return doGetFile(getProvider().getOrThrow(VAR_FILE));
	}

	public File getScheduleFile() {
		return doGetFile(getProvider().getOrThrow(SCD_FILE));
	}

	public File getResourceFile() {
		return doGetFile(getProvider().getOrThrow(RES_FILE));
	}

	public File getCalendarFile() {
		return doGetFile(getProvider().getOrThrow(CAL_FILE));
	}

	public File getParamsFile() {
		return doGetFile(getProvider().getOrThrow(PAR_FILE));
	}

	public File getCPUFile() {
		return doGetFile(getProvider().getOrThrow(CPU_FILE));
	}

	private File doGetFile(String filename) {

		File file = Paths.get(filename).toFile();

		if (file.exists()) {
			return file;
		}

		throw new TidalException("doGetFile() -> Unable to locate file[" + filename + "]");
	}
}
