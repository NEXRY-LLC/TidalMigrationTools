package com.bluehouseinc.dataconverter.parsers.bmc.model.xml;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NodeKey {

	@EqualsAndHashCode.Include
	private String name;
	@EqualsAndHashCode.Include
	private String accountName;

	private Account account;

	public NodeKey(String name, String accountname, Account account) {
		this.name = name;
		this.accountName = accountname;
		this.account = account;
	}


	public NodeKey(String name, String accountname) {
		this.name = name;
		this.accountName = accountname;
	}

}
