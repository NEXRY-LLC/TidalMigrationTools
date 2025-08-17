package com.bluehouseinc.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.tidal.api.model.job.RepeatType;
import com.bluehouseinc.tidal.utils.DateParser;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class APIDateUtils {

	/**
	 *
	 * @param list  our delemen'd list of times in military format that we need to convert to a HH:mm format for TIDAL
	 * @param delem
	 * @return
	 */
	public static String convertToTidalMiltary(String list, Character delem) {

		List<String> millhack = Arrays.asList(list.split("\\s*" + delem + "\\s*"));// .stream().sorted().collect(Collectors.toList());;

		List<String> millhackfix = new ArrayList<>();

		millhack.forEach(s -> {
			millhackfix.add(s.replaceAll("..(?!$)", "$0:")); // the TIDAL way
		});

		return String.join(delem.toString(), millhackfix);
	}

	/**
	 * Takes a list of military times and provide the unique durations. E.G every 30
	 * min or every hour, etc..
	 *
	 * @param rules - Strings of military times to add.
	 * @return Either the single time to rerun by or the list of times to run or one
	 *         time to run.
	 */
	public static List<Duration> getEveryNTimes(List<String> rules) {

		List<java.util.Calendar> calc = new ArrayList<>();
		List<String> durations = new ArrayList<>();
		List<Duration> dur = new ArrayList<>();

		if (rules.size() == 1) {
			// So lets just take our single time and add to it.. AKA 30 , 60 so our Duration logic will flow.
			String ruleone = rules.get(0);

			String intone = ruleone.replaceFirst("^0+(?!$)", "");

			int timeone = Integer.valueOf(intone);
			int timetwo = timeone + timeone; // I know, I know..

			rules.clear();

			String mileone = String.format("%1$" + 4 + "s", Integer.valueOf(timeone)).replace(' ', '0');
			String miletwo = String.format("%1$" + 4 + "s", Integer.valueOf(timetwo)).replace(' ', '0');
			rules.add(mileone);
			rules.add(miletwo);

		}

		for (String starttime : rules) {
			String milstart = starttime.replace(":", "");

			milstart = String.format("%1$" + 4 + "s", milstart).replace(' ', '0');

			java.util.Calendar start = DateParser.parseMilitaryTime(milstart);

			// After the first one is loaded, let's get our durations.
			if (!calc.isEmpty()) {
				java.util.Calendar last = calc.get(calc.size() - 1);
				Duration timeElapsed = null;
				if (last.toInstant().isBefore(start.toInstant())) {
					// System.out.println(Duration.between(start.toInstant(), last.toInstant()));
					timeElapsed = Duration.between(start.toInstant(), last.toInstant());
				} else {
					// System.out.println(
					// (Duration.between(last.toInstant(), start.toInstant()).minus(Duration.ofHours(24))));
					timeElapsed = Duration.between(last.toInstant(), start.toInstant());
				}

				String freq = timeElapsed.toString().replaceAll("-", "");
				if (!durations.contains(freq)) {
					durations.add(freq);
					dur.add(timeElapsed);// Real data.
				}
			}

			calc.add(start);

		}

		return dur;
	}

	// * used for internal repeat logic handling.
	private enum RepeatInternalType {
		SAME, NEW
	}

	public static void setRerunSameStartTimes(String listoftime, BaseCsvJobObject obj, TidalDataModel datamodel, boolean convertrerun) {
		setStartTimes(listoftime, obj, datamodel, convertrerun, RepeatInternalType.SAME);
	}

	public static void setRerunNewStartTimes(String listoftime, BaseCsvJobObject obj, TidalDataModel datamodel, boolean convertrerun) {
		setStartTimes(listoftime, obj, datamodel, convertrerun, RepeatInternalType.NEW);
	}

	/**
	 * Sets up a job based on the input of start times in the proper way for TIDAL
	 *
	 * @param listoftime   - List of times in military format
	 *                     (1000,1010,1020,1300,1445)
	 * @param obj          The BaseCsvJobObject to setup correctly based on the input
	 *                     times provided.
	 * @param convertrerun - To convert from a list of times that are for example
	 *                     every 30 min, then setup rerrun every N times for X max vs the list of times.
	 */
	private static void setStartTimes(String listoftime, BaseCsvJobObject obj, TidalDataModel datamodel, boolean convertrerun, RepeatInternalType repeattype) {

		if (StringUtils.isBlank(listoftime)) {
			return;
		} else {
			// Get our list of times sorted in numeric order.
			List<String> sortedtime = Arrays.asList(listoftime.split("\\s*,\\s*")).stream().sorted().collect(Collectors.toList());

			if (sortedtime.size() == 1) {
				// Only one time so use it as a start time for the job
				obj.setStartTime(sortedtime.get(0).replace(":", ""));
			} else {
				String starttimes = String.join(",", sortedtime);

				if (convertrerun) {
					List<Duration> durrations = APIDateUtils.getEveryNTimes(sortedtime);
					if (durrations.size() == 1) {
						long minutes = durrations.get(0).toMinutes();
						// wth - keep getting a negative number here.
						String negcheck = Long.toString(minutes);
						negcheck = negcheck.replace("-", "");
						obj.getRerunLogic().setRepeatEvery(Integer.valueOf(negcheck));
						obj.getRerunLogic().setRepeatMaxTimes(sortedtime.size());
						// Only the one :) and remove the colon so its back to the time require for start time
						obj.setStartTime(sortedtime.get(0).replace(":", ""));

						if (repeattype == RepeatInternalType.SAME) {
							obj.getRerunLogic().setRepeatType(RepeatType.SAME);
						} else {
							obj.getRerunLogic().setRepeatType(RepeatType.NEW);
						}
					} else {

						if (starttimes.length() > 256) {
							log.error("###################################TO MANY CUSTOM START TIMES [" + obj.getFullPath() + "]");

							datamodel.addCalendarToJobOrGroup(obj, new CsvCalendar("TO_MANY_RERUN_OPTIONS"));
							obj.setNotes(starttimes);
						} else {
							if (repeattype == RepeatInternalType.SAME) {
								obj.getRerunLogic().setRepeatType(RepeatType.SAMESTARTTIMES); // Same By Default
							} else {
								obj.getRerunLogic().setRepeatType(RepeatType.STARTTIMES); // Same By Default
							}
							obj.getRerunLogic().setRerunData(starttimes);
						}
					}
				} else {
					if (starttimes.length() > 256) {
						log.error("###################################TO MANY CUSTOM START TIMES [" + obj.getFullPath() + "]");

						datamodel.addCalendarToJobOrGroup(obj, new CsvCalendar("TO_MANY_RERUN_OPTIONS"));
						obj.setNotes(starttimes);
					} else {
						if (repeattype == RepeatInternalType.SAME) {
							obj.getRerunLogic().setRepeatType(RepeatType.SAMESTARTTIMES); // Same By Default
						} else {
							obj.getRerunLogic().setRepeatType(RepeatType.STARTTIMES); // Same By Default
						}
						obj.getRerunLogic().setRerunData(starttimes);
					}
				}
			}

		}
	}

	public static void setRerunSameStartMinutes(String listoftime, BaseCsvJobObject obj, TidalDataModel datamodel, boolean convertrerun) {
		setStartMinutes(listoftime, obj, datamodel, convertrerun, RepeatInternalType.SAME);
	}

	public static void setRerunNewStartMinutes(String listoftime, BaseCsvJobObject obj, TidalDataModel datamodel, boolean convertrerun) {
		setStartMinutes(listoftime, obj, datamodel, convertrerun, RepeatInternalType.NEW);
	}

	/**
	 *
	 * @param listofminutes - E.G 00,05,10,15,20,25,30,35,40,45,50,55
	 * @param obj           - our BaseCsvJobObject to apply logic too
	 * @param datamodel     - THe TidalDataModel we are working with and used if needed to apply information into
	 * @param convertrerun  - Convert from listofminutes to rerun every N minutes, up to N time with a start time on the job
	 * @param repeattype    - Same or new occurrences
	 */
	private static void setStartMinutes(String listofminutes, BaseCsvJobObject obj, TidalDataModel datamodel, boolean convertrerun, RepeatInternalType repeattype) {

		if (StringUtils.isBlank(listofminutes)) {
			return;
		}

		if (convertrerun) {

			List<String> startList = Arrays.asList(listofminutes.split(","));
			List<String> cleaned = new ArrayList<>();

			startList.forEach(f -> {
				f = f.replace(":", "");
				f = String.format("%1$" + 4 + "s", f).replace(' ', '0'); // pad to 4
				cleaned.add(f);
			});

			List<Duration> durations = getEveryNTimes(cleaned);

			if (durations == null) {
				return;
			}

			if (durations.size() == 1) {
				// This works :)
				long minutes = durations.get(0).toMinutes();
				// wth - keep getting a negative number here.
				String negcheck = Long.toString(minutes);
				negcheck = negcheck.replace("-", "");

				// obj.setStartTime(cleaned.get(0)); // Set our start time
				obj.getRerunLogic().setRepeatEvery(Integer.valueOf(negcheck));
				// obj.getRerunLogic().setRepeatMaxTimes(startList.size());

				if (repeattype == RepeatInternalType.SAME) {
					obj.getRerunLogic().setRepeatType(RepeatType.SAME);
				} else {
					obj.getRerunLogic().setRepeatType(RepeatType.NEW);
				}

			} else {
				if (repeattype == RepeatInternalType.SAME) {
					obj.getRerunLogic().setRepeatType(RepeatType.SAMESTARTMINUTES); // Same By Default
				} else {
					obj.getRerunLogic().setRepeatType(RepeatType.STARTMINUTES); // New
				}

				obj.getRerunLogic().setRerunData(listofminutes);
			}

		} else {

			if (repeattype == RepeatInternalType.SAME) {
				obj.getRerunLogic().setRepeatType(RepeatType.SAMESTARTMINUTES); // Same By Default
			} else {
				obj.getRerunLogic().setRepeatType(RepeatType.STARTMINUTES); // New
			}

			obj.getRerunLogic().setRerunData(listofminutes);
		}
	}

	public static void setRerunNewOccurance(Integer repeatevery, Integer maxreruns, BaseCsvJobObject obj, TidalDataModel datamodel) {

		if (repeatevery == 0) {
			repeatevery = 60;
		}
		RepeatType which = RepeatType.SAME;
		if (repeatevery >= 30) {
			which = RepeatType.NEW;
		}

		obj.getRerunLogic().setRepeatEvery(repeatevery);
		obj.getRerunLogic().setRepeatType(which);

		if (maxreruns != null) {
			obj.getRerunLogic().setRepeatMaxTimes(maxreruns);
		}
	}

	// Common date formats used in enterprise systems
	private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(DateTimeFormatter.ofPattern("yyyy-MM-dd"), // ISO format: 2024-01-15
			DateTimeFormatter.ofPattern("MM/dd/yyyy"), // US format: 01/15/2024
			DateTimeFormatter.ofPattern("dd/MM/yyyy"), // European format: 15/01/2024
			DateTimeFormatter.ofPattern("yyyy/MM/dd"), // Alternative ISO: 2024/01/15
			DateTimeFormatter.ofPattern("MMM dd, yyyy"), // Jan 15, 2024
			DateTimeFormatter.ofPattern("dd-MMM-yyyy"), // 15-Jan-2024
			DateTimeFormatter.ofPattern("yyyyMMdd"), // Compact: 20240115
			DateTimeFormatter.ofPattern("MM-dd-yyyy"), // Dash US: 01-15-2024
			DateTimeFormatter.ofPattern("dd-MM-yyyy") // Dash European: 15-01-2024
	);

	private static final List<DateTimeFormatter> DATETIME_FORMATTERS = Arrays.asList(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), // ISO datetime
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"), // ISO datetime short
			DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"), // US datetime
			DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"), // European datetime
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"), // ISO T format
			DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss") // Readable datetime
	);

	// Define all possible time formats in order of preference
	private static final List<DateTimeFormatter> TIME_FORMATTERS = Arrays.asList(DateTimeFormatter.ofPattern("h:mm a"), // 1:00 PM, 12:00 AM
			DateTimeFormatter.ofPattern("h:m a"), // 1:0 PM
			DateTimeFormatter.ofPattern("hh:mm a"), // 01:00 PM
			DateTimeFormatter.ofPattern("H:mm"), // 13:00, 1:00
			DateTimeFormatter.ofPattern("HH:mm"), // 13:00, 01:00
			DateTimeFormatter.ofPattern("H:m"), // 13:0, 1:0
			DateTimeFormatter.ofPattern("h a"), // 1 PM
			DateTimeFormatter.ofPattern("ha"), // 1PM
			DateTimeFormatter.ofPattern("H") // 13, 1
	);

	private static final Pattern NUMERIC_ONLY = Pattern.compile("^\\d+$");

	/**
	 * Checks if the given date string represents a date in the past.
	 * 
	 * @param dateString The date string to check
	 * @return true if the date is in the past, false if it's today or in the future
	 * @throws IllegalArgumentException if the date string cannot be parsed
	 */
	public static boolean isInPast(String dateString) {
		if (dateString == null || dateString.trim().isEmpty()) {
			throw new IllegalArgumentException("Date string cannot be null or empty");
		}

		String trimmedDate = dateString.trim();

		// Try to parse as LocalDateTime first (includes time component)
		LocalDateTime parsedDateTime = tryParseDateTime(trimmedDate);
		if (parsedDateTime != null) {
			return parsedDateTime.isBefore(LocalDateTime.now());
		}

		// Try to parse as LocalDate (date only)
		LocalDate parsedDate = tryParseDate(trimmedDate);
		if (parsedDate != null) {
			return parsedDate.isBefore(LocalDate.now());
		}

		throw new IllegalArgumentException(String.format("Unable to parse date string: '%s'. Supported formats include: " + "yyyy-MM-dd, MM/dd/yyyy, dd/MM/yyyy, MMM dd yyyy, yyyyMMdd, etc.", dateString));
	}

	/**
	 * Checks if the given date string represents a date in the past, with custom error handling.
	 * 
	 * @param dateString The date string to check
	 * @return DateCheckResult containing the result and any error information
	 */
	public static DateCheckResult checkIfPast(String dateString) {
		try {
			boolean isPast = isInPast(dateString);
			return new DateCheckResult(isPast, true, null);
		} catch (Exception e) {
			return new DateCheckResult(false, false, e.getMessage());
		}
	}

	/**
	 * Checks if the first date string is in the past relative to the second date string.
	 * Useful for comparing contract dates, milestone validations, etc.
	 * 
	 * @param dateToCheck   The date string to check (e.g., contract start date)
	 * @param referenceDate The reference date string (e.g., contract end date)
	 * @return true if dateToCheck is before referenceDate, false otherwise
	 * @throws IllegalArgumentException if either date string cannot be parsed
	 */
	public static boolean isInPast(String dateToCheck, String referenceDate) {
		if (dateToCheck == null || dateToCheck.trim().isEmpty()) {
			throw new IllegalArgumentException("Date to check cannot be null or empty");
		}
		if (referenceDate == null || referenceDate.trim().isEmpty()) {
			throw new IllegalArgumentException("Reference date cannot be null or empty");
		}

		String trimmedDateToCheck = dateToCheck.trim();
		String trimmedReferenceDate = referenceDate.trim();

		// Try to parse both as LocalDateTime first
		LocalDateTime parsedDateTimeToCheck = tryParseDateTime(trimmedDateToCheck);
		LocalDateTime parsedDateTimeReference = tryParseDateTime(trimmedReferenceDate);

		if (parsedDateTimeToCheck != null && parsedDateTimeReference != null) {
			return parsedDateTimeToCheck.isBefore(parsedDateTimeReference);
		}

		// Try to parse both as LocalDate
		LocalDate parsedDateToCheck = tryParseDate(trimmedDateToCheck);
		LocalDate parsedDateReference = tryParseDate(trimmedReferenceDate);

		if (parsedDateToCheck != null && parsedDateReference != null) {
			return parsedDateToCheck.isBefore(parsedDateReference);
		}

		// Mixed date/datetime comparison - convert date to datetime at start of day
		if (parsedDateToCheck != null && parsedDateTimeReference != null) {
			return parsedDateToCheck.atStartOfDay().isBefore(parsedDateTimeReference);
		}
		if (parsedDateTimeToCheck != null && parsedDateReference != null) {
			return parsedDateTimeToCheck.isBefore(parsedDateReference.atStartOfDay());
		}

		// Build error message showing which dates failed to parse
		StringBuilder errorMsg = new StringBuilder("Unable to parse date strings: ");
		if (parsedDateToCheck == null && parsedDateTimeToCheck == null) {
			errorMsg.append(String.format("'%s' (first date)", dateToCheck));
		}
		if (parsedDateReference == null && parsedDateTimeReference == null) {
			if (parsedDateToCheck == null && parsedDateTimeToCheck == null) {
				errorMsg.append(" and ");
			}
			errorMsg.append(String.format("'%s' (reference date)", referenceDate));
		}

		throw new IllegalArgumentException(errorMsg.toString());
	}

	/**
	 * Attempts to parse the string as a LocalDate using supported formats.
	 */
	private static LocalDate tryParseDate(String dateString) {
		for (DateTimeFormatter formatter : DATE_FORMATTERS) {
			try {
				return LocalDate.parse(dateString, formatter);
			} catch (DateTimeParseException e) {
				// Continue to next formatter
			}
		}
		return null;
	}

	/**
	 * Attempts to parse the string as a LocalDateTime using supported formats.
	 */
	private static LocalDateTime tryParseDateTime(String dateString) {
		for (DateTimeFormatter formatter : DATETIME_FORMATTERS) {
			try {
				return LocalDateTime.parse(dateString, formatter);
			} catch (DateTimeParseException e) {
				// Continue to next formatter
			}
		}
		return null;
	}

	/**
	 * Result container for date checking operations.
	 */
	public static class DateCheckResult {
		private final boolean isPast;
		private final boolean success;
		private final String errorMessage;

		public DateCheckResult(boolean isPast, boolean success, String errorMessage) {
			this.isPast = isPast;
			this.success = success;
			this.errorMessage = errorMessage;
		}

		public boolean isPast() {
			return isPast;
		}

		public boolean isSuccess() {
			return success;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		@Override
		public String toString() {
			if (success) {
				return String.format("DateCheckResult{isPast=%s}", isPast);
			} else {
				return String.format("DateCheckResult{error='%s'}", errorMessage);
			}
		}
	}

	/**
	 * Utility method to get all supported date format patterns for documentation.
	 */
	public static List<String> getSupportedDateFormats() {
		return Arrays.asList("yyyy-MM-dd (ISO format)", "MM/dd/yyyy (US format)", "dd/MM/yyyy (European format)", "yyyy/MM/dd", "MMM dd, yyyy", "dd-MMM-yyyy", "yyyyMMdd (compact)", "MM-dd-yyyy", "dd-MM-yyyy", "yyyy-MM-dd HH:mm:ss (with time)",
				"yyyy-MM-dd'T'HH:mm:ss (ISO datetime)");
	}

	/**
	 * Calculate minutes between two time strings
	 * Handles various formats: "600", "12:30", "1:00 PM", etc.
	 */
	public static long getMinutesBetween(String startTime, String endTime) {
		LocalTime start = parseTime(startTime);
		LocalTime end = parseTime(endTime);

		long minutes = ChronoUnit.MINUTES.between(start, end);

		// Handle crossing midnight
		return minutes < 0 ? minutes + (24 * 60) : minutes;
	}

	/**
	 * Parse time string to LocalTime object
	 * Automatically handles padding and format detection
	 */
	public static LocalTime parseTime(String timeStr) {
		if (timeStr == null || timeStr.trim().isEmpty()) {
			throw new IllegalArgumentException("Time string cannot be null or empty");
		}

		String processedTime = preprocessTime(timeStr.trim().toUpperCase());

		for (DateTimeFormatter formatter : TIME_FORMATTERS) {
			try {
				return LocalTime.parse(processedTime, formatter);
			} catch (DateTimeParseException e) {
				// Continue to next formatter
			}
		}

		throw new IllegalArgumentException("Unable to parse time: " + timeStr);
	}

	/**
	 * Internal method - handles padding and preprocessing
	 */
	private static String preprocessTime(String timeStr) {
		// If it's purely numeric, apply internal padding logic
		if (NUMERIC_ONLY.matcher(timeStr).matches()) {
			return padNumericTime(timeStr);
		}

		// Return as-is for other formats
		return timeStr;
	}

	/**
	 * Internal padding logic - not exposed to users
	 */
	private static String padNumericTime(String numericTime) {
		int value = Integer.parseInt(numericTime);
		int length = numericTime.length();

		// Validate reasonable time values
		if (value < 0 || value > 2400) {
			throw new IllegalArgumentException("Time value out of range: " + value);
		}

		switch (length) {
		case 1:
		case 2:
			// Hours only
			if (value > 24) {
				throw new IllegalArgumentException("Invalid hour value: " + value);
			}
			return String.format("%02d:00", value);

		case 3:
			// H:MM format
			int hours3 = Integer.parseInt(numericTime.substring(0, 1));
			int minutes3 = Integer.parseInt(numericTime.substring(1));

			if (hours3 > 24 || minutes3 > 59) {
				throw new IllegalArgumentException("Invalid time components: " + hours3 + ":" + minutes3);
			}

			return String.format("%02d:%02d", hours3, minutes3);

		case 4:
			// HH:MM format
			int hours4 = Integer.parseInt(numericTime.substring(0, 2));
			int minutes4 = Integer.parseInt(numericTime.substring(2));

			if (hours4 > 24 || minutes4 > 59) {
				throw new IllegalArgumentException("Invalid time components: " + hours4 + ":" + minutes4);
			}

			return String.format("%02d:%02d", hours4, minutes4);

		default:
			throw new IllegalArgumentException("Invalid numeric time format length: " + length);
		}
	}

	/**
	 * Get absolute difference (always positive)
	 */
	public static long getAbsoluteMinutesBetween(String startTime, String endTime) {
		return Math.abs(getMinutesBetween(startTime, endTime));
	}

	/**
	 * Batch processing for multiple time pairs
	 */
	public static long[] calculateMinutesBatch(String[][] timePairs) {
		long[] results = new long[timePairs.length];

		for (int i = 0; i < timePairs.length; i++) {
			try {
				results[i] = getMinutesBetween(timePairs[i][0], timePairs[i][1]);
			} catch (Exception e) {
				throw new RuntimeException("Failed to process time pair at index " + i + ": [" + timePairs[i][0] + ", " + timePairs[i][1] + "]", e);
			}
		}

		return results;
	}
}
