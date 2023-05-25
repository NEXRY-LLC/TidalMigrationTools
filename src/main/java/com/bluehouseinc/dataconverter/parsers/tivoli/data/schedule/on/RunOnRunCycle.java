package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RunOnRunCycle extends RunOn {

	String calendarData;
	String description;
	String calendarName;
	
}
