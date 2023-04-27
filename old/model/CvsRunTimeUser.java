package com.dataconverter.csv.model;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CvsRunTimeUser {

	@CsvBindByName(column = "runtime_name")
	private String name;
	
	
	public CvsRunTimeUser(String name){
		this.name = name;
	}
	
	
}
