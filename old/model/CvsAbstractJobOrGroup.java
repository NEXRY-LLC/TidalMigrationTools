package com.dataconverter.csv.model;

import java.util.concurrent.ThreadLocalRandom;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.CsvRecurse;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;


@Data
@ToString
public abstract class CvsAbstractJobOrGroup {

	@CsvBindByName(column = "id")
	private Integer id;

	@CsvBindByName(column = "name")
	private String name;

	@CsvBindByName(column = "parentId")
	private Integer parentId;

	@CsvRecurse
	private CvsOwner owner;
	
	@Setter(value = AccessLevel.PROTECTED)
	@CsvIgnore()
	CvsAbstractJobOrGroup parent;

	@CsvRecurse
	private CvsCalendar calender;
	
	@CsvBindByName(column = "startTime")
	private String startTime;
	
	@CsvBindByName(column = "endTime")
	private String endTime;

	@CsvBindByName(column = "customStartTimes")
	private String customStartTimes; // 10:00, 09:00, etc.. 

	@CsvBindByName(column = "rerunEveryNMinutes")
	private int rerunEveryNMinutes;
	
	@CsvBindByName(column = "rerunMaxReruns")
	private int rerunMaxReruns;
	
	@CsvBindByName(column = "notes")
	private String notes;

	@CsvRecurse
	private CvsRunTimeUser runtimeuser;
	
	public CvsAbstractJobOrGroup() {
		this.id = ThreadLocalRandom.current().nextInt(); // Auto set ID for reference
	}

	public Integer getId() {
		return id;
	}

	public String getParentPath() {
		String path = this.name;
		CvsAbstractJobOrGroup parent = this.parent;
		
		do {
			if(parent == null) {break;}
			// walk up the chain until we have no more parents. 
			String name = parent.getName(); // We need our name;	
			path =  name + "\\" + path;
			parent = parent.parent;
		}while(parent !=null);
		
		return "\\"+path;
	}
	
	public String getParentPathIDs() {
		String path = this.getId().toString();
		CvsAbstractJobOrGroup parent = this.parent;
		
		do {
			if(parent == null) {break;}
			// walk up the chain until we have no more parents. 
			Integer name = parent.getId(); // We need our name;	
			path =  name + "\\" + path;
			parent = parent.parent;
		}while(parent !=null);
		
		return "\\"+path;
	}

	public CvsCalendar getCalender() {
		return calender;
	}

	public void setCalender(CvsCalendar calender) {
		this.calender = calender;
	}
}
