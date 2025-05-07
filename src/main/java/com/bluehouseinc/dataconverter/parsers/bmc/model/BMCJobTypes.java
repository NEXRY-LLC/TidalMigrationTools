package com.bluehouseinc.dataconverter.parsers.bmc.model;

import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCAOP11Job;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCAmazonEC2Job;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCBIMJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCCommandLineJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCDataBaseJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCFileTransferJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCFileWatcher;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCHadoop;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCInformaticaETL;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOS400AttachJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOS400Job;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOS400MultiCommand;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOracleEBSJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCPeopleSoftJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCRestOICORA;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCRestOracle;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCRestWROIC;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSAPBOJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSAPJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCSendEmailP;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCWebServiceJob;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.reporters.BMCCountByBMCType;

/**
 * BMC has many job types like TIDAL so we have to deal with each one differently,
 *
 * @author brianhayes
 *
 */
public enum BMCJobTypes {

	OS, OS400, OS400ATTACH, OS400MULTICOMMAND, DATABASE, FILE_TRANS, OEBS, PS8, FileWatch, OAP_11I, BIM, AmazonEC2
	, WS, SAP_BO, SAP,HADOOP,SENDEMAILP,ETL_INFA,RESTORACLE,RESTOICORA,RESTWROIC,

	// SPECIAL STUFF not build into BMC XML.
	SMARTFOLDER, SIMPLEFOLDER, SUBFOLDER;

	public static BaseBMCJobOrFolder getJobByType(BMCJobTypes types) {

		switch (types) {
		case OS:
			return new BMCCommandLineJob();
		case OS400:
			return new BMCOS400Job();
		case OS400ATTACH:
			return new BMCOS400AttachJob();
		case OS400MULTICOMMAND:
			return new BMCOS400MultiCommand();
		case DATABASE:
			return new BMCDataBaseJob();
		case FILE_TRANS:
			return new BMCFileTransferJob();
		case OEBS:
			return new BMCOracleEBSJob();
		case PS8:
			return new BMCPeopleSoftJob();
		case FileWatch:
			return new BMCFileWatcher();
		case OAP_11I:
			return new BMCAOP11Job();
		case BIM:
			return new BMCBIMJob();
		case AmazonEC2:
			return new BMCAmazonEC2Job();
		case WS:
			return new BMCWebServiceJob();
		case SAP_BO:
			return new BMCSAPBOJob();
		case SAP:
			return new BMCSAPJob();
		case HADOOP:
			return new BMCHadoop();
		case SENDEMAILP:
			return new BMCSendEmailP();
		case ETL_INFA:
			return new BMCInformaticaETL();
		case RESTORACLE:
			return new BMCRestOracle();
		case RESTWROIC:
			return new BMCRestWROIC();
		case RESTOICORA:
			return new BMCRestOICORA();
		default:
			throw new RuntimeException(types.name() + " Not Supported");
		}
	}

}
