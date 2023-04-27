package com.bluehouseinc.dataconverter.api.reporters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CalendarCountValidator extends TidalReporter {
	Map<String, List<BaseCsvJobObject>> cnt = new HashMap<>();

	@Override
	public void doReport(TidalDataModel model) {

		log.trace("Total Calendars To Process [" + model.getCalendars().size() + "]\n");

		log.trace(" Total Calendars Processed [" + model.getCalendars().size() + "] ");

		model.getCalendars().stream().forEach(s -> log.trace(s.getCalendarName()));

		log.trace(" Total Calendars Processed [" + model.getCalendars().size() + "] ");

		log.trace(" Total Jobs Per Calendar [" + model.getCalendars().size() + "] ");

		model.getModelJobs().forEach(f -> doCount(f));

		for (String key : cnt.keySet()) {
			int counter = cnt.get(key).size();
			log.trace("Calendar[" + key + "] Total Jobs[" + counter + "]");

		}

		log.trace(" Total Jobs Per Calendar [" + model.getCalendars().size() + "] ");

	}

	// /**
	// * Recurse all data.
	// * @param data
	// * @param cur
	// * @return
	// */
	// private int getTotalJobsGroups(List<AbstractJobOrGroup> data, List<Calendar> cals) {
	//
	// for (AbstractJobOrGroup d : data) {
	// if (d instanceof JobGroup) {
	// cur++;
	// getTotalJobsGroups(((JobGroup) d).getChildren(), cur);
	// } else {
	// cur++;
	// }
	// }
	//
	// return cur;
	// }

	private void doCount(BaseCsvJobObject obj) {

		String key = null;

		if (obj.getCalendar() != null) {
			key = obj.getCalendar().getCalendarName();
		} else {
			key = "ADHOC";
		}

		if (cnt.containsKey(key)) {

			cnt.get(key).add(obj);
		} else {
			List<BaseCsvJobObject> l = new ArrayList<>();
			l.add(obj);
			cnt.put(key, l);
		}

		if (!obj.getChildren().isEmpty()) {
			obj.getChildren().forEach(f -> doCount((BaseCsvJobObject) f));
		}
	}
}
