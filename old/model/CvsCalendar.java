package com.dataconverter.csv.model;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class CvsCalendar {

	@CsvBindByName(column = "calendar_name")
	private String name;
	
	@CsvBindByName(column = "calendar_notes")
	private String notes;
	
	public CvsCalendar(String name){
		this.name = name;
	}
	
}
