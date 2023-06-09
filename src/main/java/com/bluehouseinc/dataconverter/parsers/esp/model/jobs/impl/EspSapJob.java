package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.HashMap;
import java.util.Map;

import com.bluehouseinc.dataconverter.importers.csv.CsvSAPData;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspSapJob extends EspAbstractJob {

	CsvSAPData sapData;

	String abapName; // not required if JOBCOPY statement is used to copy an existing SAP job. Pay attention when converting into TIDAL's SAP job type.
	String sapUser;
	String sapStepUser;
	String variant;
	String sapJobName;
	String sapJobClass;
	String startMode;
	String printRecipient;
	String printDept;
	Integer printColumns = 80;//COLUMNS
	Integer printExpire = 8;
	Integer printRows = 65;
	String printFormat;
	String printSpoolName;
	
	Map<EspSapJobOptionalStatement, String> optionalStatements = new HashMap<>();

	public EspSapJob(String name) {
		super(name);
	}

	//SAPJOBCLASS, SAPJOBNAME, VARIANT
	public enum EspSapJobOptionalStatement {
		ARCCLIENT, ARCCONNECT, ARCDATE, ARCDOCCLASS, ARCDOCTYPE, ARCFORMAT, ARCHOSTLINK, ARCINFO, ARCMODE, ARCOBJTYPE, ARCPATH, ARCPRINTER, ARCPROTOCOL, ARCREPORT, ARCSERVICE, ARCSTORAGE, ARCTEXT, ARCUSER, ARCVERSION, BANNER, BANNERPAGE,
		CHILDMONITOR, EMAILADDR, FAILUREMSG, LANGUAGE, PRINTCOPIES, PRINTCOVER, PRINTDATASET, PRINTDEST, PRINTFOOTER, PRINTHOSTPAGE, PRINTIMMED, PRINTNEWSPOOL, PRINTPRIORITY, PRINTPW,
		PRINTREL, PRINTREQTYPE, PUTINOUTBOX, RECIPIENTBCC, RECIPIENTCC, RECIPIENTEXPRESS, RECIPIENTFORWARD, RECIPIENTPRINT, RECIPIENTTYPE, RFCDEST, SAPCLIENT, SAPEMAILADDR, SAPFAILUREMSG,
		SAPLANGUAGE, SAPSUCCESSMSG, SAPTARGETSYSTEM, SPOOLRECIPIENT, SUCCESSMSG, WEBPOSTING, RELDElAY
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.SAP;
	}
}
