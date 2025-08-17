package com.bluehouseinc.dataconverter.parsers.bmc.util;

import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.parsers.bmc.model.CalendarTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;

public abstract class DateUtil {

	public static final String NODATES = "NOTDEFINED";

	/**
	 * Returns a Type if know as a string or the data from the weekdays method of
	 * the job
	 *
	 * @param job
	 * @return
	 */
	public static String getMonthDaysFromString(BaseBMCJobOrFolder job) {

		if (job.getName().equals("scepm004_DEM")) {
			// System.out.println();
		}
		
		String calname = "";
		
		String confcal = job.getJobData().getCONFCAL();
		
		if(confcal != null) {
			String confcalcalendar = confcal;
			String val = "CONFCAL[" + confcalcalendar + "]";

			calname = calname.isEmpty() ? val : calname + val;
			
			return calname;
		}

		String weekdays = job.getJobData().getWEEKDAYS();

		if (weekdays != null) {
			String compiledweekdays = buildWeekdayString(weekdays);
			String val = "WKD[" + compiledweekdays + "]";

			calname = calname.isEmpty() ? val : calname + val;
		}

		String daycalc = job.getJobData().getDAYSCAL();

		if (daycalc != null) {
			String val = "DCL[" + daycalc + "]";
			calname = calname.isEmpty() ? val : calname + val;
		}

		String weekscalc = job.getJobData().getWEEKSCAL();

		if (weekscalc != null) {
			String val = "WCL[" + weekscalc + "]";
			calname = calname.isEmpty() ? val : calname + val;
		}

		String days = job.getJobData().getDAYS(); // if this has data then this list the static days something can run.

		if (days != null) {
			String val = "DYS[" + days + "]";
			calname = calname.isEmpty() ? val : calname + val;
		}

		if (weekdays == null && daycalc == null && weekscalc == null && days == null) {
			return null;
		}

		String inuse = getMonthNamesInUse(job);

		if ("JAN-FEB-MAR-APR-MAY-JUN-JUL-AUG-SEP-OCT-NOV-DEC".equals(inuse)) {
			// Do I care?
		} else if (!inuse.isEmpty()) {
			String val = "MIU[" + inuse + "]";
			calname = calname.isEmpty() ? val : calname + val;
		}

		if (!job.getRuleBasedCalendar().isEmpty()) {
			calname = job.getRuleBasedCalendar().stream().map(m -> m.getNAME()).collect(Collectors.joining("-"));
		}

		if (calname.isEmpty()) {
			return null;
		}

		return calname;

	}

	private static String buildWeekdayString(String weekdays) {
		String[] days = weekdays.split(",");
		String results = "";

		for (String day : days) {

			try {

				CalendarTypes type = getTypeFromWeekdays(day);
				// int v = Integer.parseInt(days[i]); // Just a test.

				if (type != null) {
					results += type.toString() + ",";
					// System.out.println(results);
				}
			} catch (NumberFormatException e) {
				// e.printStackTrace();
			}
		}

		if (results.equals("")) {
			results = weekdays;
		}
		if (results.endsWith(",")) {
			return results.substring(0, results.length() - 1);
		}
		return results;
	}

	private static CalendarTypes getTypeFromWeekdays(String day) {
		if (day == null) {
			return CalendarTypes.UNDEFINED;
		}

		for (CalendarTypes type : CalendarTypes.values()) {
			if (type.getWeekDays().equals(day)) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Returns a comma delim list of months used by BMC Logic is if the month is null or zero it's not used.
	 *
	 * @param jw
	 * @return Command delim list of months or empty string if non are used.
	 */
	static String getMonthNamesInUse(BaseBMCJobOrFolder jd) {
		String months = "";

		if (isMonthUsed(jd.getJobData().getJAN())) {
			months += "JAN-";
		}

		if (isMonthUsed(jd.getJobData().getFEB())) {
			months += "FEB-";
		}

		if (isMonthUsed(jd.getJobData().getMAR())) {
			months += "MAR-";
		}

		if (isMonthUsed(jd.getJobData().getAPR())) {
			months += "APR-";
		}

		if (isMonthUsed(jd.getJobData().getMAY())) {
			months += "MAY-";
		}

		if (isMonthUsed(jd.getJobData().getJUN())) {
			months += "JUN-";
		}

		if (isMonthUsed(jd.getJobData().getJUL())) {
			months += "JUL-";
		}

		if (isMonthUsed(jd.getJobData().getAUG())) {
			months += "AUG-";
		}

		if (isMonthUsed(jd.getJobData().getSEP())) {
			months += "SEP-";
		}

		if (isMonthUsed(jd.getJobData().getOCT())) {
			months += "OCT-";
		}

		if (isMonthUsed(jd.getJobData().getNOV())) {
			months += "NOV-";
		}

		if (isMonthUsed(jd.getJobData().getDEC())) {
			months += "DEC-";
		}

		if (months.endsWith("-")) {
			months = months.substring(0, months.length() - 1);
		}

		return months;
	}

	private static boolean isMonthUsed(String month) {
		if (month == null) {
			return false;
		}

		if (!month.isEmpty()) { // Not empty and
			if (month.equals("1")) { // not a zero, zero is not used.
				return true;
			}
		}
		return false;
	}

}
