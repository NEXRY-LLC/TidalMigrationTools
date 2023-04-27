package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.io.Serializable;

public class AdRule implements Serializable {
	private String rule;

	public AdRule(String rule) {
		this.rule = sanitizeAdRule(rule);
	}

	public String getAdRule() {
		return this.rule;
	}

	public static String sanitizeAdRule(String line) {
		return line.replaceAll("^[0,1,-]", "").replaceAll("(\\s*)\\<", "").replaceAll("(\\s*)\\>", "").replaceAll("ADRULE", "").trim();
	}

	@Override
	public String toString() {
		return rule;
	}
}
