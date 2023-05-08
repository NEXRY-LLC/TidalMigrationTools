package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchCalendar;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchComment;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchDString;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchEventElement;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchInvoke;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchNoScheduleAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchResumeAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchScheduleAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchSuspendAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchVsAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchWobtriggerAction;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Component
@Log4j2
public class ScheduleEventDataProcessor {
	private final static String EVENT_PATTERN = "EVENT.*?ID\\((.*?)\\).*?USER\\((.*?)\\).*?OWNER\\((.*?)\\).*?SYSTEM\\((.*?)\\).*?";

	Map<String, SchEventElement> elements;
	List<SchEventElement> advancedEventData;

	public ScheduleEventDataProcessor() {

	}

	public void doProcessScheduleData(String datafile) {

		this.elements = new HashMap<>();
		this.advancedEventData = new LinkedList<>();

		BufferedReader reader = null;

		try {
			if (datafile == null) {
				throw new TidalException("Missing esp.schedule.datafile entry");
			}

			reader = new BufferedReader(new FileReader(datafile));

			String line;

			while ((line = EspFileReaderUtils.readLineTrimmed(reader)) != null) {

				line.trim();

				if (EspFileReaderUtils.skippedLine(line)) {
					continue;
				}

				// Fix our newline
				line = EspFileReaderUtils.readLineMerged(reader, line, '-');

				if (isEventPattern(line)) {

					processEvent(reader, line);

				}
			}
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {

			}
		}
	}

	private boolean isEventPattern(String line) {
		return RegexHelper.matchesRegexPattern(line, EVENT_PATTERN);
	}

	private void processEvent(final BufferedReader reader, String eventline) throws IOException {

		// EVENT ID(AI01.AIPED150) USER(UXESPJC) OWNER(SWESPJC) SYSTEM(ESPM) -REPLACE
		String id = RegexHelper.extractNthMatch(eventline, EVENT_PATTERN, 0);
		String user = RegexHelper.extractNthMatch(eventline, EVENT_PATTERN, 1);
		String owner = RegexHelper.extractNthMatch(eventline, EVENT_PATTERN, 2);
		String system = RegexHelper.extractNthMatch(eventline, EVENT_PATTERN, 3);

		SchEventElement event = new SchEventElement(id, user, owner, system);
		
		event.setRawEventLine(eventline);
		
		List<String> lines = EspFileReaderUtils.parseJobLines(reader, "ENDDEF", '-');

		for (String line : lines) {

			String data[] = line.split(" ", 2);
			String element = data[0]; // Who are we?
			String value = data[1].trim(); // What are we?

			switch (element) {
			case "COM":
				event.getComments().add(new SchComment(value));
				break;
			case "CALENDAR":
				event.setCalendar(new SchCalendar(value));
				break;
			case "SCHEDULE":
				SchScheduleAction sched = new SchScheduleAction(value);
				if (!sched.isAtTime()) {
					event.setScheduleDataOnly(false);
				}
				event.getActions().add(sched);
				break;
			case "RESUME":
				event.getActions().add(new SchResumeAction(value));
				event.setScheduleDataOnly(false);
				break;
			case "SUSPEND":
				event.getActions().add(new SchSuspendAction(value));
				event.setScheduleDataOnly(false);
				break;
			case "NOSCHED":
				event.getActions().add(new SchNoScheduleAction(value));
				event.setScheduleDataOnly(false);
				break;
			case "STARTING":
				event.getActions().add(new SchNoScheduleAction(value));
				event.setScheduleDataOnly(false);
				break;
			case "EXPECT":
				event.getActions().add(new SchNoScheduleAction(value));
				event.setScheduleDataOnly(false);
				break;
			case "DSTRIG":
				event.setDstring(new SchDString(value));
				event.setScheduleDataOnly(false);
				break;
			case "WOBTRIG":
				event.getActions().add(new SchWobtriggerAction(value));
				event.setScheduleDataOnly(false);
				break;
			case "INVOKE":
				event.setInvoke(new SchInvoke(value));
				// event.setScheduleDataOnly(false);
				break;
			case "VS":
				event.getActions().add(new SchVsAction(value));
				event.setScheduleDataOnly(false);
				break;
			default:
				throw new TidalException("Unknown Data Element: " + line + "In Event ID" + event.getId());
			}

		}

		event.setRawDataLines(lines); // Set the data we used. Not really needed but just in case

		if (event.getInvoke() != null) { // No invoke statement means its not used or applied to appl id
			String key = event.getInvoke().getFolderName();

			// Adding all elements so we can use them in the future , for example we can set comments on all objects.
			if (!this.elements.containsKey(key)) {
				log.debug("ScheduleEventDataProcessor processEvent[" + event.getId() + "] Registering");
				this.elements.put(key, event);
			}

		}
		// Add our advance data into its own list for reporting.
		if (!event.isScheduleDataOnly()) {
			log.debug("ScheduleEventDataProcessor advancedEventData [" + event.getId() + "] Reporting");
			this.advancedEventData.add(event);
		}
	}

	public SchEventElement getElementByName(String name) {
		return this.elements.get(name);
	}

	/**
	 * Returns every <J> object in the supplied collection , the Node is recursive
	 * returning all objects.
	 *
	 * @param <J>
	 * @param collection
	 * @param type
	 * @return
	 */
	public static <J extends SchAction> List<J> getActionsByType(SchEventElement element, Class<J> clazz) {
		return element.getActions().stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
	}
}
