package com.bluehouseinc.dataconverter.parsers.bmcmainframe.cvs;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.parsers.bmc.BMCParser;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCDataModel;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoIfRerunData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.JobData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.OnData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.OutConditionData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SimpleFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SmartFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SubFolder;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BMCParserMainframeCVS extends BMCParser {

	public BMCParserMainframeCVS(ConfigurationProvider cfgProvider) {
		super(cfgProvider);
	}

	@Override
	public TidalDataModel parseXmlFile(File file) throws Exception {
		TidalDataModel datamodel = super.parseXmlFile(file);

		JAXBContext context = JAXBContext.newInstance(this.data.getClass());
		Marshaller marsh = context.createMarshaller();

		File parentfolder = file.getAbsoluteFile().getParentFile();

		String filename = "Processed_" + file.getName().toString();

		File outputfile = new File(parentfolder, filename);

		// Configure marshaller
		marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marsh.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

		if (outputfile.exists()) {
			outputfile.delete();
		}
		log.info("Saving Updated XMLFile[" + outputfile.getAbsolutePath() + "]");

		marsh.marshal(this.data, outputfile);

		return datamodel;
	}

	@Override
	public TidalDataModel processDataModel() throws Exception {
		TidalDataModel dm = super.processDataModel();

		return dm;
	}

	@Override
	public void doProcessJob(BMCDataModel model, BaseBMCJobOrFolder group, JobData ajob) {

		// REMOVE DOC* Statements
		// USE_INSTREAM_JCL="N" - Remove this.
		// MEMNAME = Script name
		// MEMLIB = Path
		// PREVENTNCT2="N" - Remove
		// AUTOARCH="1" - Remove
		// SYSDB="1" - Remove
		// <OUTCOND NAME="BB001D-ENDED-OK" SIGN="ADD" <- ADD should be +
		// ON PGMS="ANYSTEP" CODE="*****,****" > <- Remove

		ajob.setPREVENTNCT2(null);
		ajob.setAUTOARCH(null);
		ajob.setSYSDB(null);
		ajob.setNODEID(null);
		ajob.setDOCLIB(null);
		ajob.setDOCMEM(null);

		String groupname = ajob.getGROUP();

		ajob.setGROUP(null);

		ajob.setSUBAPPLICATION(groupname);

		ajob.setPARENTFOLDER(group.getName());

		ajob.setUSEINSTREAMJCL("N");
		String memname = ajob.getMEMNAME();

		ajob.setJOBNAME(memname);

		ajob.setMEMNAME(memname + ".groovy");

		ajob.setMEMLIB("/fmf/groovy/JCL/");

		String apptype;

		if (!StringUtils.isBlank(ajob.getTASKTYPE())) {
			apptype = "OS";
		} else {
			apptype = "OS";
		}

		ajob.setAPPLTYPE(apptype);

		ajob.setRUNAS("controlm");

		List<JAXBElement<?>> data = ajob.getAUTOEDIT2OrAUTOEDITOrVARIABLE();
		List<JAXBElement<?>> results = filterOnDataDetails(data);

		ajob.getAUTOEDIT2OrAUTOEDITOrVARIABLE().clear();
		ajob.getAUTOEDIT2OrAUTOEDITOrVARIABLE().addAll(results);
		
		super.doProcessJob(model, group, ajob);

	}

	/*
	 * 
	 * Need to change the folder to VERSION="919" PLATFORM="UNIX" DATACENTER="uat_server" vs the below.
	 * FOLDER_ORDER_METHOD="SYSTEM" REAL_FOLDER_ID="6038" TYPE="1" USED_BY_CODE="0"
	 * <DEFTABLE xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="Folder.xsd">
	 * <FOLDER FOLDER_NAME="BB" DATACENTER="1234567890123456789" FOLDER_DSN="SYS3.QPP3E.CTMOPR.SCHEDULE"
	 * VERSION="918" PLATFORM="MVS" >
	 */
	@Override
	public void onSimpleFolder(BMCDataModel model, SimpleFolder folder) {
		// super.onSimpleFolder(model, folder);
		folder.setDATACENTER("uat_server");
		folder.setPLATFORM("UNIX");
		folder.setREALFOLDERID(6038);
		folder.setTYPE(1);
		folder.setUSEDBYCODE(0);
		folder.setFOLDERORDERMETHOD("SYSTEM");
		folder.setFOLDERDSN(null);
		folder.setVERSION("918");

		super.onSimpleFolder(model, folder);// keep going
	}

	   // Method 2: Specific implementation for your use case
    public static List<JAXBElement<?>> filterOnDataDetails(List<JAXBElement<?>> data) {
        return data.stream()
                .filter(element -> {
                    Object value = element.getValue();
                    
                    // Check if it's a JobDetail type (replace with your actual class)
                    if (value instanceof OnData) {
                    	OnData ondata = (OnData) value;
                        return ondata.getPGMS().isEmpty();
                    }
 
                    // Keep all other types
                    return true;
                })
                .collect(Collectors.toList());
    }
    
	public static <T> List<T> filterByTypeExclude(List<T> originalList, Class<?> excludeType) {
		return originalList.stream().filter(item -> !excludeType.isInstance(item.getClass())).collect(Collectors.toList());
	}

	@Override
	public void onSmartFolder(BMCDataModel model, SmartFolder folder, BaseBMCJobOrFolder group) {
		folder.setNODEID("BMCAGENT");
		folder.setRUNAS("TIDAL");
		super.onSmartFolder(model, folder, group);
	}

	@Override
	public void onSubFolderFolder(BMCDataModel model, SubFolder folder, BaseBMCJobOrFolder group) {
		folder.setNODEID("BMCAGENT");
		folder.setRUNAS("TIDAL");
		super.onSubFolderFolder(model, folder, group);
	}

	@Override
	public void doProcessData(BMCDataModel model, BaseBMCJobOrFolder job, OutConditionData data) {

		if (data.getSIGN().equals("ADD")) {
			data.setSIGN("+");
		}

		super.doProcessData(model, job, data);
	}
}
