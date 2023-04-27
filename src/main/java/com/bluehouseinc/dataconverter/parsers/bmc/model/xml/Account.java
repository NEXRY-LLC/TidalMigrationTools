package com.bluehouseinc.dataconverter.parsers.bmc.model.xml;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Data;
import lombok.ToString;



@XmlAccessorType(XmlAccessType.FIELD)
@Data
@ToString
public class Account implements Serializable  {
	private static final long serialVersionUID = -2756771030007298605L;

	@XmlElement(name = "AccountName")
	String accountName;

	boolean checksum;

	Object eMgroup;

	String eMuser;

	@XmlElement(name = "Host1")
	Host leftHost;

	@XmlElement(name = "Host2")
	Host rightHost;

	double timeStamp;

	boolean verifyBytes;

	boolean verifyDest;

}