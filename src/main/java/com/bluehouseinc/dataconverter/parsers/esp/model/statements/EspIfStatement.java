package com.bluehouseinc.dataconverter.parsers.esp.model.statements;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspIfStatement {

	String data;


	public EspIfStatement(String data) {
		this.data = data;
	}
}
