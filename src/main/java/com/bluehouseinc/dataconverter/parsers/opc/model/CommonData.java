package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class CommonData implements Serializable {
	// 1st row
	private String appText;
	private String calendarId;

	// 2nd row
	private String ownerId;
	private String validFrom;
	private String validTo;
	private String status;
}
