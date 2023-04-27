package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;

public class OPCCalendar extends CsvCalendar implements Serializable {

	List<String> starttimes = new ArrayList<>();
	String originalName;

	public OPCCalendar(String name) {
		this.originalName = name;
		sanatize(name);
	}

	String getOriginalName() {
		return this.originalName;
	}

	public List<String> getStarttimes() {
		return starttimes;
	}

	private void sanatize(String val) {
		this.setCalendarNotes(val);

		if (val.length() > 64) {
			val = val.replaceAll("MONDAY TUESDAY WEDNESDAY THURSDAY FIRDAY SATURDAY SUNDAY", "DAILY");

			val = val.replaceAll("MONDAY", "M");
			val = val.replaceAll("TUESDAY", "T");
			val = val.replaceAll("WEDNESDAY", "W");
			val = val.replaceAll("THURSDAY", "T");
			val = val.replaceAll("FRIDAY", "F");
			val = val.replaceAll("SATURDAY", "S");
			val = val.replaceAll("SUNDAY", "S");

			if (val.length() > 64) { // If we are still do long uggg.
				val = val.replaceAll("JANURARY", "JAN");
				val = val.replaceAll("FEBURARY", "FEB");
				val = val.replaceAll("MARCH", "MAR");
				val = val.replaceAll("APRIL", "APR");
				val = val.replaceAll("MAY", "MAY");
				val = val.replaceAll("JUNE", "JUN");
				val = val.replaceAll("JULY", "JUL");

				val = val.replaceAll("AUGUST", "AUG");
				val = val.replaceAll("SEPTEMBER", "SEP");
				val = val.replaceAll("OCTOBER", "OCT");
				val = val.replaceAll("NOVEMBER", "NOV");
				val = val.replaceAll("DECEMBER", "DEC");

			}

			if (val.length() > 64) { // If we are still do long uggg.
				val = val.replaceAll(" ", "");
			}

			if (val.length() > 64) { // If we are still do long uggg.
				val = val.substring(0, 64); // Brutt force cut it off
			}
		}

		if (val.isEmpty()) {
			val = "UNDEFINED";
		}

		this.setCalendarName(val);
	}

}
