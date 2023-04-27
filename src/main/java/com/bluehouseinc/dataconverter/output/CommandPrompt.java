package com.bluehouseinc.dataconverter.output;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CommandPrompt {

	public void printEmptyLine() {
		printOutputLine("");
	}

	public void printOutputLine(String output, Object... args) {
		log.info(output, args);
	}

	public void printErrorLine(String output, Object... args) {
		log.error("Error: " + output + System.lineSeparator(), args);
	}

	private Consumer<String> onUserCommandEntered;

	public void setOnUserCommandEntered(Consumer<String> onUserCommandEntered) {
		this.onUserCommandEntered = onUserCommandEntered;
	}

	private void waitForUserToEnterCommandInternal(String message, Consumer<String> onUserCommandEnteredCallback) {

		try {
			Scanner scanner = new Scanner(System.in);

			printEmptyLine();
			printOutputLine(message);

			String line = scanner.nextLine();
			onUserCommandEnteredCallback.accept(line);

		} catch (IllegalStateException | NoSuchElementException e) {
			// System.in has been closed
			log.debug("System.in was closed; exiting");
		}
	}

	public void waitForUserToEnterCommand(String message) {
		waitForUserToEnterCommandInternal(message, this.onUserCommandEntered);
	}

	public void waitForUserToEnterCommand(String message, Consumer<String> onUserCommandEnteredCallback) {
		waitForUserToEnterCommandInternal(message, onUserCommandEnteredCallback);
	}
}
