package com.bluehouseinc.dataconverter.parsers.bmc.model.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@XmlRootElement(name = "Accounts")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Accounts implements Serializable {

	private static final long serialVersionUID = -2756771030007298605L;

	@XmlElement(name = "Account")
	List<Account> accounts;

}
