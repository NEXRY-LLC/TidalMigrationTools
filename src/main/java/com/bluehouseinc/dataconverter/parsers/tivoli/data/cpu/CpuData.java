package com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu;

import lombok.Data;

/*
 * CPUNAME AMFINAN1
  DESCRIPTION "lawdev"
  OS UNIX
  NODE lawdev TCPADDR 41111
  DOMAIN MASTERDM
  FOR MAESTRO
    TYPE FTA
    AUTOLINK ON
    BEHINDFIREWALL OFF
    FULLSTATUS ON
END
 */
@Data
public class CpuData {

	private String name;
	private String description;
	private String operatingSystem;
	private String nodeName;
	private String forwhat;
	private String type;
	private String autoLink;
	private String behindFirewall;
	private String fullStatus;
	private String domain;
	private String ignore;
	
	public enum OS {
		UNIX, WINDOWS
	}
}
