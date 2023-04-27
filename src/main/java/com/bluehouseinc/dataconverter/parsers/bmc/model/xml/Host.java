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
public class Host implements Serializable {
	private static final long serialVersionUID = -2756771030007298605L;

	@XmlElement(name = "CCC_command")
	int cCC_command;

	@XmlElement(name = "CDC_command")
	int cDC_command;

	@XmlElement(name = "Charset")
	String charset;

	@XmlElement(name = "Conntype")
	String conntype;

	@XmlElement(name = "Directory")
	String directory;

	@XmlElement(name = "HostName")
	String hostName;

	@XmlElement(name = "Lang")
	Object lang;

	@XmlElement(name = "LogicalKeyName")
	String logicalKeyName;

	@XmlElement(name = "OsType")
	String osType;

	@XmlElement(name = "Passive")
	int passive;

	@XmlElement(name = "Passphrase")
	String passphrase;

	@XmlElement(name = "Password")
	String password;

	@XmlElement(name = "Port")
	int port;

	@XmlElement(name = "SSHCompression")
	boolean sSHCompression;

	@XmlElement(name = "User")
	String user;


}
