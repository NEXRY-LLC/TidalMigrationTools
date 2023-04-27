package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class JobResource implements Serializable {
	private String name;
	private String asterisk;
	private String s;
	private String n;
}
