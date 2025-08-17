package com.bluehouseinc.dataconverter.mainimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.api.importer.TidalImporter;
import com.bluehouseinc.dataconverter.common.Commands;
import com.bluehouseinc.dataconverter.common.exceptions.ImportInProgressException;
import com.bluehouseinc.dataconverter.csv.io.CsvExporter;
import com.bluehouseinc.dataconverter.importers.AbstractCsvImporter;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.output.CommandPrompt;
import com.bluehouseinc.dataconverter.parsers.IParser;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.providers.ImportProvider;
import com.bluehouseinc.dataconverter.providers.ParserProvider;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class MainImport {

	private CommandPrompt cmdPrompt;
	private ConfigurationProvider cfgProvider;
	private ParserProvider parserProvider;
	private ImportProvider importProvider;
	private TidalImporter importer;
	TidalDataModel datamodel;

	private Boolean inProgress = false;

	@Autowired
	void setCmdPrompt(CommandPrompt cmdPrompt) {
		this.cmdPrompt = cmdPrompt;
	}

	@Autowired
	void setConfigurationProvider(ConfigurationProvider cfgProvider) {
		this.cfgProvider = cfgProvider;
	}

	@Autowired
	void setParserProvider(ParserProvider parserProvider) {
		this.parserProvider = parserProvider;
	}

	@Autowired
	void setImportProvider(ImportProvider importProvider) {
		this.importProvider = importProvider;
	}

	@Autowired
	void setAbstractImporter(TidalImporter importer) {
		this.importer = importer;
	}

	/**
	 * Handle general user commands
	 *
	 * @param command
	 */
	public void onCommandReceived(Commands command) throws Exception {

		if (passwordPassed()) {

			switch (command) {
			case LOAD:
				loadDataModel(true);
				break;

			case EXPORT:
				saveToCsv();
				break;

			case TESTCM:
				importer.testLogin();
				break;

			case IMPORT:
				importDataModelToTIDAL();
				break;

			case STOP:
				stopExecution();
				break;

			case IMPORTAGENT:
				importer.installAgents(datamodel);
				break;
				
			default:
				cmdPrompt.printOutputLine("Command Not Supported");
			}
		}
	}

	/**
	 * Get configuration map
	 *
	 * @return map
	 */
	public Map<String, String> getConfiguration() {
		cmdPrompt.printOutputLine("Reading properties file...");
		return cfgProvider.readConfigurationFile();
	}

	/** Stop migration process */
	public void stopExecution() {
		if (!inProgress)
			return;

		cmdPrompt.printOutputLine("Stopping...");

		this.datamodel = null;
		// this.importer.getTidal().

		inProgress = false;
		cmdPrompt.printOutputLine("Stopped");
	}

	/** Start migration process */
	private void loadDataModel(boolean reload) throws Exception {
		if (inProgress) {
			throw new ImportInProgressException();
		}

		inProgress = true;
		cmdPrompt.printOutputLine("Loading DataModel");
		try {
			// TODO: Should this following up commented-out code be removed or not?
			// Map<String, String> configuration = this.getConfiguration();
			// String filePath = configuration.get("file.path");

			if (reload || datamodel == null) {
				IParser parser = parserProvider.getParser();

				this.datamodel = parser.processDataModel();
	
				this.importer.printWorkToDo(this.datamodel);
			}

			inProgress = false;
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			cmdPrompt.printErrorLine("StackTrace: {}", Arrays.toString(e.getStackTrace()));
			cmdPrompt.printOutputLine("Flow will stop due to Exception.");
		} finally {
			// stop flow
			stopExecution();
		}
	}

	/** Start migration process */
	private void importDataModelToTIDAL() throws Exception {
		if (inProgress) {
			throw new ImportInProgressException();
		}

		inProgress = true;
		log.info("\nImporting DataModel to TIDAL");
		try {

			inProgress = false;

			loadDataModel(false);

			inProgress = true;

			// this.importer.test();

			this.importer.processDataModel(this.datamodel);

			
			log.info("\nImporting DataModel to TIDAL - Completed");

			inProgress = false;
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			cmdPrompt.printErrorLine("StackTrace: {}", Arrays.toString(e.getStackTrace()));
			cmdPrompt.printOutputLine("Flow will stop due to Exception.");
		} finally {
			// stop flow
			stopExecution();
		}
	}

	/** Start migration process */
	private void saveToCsv() throws Exception {
		if (inProgress) {
			throw new ImportInProgressException();
		}

		inProgress = true;
		cmdPrompt.printOutputLine("Save datamodel to csv");

		try {

			if (this.datamodel == null) {
				this.loadDataModel(false);
			}

			// this.importer.processDataModel(parsedData);

			List<CsvJobGroup> groups = this.datamodel.getJobsByType(CsvJobGroup.class);

			CsvExporter.WriteToFile("groups.csv", groups);

			List<CsvOSJob> jobs = this.datamodel.getJobsByType(CsvOSJob.class);

			CsvExporter.WriteToFile("jobs.csv", jobs);

			CsvExporter.WriteToFile("variables.csv", this.datamodel.getVariables().stream().collect(Collectors.toList()));

			CsvExporter.WriteToFile("owners.csv", this.datamodel.getOwners().stream().collect(Collectors.toList()));

			CsvExporter.WriteToFile("calendars.csv", this.datamodel.getCalendars().stream().collect(Collectors.toList()));

			CsvExporter.WriteToFile("dependency.csv", this.datamodel.getDependencies().stream().collect(Collectors.toList()));

			cmdPrompt.printOutputLine("Save datamodel to csv - Completed");

			inProgress = false;

		} catch (Exception e) {
			log.error(e);
			cmdPrompt.printErrorLine("StackTrace: {}", Arrays.toString(e.getStackTrace()));
			cmdPrompt.printOutputLine("Flow will stop due to Exception.");
		} finally {
			// stop flow
			stopExecution();
		}
	}

	// FIXME: Need to laod csv for importing.
	private void loadCsvToDataModel() throws Exception {
		if (inProgress) {
			throw new ImportInProgressException();
		}

		inProgress = true;
		cmdPrompt.printOutputLine("Start import flow");
		try {
			Map<String, String> configuration = this.getConfiguration();

			// TODO repeat for all data types and assemble model
			List<CsvOSJob> jobs = AbstractCsvImporter.importFromFile(importProvider, configuration.get("import.jobs.path"), CsvOSJob.class);

			// TODO assemble model from parsed data parts

			cmdPrompt.printOutputLine("Data imported successfully");

			// TODO next steps?

			inProgress = false;
		} catch (Exception e) {
			log.error(e);
			cmdPrompt.printErrorLine("StackTrace: {}", Arrays.toString(e.getStackTrace()));
			cmdPrompt.printOutputLine("Flow will stop due to Exception.");
		} finally {
			// stop flow
			stopExecution();
		}
	}

	private boolean passwordPassed() {
		boolean yn = Boolean.valueOf(cfgProvider.getConfigurations().getOrDefault("password.protected", "true"));

		if (yn) {
			// We want to validate with a password

			String pwd = getPassword("Protected Function, enter Password:");

			if ("tidal97".equals(pwd)) {
				return true;
			} else {
				return false;
			}
		}

		return true;
	}

	private String getPassword(String prompt) {

		String password = "";
		ConsoleEraser consoleEraser = new ConsoleEraser();
		System.out.print(prompt);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		consoleEraser.start();
		try {
			password = in.readLine();
		} catch (IOException e) {
			cmdPrompt.printErrorLine("Error trying to read your password!");
			System.exit(1);
		}

		consoleEraser.halt();
		System.out.print("\b");

		return password;
	}

	private static class ConsoleEraser extends Thread {

		private boolean running = true;

		@Override
		public void run() {
			while (running) {
				System.out.print("\b ");
				try {
					Thread.currentThread();
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		public synchronized void halt() {
			running = false;
		}
	}
}
