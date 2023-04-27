package com.dataconverter.csv.model;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CvsOwner {

	@CsvBindByName(column = "owner_name")
	private String name;
	
	
	public CvsOwner(String name){
		this.name = name;
	}
}
