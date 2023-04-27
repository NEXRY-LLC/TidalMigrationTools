package com.bluehouseinc.dataconverter.parsers.esp.model.schedule;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.actions.SchAction;

import lombok.Data;

@Data
public class SchEventElement {

	List<String> rawDataLines = new ArrayList<>();
	
	List<SchAction> actions;
	List<SchComment> comments;
	SchCalendar calendar;
	SchInvoke invoke;
	SchDString dstring;

	String owner;
	String id;
	String system;
	String user;

	boolean scheduleDataOnly = true; // Right now this is all we can process. 
	
	//EVENT ID(AI01.AIPED150)  USER(UXESPJC)  OWNER(SWESPJC)  SYSTEM(ESPM) -REPLACE
	public SchEventElement(String id, String user, String owner, String system){
		this.actions = new LinkedList<>();
		this.comments = new LinkedList<>();
		this.id = id;
		this.user = user;
		this.owner = owner;
		this.system = system;

	}


}
