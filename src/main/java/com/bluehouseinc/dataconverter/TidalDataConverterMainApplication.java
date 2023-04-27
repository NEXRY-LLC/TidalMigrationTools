package com.bluehouseinc.dataconverter;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.AppConfig;
import com.bluehouseinc.dataconverter.common.Commands;
import com.bluehouseinc.dataconverter.mainimport.MainImport;
import com.bluehouseinc.dataconverter.output.CommandPrompt;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@SpringBootApplication
@ComponentScan({ "com.bluehouseinc" })
public class TidalDataConverterMainApplication implements CommandLineRunner {

	static AppConfig appConfig;
	static CommandPrompt cmdPrompt;
	static MainImport mainImport;

	@Autowired
	void setCmdPrompt(CommandPrompt cmdPromptBean) {
		cmdPrompt = cmdPromptBean;
	}

	@Autowired
	void setAppConfig(AppConfig appConfigBean) {
		appConfig = appConfigBean;
	}

	@Autowired
	void setMainImport(MainImport mainImportBean) {
		mainImport = mainImportBean;
	}

	public static void main(String[] args) {

		// String password = getPassword("Password: ");

		// if ("BlueH0use".equals(password)) {
		SpringApplication.run(TidalDataConverterMainApplication.class, args);
		// } else {
		cmdPrompt.printErrorLine("Invalid password");
		// }

	}

	@Override
	public void run(String... args) throws Exception {
		log.debug("EXECUTING : command line runner");

		cmdPrompt.setOnUserCommandEntered(TidalDataConverterMainApplication::onUserCommandEntered);

		cmdPrompt.printEmptyLine();
		printHelp();
		try {
			// perform stop execution if app is closed
			Runtime.getRuntime().addShutdownHook(new Thread(() -> log.info("Performing shutdown")));

			while (true) {
				cmdPrompt.waitForUserToEnterCommand("Enter a command...");
			}

		} catch (IllegalStateException | NoSuchElementException e) {
			// System.in has been closed
			cmdPrompt.printErrorLine("System.in was closed; exiting");
		}
	}

	/**
	 * Handle user commands
	 *
	 * @param command
	 */
	private static void onUserCommandEntered(String command) {
		try {
			Commands cmd = Commands.valueOf(command.toUpperCase());
			log.debug("Entered command: " + cmd);

			// General app commands
			switch (cmd) {
			case QUIT:
				exit();
				break;

			case CONFIG:
				printConfiguration();
				break;

			case HELP:
				printHelp();
				break;

			default:
				// TODO forward command to execution process
				mainImport.onCommandReceived(cmd);
			}
		} catch (Exception ex) {
			// special command check: question mark
			if ("?".equals(command)) {
				printHelp();
			} else {
				log.error("Exception encountered Invalid Command: {}", command);
			}
		}
	}

	/** Stops the application */
	private static void exit() {
		// TODO stop execution process
		System.exit(0);
	}

	/** Print help on screen */
	private static void printHelp() {
		cmdPrompt.printOutputLine("Commands are (case insensitive)!:");
		cmdPrompt.printEmptyLine();
		for (Commands command : Commands.helpCommands) {
			cmdPrompt.printOutputLine(command.toString());
		}
	}

	/** Print configuration on screen */
	private static void printConfiguration() {
		Map<String, String> properties = mainImport.getConfiguration();
		if (properties != null) {
			Map<String, String> treeMap = new TreeMap<>(properties);
			cmdPrompt.printEmptyLine();
			treeMap.forEach((k, v) -> cmdPrompt.printOutputLine("{}: {}", k, v));
		} else {

			cmdPrompt.printErrorLine("Error reading configuration file: {}", appConfig.CONFIGURATION_FILE);
		}
	}

}
