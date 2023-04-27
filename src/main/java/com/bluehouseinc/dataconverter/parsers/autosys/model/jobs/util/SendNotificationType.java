package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.util;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum SendNotificationType {
	NO("N", 0), YES("Y", 1), FAILURE("F", 2);

	final String name;
	final int orderNum;

	SendNotificationType(String name, int orderNum) {
		this.name = name;
		this.orderNum = orderNum;
	}

	static SendNotificationType findByName(String name) {
		return Arrays.stream(values()).filter(snt -> snt.getName().equals(name)).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	static SendNotificationType findByOrderNum(int orderNum) {
		return Arrays.stream(values()).filter(snt -> snt.getOrderNum() == orderNum).findFirst().orElseThrow(IllegalArgumentException::new);
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

	public static SendNotificationType getSendNotificationType(String value) {
		if (isNumeric(value)) {
			return findByOrderNum(Integer.parseInt(value));
		} else {
			return findByName(value);
		}
	}

}