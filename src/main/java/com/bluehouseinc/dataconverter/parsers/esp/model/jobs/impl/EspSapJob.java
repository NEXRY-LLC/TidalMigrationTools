package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.HashMap;
import java.util.Map;

import com.bluehouseinc.dataconverter.importers.csv.CsvSAPData;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspSapJob extends EspAbstractJob {

	CsvSAPData sapData;

	String abapName; // not required if JOBCOPY statement is used to copy an existing SAP job. Pay attention when converting into TIDAL's SAP job type.
	String sapUser;
	String sapStepUser;

	Map<EspSapJobOptionalStatement, String> optionalStatements = new HashMap<>();

	public EspSapJob(String name) {
		super(name);
	}

	public enum EspSapJobOptionalStatement {
		ARCCLIENT, ARCCONNECT, ARCDATE, ARCDOCCLASS, ARCDOCTYPE, ARCFORMAT, ARCHOSTLINK, ARCINFO, ARCMODE, ARCOBJTYPE, ARCPATH, ARCPRINTER, ARCPROTOCOL, ARCREPORT, ARCSERVICE, ARCSTORAGE, ARCTEXT, ARCUSER, ARCVERSION, BANNER, BANNERPAGE,
		CHILDMONITOR, COLUMNS, EMAILADDR, EXPIRATION, FAILUREMSG, JOBCOPY, LANGUAGE, LINES, PRINTCOPIES, PRINTCOVER, PRINTDATASET, PRINTDEPARTMENT, PRINTDEST, PRINTFOOTER, PRINTFORMAT, PRINTHOSTPAGE, PRINTIMMED, PRINTNEWSPOOL, PRINTPRIORITY, PRINTPW,
		PRINTREL, PRINTREQTYPE, PRINTSPOOLNAME, PUTINOUTBOX, RECIPIENT, RECIPIENTBCC, RECIPIENTCC, RECIPIENTEXPRESS, RECIPIENTFORWARD, RECIPIENTPRINT, RECIPIENTTYPE, RFCDEST, SAPCLIENT, SAPEMAILADDR, SAPFAILUREMSG, SAPJOBCLASS, SAPJOBNAME,
		SAPLANGUAGE, SAPSUCCESSMSG, SAPTARGETSYSTEM, SPOOLRECIPIENT, STARTMODE, SUCCESSMSG, VARIANT, WEBPOSTING, RELDElAY
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.SAP;
	}
}
