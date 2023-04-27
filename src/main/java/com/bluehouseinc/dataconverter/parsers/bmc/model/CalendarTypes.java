package com.bluehouseinc.dataconverter.parsers.bmc.model;

/**
 * Currently this class is just for descriptive names for the simpler calendars
 * of BMC. Ugly right now as I am not sure what to do with these values yet in
 * terms of mapping to a TIDAL calendar
 *
 * @author Brian Hayes
 *
 */

public enum CalendarTypes {

	UNDEFINED {
		@Override
		public String getWeekDays() {
			return "null";
		}
	},
	ALL {
		@Override
		public String getWeekDays() {
			return "ALL";
		}
	},
	SUN {
		@Override
		public String getWeekDays() {
			return "0";
		}
	},
	MON {
		@Override
		public String getWeekDays() {
			return "1";
		}
	},
	TUE {
		@Override
		public String getWeekDays() {
			return "2";
		}
	},
	WED {
		@Override
		public String getWeekDays() {
			return "3";
		}
	},
	THU {
		@Override
		public String getWeekDays() {
			return "4";
		}
	},
	FRI {
		@Override
		public String getWeekDays() {
			return "5";
		}
	},
	SAT {
		@Override
		public String getWeekDays() {
			return "6";
		}
	},
	FIRST_SUNDAY_SECOND_WEEK {
		@Override
		public String getWeekDays() {
			return "D0W2";
		}
	},
	FIRST_SUNDAY_FIRST_WEEK {
		@Override
		public String getWeekDays() {
			return "D0W1";
		}
	},
	FIRST_FRIDAY_FIRST_WEEK {
		@Override
		public String getWeekDays() {
			return "D5W1";
		}
	},
	FIRST_MONDAY_FIRST_WEEK {
		@Override
		public String getWeekDays() {
			return "D1W1";
		}
	},
	FIRST_TUESDAY_FIRST_WEEK {
		@Override
		public String getWeekDays() {
			return "D2W1";
		}
	},
	FIRST_SATURDY_FIRST_WEEK {
		@Override
		public String getWeekDays() {
			return "D6W1";
		}
	},
	FIRST_SATURDY_SECOND_WEEK {
		@Override
		public String getWeekDays() {
			return "D6W2";
		}
	},
	FIRST_SATURDY_THIRD_WEEK {
		@Override
		public String getWeekDays() {
			return "D6W3";
		}
	},
	FIRST_SATURDY_FOURTH_WEEK {
		@Override
		public String getWeekDays() {
			return "D6W4";
		}
	},
	FIRST_SATURDY_FITH_WEEK {
		@Override
		public String getWeekDays() {
			return "D6W5";
		}
	},
	AFTER_THE_FIRST {
		@Override
		public String getWeekDays() {
			return ">1";
		}
	};

	public abstract String getWeekDays();

}
