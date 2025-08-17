package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc;

import java.time.LocalDate;

import lombok.Data;
import lombok.Setter;

// CA7Schedule.java - Scheduling rule definition
@Data
@Setter
public class CA7Schedule {
	private String name;
	private String rule;
	private LocalDate validFrom;
	private LocalDate validTo;
	private int shift;
	private String shiftSign;
	private String type;
	private String iaTime;
	private int deadlineDay;
	private String deadlineTime;
	private String ruleDescription;

	// Constructors
	public CA7Schedule() {
	}

	public CA7Schedule(String name, String rule) {
		this.name = name;
		this.rule = rule;
	}

	@Override
	public String toString() {
		return String.format("CA7Schedule{name='%s', rule=%d, type='%s'}", name, rule, type);
	}
}