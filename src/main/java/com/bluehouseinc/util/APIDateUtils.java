package com.bluehouseinc.util;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
			
			int timeone =  Integer.valueOf(intone);
			int timetwo = timeone+timeone; // I know, I know.. 
			
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

			if(durations == null) {
				return;
			}
			
			if (durations.size() == 1) {
				// This works :)
				long minutes = durations.get(0).toMinutes();
				// wth - keep getting a negative number here.
				String negcheck = Long.toString(minutes);
				negcheck = negcheck.replace("-", "");

				obj.setStartTime(cleaned.get(0)); // Set our start time
				obj.getRerunLogic().setRepeatEvery(Integer.valueOf(negcheck));
				//obj.getRerunLogic().setRepeatMaxTimes(startList.size());

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

}
