package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.util;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum AutosysYesNoType {
	NO("N", 0), YES("Y", 1);

	final String name;
	final int orderNum;

	AutosysYesNoType(String name, int orderNum) {
		this.name = name;
		this.orderNum = orderNum;
	}

	static AutosysYesNoType findByName(String name) {
		return Arrays.stream(values()).filter(aynt -> aynt.getName().equals(name)).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	static AutosysYesNoType findByOrderNum(int orderNum) {
		return Arrays.stream(values()).filter(aynt -> aynt.getOrderNum() == orderNum).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	static boolean isNumeric(String value) {
		if (value == null) {
			return false;
		}
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static AutosysYesNoType getAutosysYesNoType(String value) {
		if (isNumeric(value)) {
			return findByOrderNum(Integer.parseInt(value));
		} else {
			return findByName(value);
		}
	}
}
