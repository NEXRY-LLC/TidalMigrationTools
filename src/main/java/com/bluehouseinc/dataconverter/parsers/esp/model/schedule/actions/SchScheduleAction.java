package com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class SchScheduleAction extends SchAction {

	private boolean isAtTime = false;
	private String time;
	private String calendarData;
	
	public SchScheduleAction(String data) {
		super(data);
		init();
	}

	
	private void  init() {
		String data = this.data;
		
		//SCHEDULE AT 21.00 ON LAST WORKDAY OF EACH MONTH STARTING FRI 31ST MAR 2023
		if(data.startsWith("AT")) {
			isAtTime = true;
			data = data.replaceFirst("AT ", "");
		
			int idx = data.indexOf("STARTING");
			String removeme = data.substring( idx, data.length());
			data = data.replace(removeme, "");
			
			
			String[] elements = data.split(" ",2); // Just get time out of this and use the rest for calendar info
			
			time = elements[0]; // First one. 
			
			calendarData = elements[1]; // Rest of it. 
		}
	}
}
