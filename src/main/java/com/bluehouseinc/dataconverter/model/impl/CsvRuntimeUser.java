package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.utils.StringUtils;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = false)
public class CsvRuntimeUser {

	public CsvRuntimeUser(String name) {
		this(name,null);
	}

	public CsvRuntimeUser(String name, String domain) {
		this.runTimeUserName = name;
		this.runTimeUserDomain = domain;
	}

	@EqualsAndHashCode.Include
	@CsvBindByName
	String runTimeUserName;

	@EqualsAndHashCode.Include
	@CsvBindByName
	String runTimeUserDomain;

	@CsvBindByName
	String passwordForFtpOrAS400;

	@CsvBindByName
	String passwordForPeopleSoft;

	@CsvBindByName
	String passwordForSAP;

	/**
	 *
	 * @return username@domain format, if domain is null then just username
	 *
	 */
	public String getUserAndDomainName() {
		if(StringUtils.isBlank(this.runTimeUserDomain)) {
			return runTimeUserName;
		}

		return  this.runTimeUserName + "@" + this.runTimeUserDomain ;
	}

}
