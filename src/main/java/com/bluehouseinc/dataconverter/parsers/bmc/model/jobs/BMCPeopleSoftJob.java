package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class BMCPeopleSoftJob extends BaseBMCJobOrFolder {

	List<SetVarData> unusedVarData = new ArrayList<>();
	PSObject psObject = new PSObject();

	@Override
	public BMCJobTypes getBMCJobType() {
		return BMCJobTypes.PS8;
	}

	@Override
	public String getPlaceHolderData() {
		StringBuilder b = new StringBuilder();

		this.unusedVarData.stream().forEach(data -> {

			b.append(data.getNAME());
			b.append("=");
			b.append(data.getVALUE());
			b.append("\n");
		});

		return b.toString();
	}

	/**
	 * This is missing PS8-PROCESSES-J1-P1-PROCESSTYPE,there seems to be a large number of them
	 * Notice that type is PSJob
	 * <VARIABLE NAME="%%PS8-PROCESSES-J1-P1-PROCESSNAME" VALUE="CB3WMVCH" />
	 * <VARIABLE NAME="%%PS8-PROCESSES-J1-P1-PROCESSTYPE" VALUE="PSJob" />
	 * <VARIABLE NAME="%%PS8-PROCESSES-J1-P1-DESCRIPTION" VALUE="3WM Interface/Voucher Build" />
	 * <VARIABLE NAME="%%PS8-PROCESSES-J1-P2-PROCESSNAME" VALUE="CB_VCHR_IN" />
	 * <VARIABLE NAME="%%PS8-PROCESSES-J1-P2-PROCESSTYPE" VALUE="Application Engine" />
	 * <VARIABLE NAME="%%PS8-PROCESSES-J1-P2-DESCRIPTION" VALUE="Voucher Interface process" />
	 * <VARIABLE NAME="%%PS8-PROCESSES-J1-P3-PROCESSNAME" VALUE="AP_VCHRBLD" />
	 * <VARIABLE NAME="%%PS8-PROCESSES-J1-P3-PROCESSTYPE" VALUE="Application Engine" />
	 * <VARIABLE NAME="%%PS8-PROCESSES-J1-P3-DESCRIPTION" VALUE="Voucher Build" />
	 */
	@Override
	public void doProcesJobSpecificVarData(SetVarData data) {
		/*
		 * bmcProcessType = PSProcessTypes.getType(getVarByName("%%PS8-PRCSTYPE"));
		 * bmcUserid = getVarByName("%%PS8-USERID");
		 * bmcRunControl = getVarByName("%%PS8-RUNCONTROLID"); // Named ID in BMC but it's really not the ID, this is
		 * used to get the real id
		 * bmcConProfile = getVarByName("%%PS8-CON_PROFILE");
		 * bmcServerName = getVarByName("%%PS8-SERVERNAME");
		 * bmcProcessName = getVarByName("%%PS8-PRCSNAME");
		 * bmcApOut = getVarByName("%%PS8-APOUT");
		 * bmcDescription = getVarByName("%%PS8-DESCRIPTION");
		 * bmcProcessJ1P1Description = getVarByName("%%PS8-PROCESSES-J1-P1-DESCRIPTION");
		 * bmcProcessJ1P1ProcessName = getVarByName("%%PS8-PROCESSES-J1-P1-PROCESSNAME");
		 * bmcProcessJ1P1ProcessType = getVarByName("%%PS8-PROCESSES-J1-P1-PROCESSTYPE");
		 */

		if (data.getNAME().equals("%%PS8-USERID")) {
			psObject.setBmcUserid(data.getVALUE());
		} else if (data.getNAME().equals("%%PS8-RUNCONTROLID")) {
			psObject.setBmcRunControl(data.getVALUE());
		} else if (data.getNAME().equals("%%PS8-CON_PROFILE")) {
			psObject.setBmcConProfile(data.getVALUE());
		} else if (data.getNAME().equals("%%PS8-SERVERNAME")) {
			psObject.setBmcServerName(data.getVALUE());
		} else if (data.getNAME().equals("%%PS8-PRCSNAME")) {
			psObject.setBmcProcessName(data.getVALUE());
		} else if (data.getNAME().equals("%%PS8-APOUT")) {
			psObject.setBmcApOut(data.getVALUE());
		} else if (data.getNAME().equals("%%PS8-DESCRIPTION")) {
			psObject.setBmcDescription(data.getVALUE());
		} else if (data.getNAME().equals("%%PS8-PROCESSES-J1-P1-DESCRIPTION")) {
			psObject.setBmcProcessJ1P1Description(data.getVALUE());
		} else if (data.getNAME().equals("%%PS8-PROCESSES-J1-P1-PROCESSNAME")) {
			psObject.setBmcProcessJ1P1ProcessName(data.getVALUE());
		} else if (data.getNAME().equals("%%PS8-PROCESSES-J1-P1-PROCESSTYPE")) {
			psObject.setBmcProcessJ1P1ProcessType(data.getVALUE());
		} else {
			this.unusedVarData.add(data);
		}

	}

	public String getExtenedInfo() {
		StringBuilder s = new StringBuilder();

		s.append("<jobdef>");
		s.append("<connusr><var.username>3</var.username><var.value/></connusr>");
		s.append("<password><var.password>3</var.password><var.value /></password>");
		s.append("<conndomain><var.conndomain>3</var.conndomain><var.value /></conndomain>");
		s.append("<principal><var.principal>3</var.principal><var.value/></principal>");
		s.append("<keytab><var.keytab>3</var.keytab><var.value /></keytab>");
		s.append("<userid/><username/><userdomain/><properties/><parameters />");
		s.append("<steps>");
		s.append("<step psjob_step=\"1\">");
		s.append("<psjob_procname>");

		s.append(this.getPsObject().getBmcProcessName());

		s.append("</psjob_procname>");
		s.append("<descr>");

		s.append(this.getPsObject().getBmcDescription());

		s.append("</descr>");
		s.append("<psjob_proctype>");

		s.append("Application Engine");
		// s.append(this.getPsObject().getBmcProcessType());

		s.append("</psjob_proctype>");
		s.append("<psjob_componentname>");

		// s.append(this.getData().getCalcedComponentName());
		// PRCSMULTI.GBL.Sample Processes
		s.append("PRCSMULTI.GBL.Sample Processes");

		s.append("</psjob_componentname>");
		s.append("<psjob_always>0</psjob_always>");
		s.append("<psjob_runcontrol>");

		s.append(this.getPsObject().getBmcRunControl()); // Not the right ID, need to query this from PS system.

		s.append("</psjob_runcontrol>");
		s.append("<outputtype>Web</outputtype><psjob_output>6</psjob_output><outputformat>PDF</outputformat><parent_stepsaved></parent_stepsaved><psjob_outfmt>2</psjob_outfmt>");
		s.append("<psjob_outdest>%Log/Output Directory%</psjob_outdest><outdest_over>N</outdest_over><psjob_server />");

		// s.append("<psjob_saoutput>\\\\hd-psappdw01\\psoft\\pscfg\\FINDEV\\appserv\\prcs\\FINDEV\\log_output</psjob_saoutput>");

		String output = "\\\\hd-psappdw01\\psoft\\pscfg\\FINDEV\\appserv\\prcs\\FINDEV\\log_output";
		s.append("<psjob_saoutput>" + output + "</psjob_saoutput>");

		s.append("<psjob_runmask />");

		s.append("<psjob_runvals>");
		s.append("<param name=\"MKT\"><var.compound>");

		// s.append(this.getData().getMarket());
		s.append("GBL");

		s.append("</var.compound></param>");

		s.append("<param name=\"RID\"><var.compound>");

		// PRCSRUNCNTL_RUN_CNTL_ID
		// s.append(this.getData().getRunControlId());
		s.append("PRCSRUNCNTL_RUN_CNTL_ID");

		s.append("</var.compound></param>");

		s.append("<param name=\"URL\"><var.compound>");

		String url = "https://dev-psoft.hqric.udrt.com:9285/psp/FINDEV/EMPLOYEE/ERP/c/";
		String runcontrol = "PROCESS_SCHEDULER.PRCSMULTI.GB";
		// PROCESS_SCHEDULER.PRCSMULTI.GBL
		s.append(url + runcontrol);
		s.append("</var.compound></param>");

		s.append("<param name=\"PNL\"><var.compound>");

		// s.append(this.getData().getCalcedPnlDescription());
		s.append("Sample Processes");

		s.append("</var.compound></param>");

		s.append("<param name=\"RCTL\"><var.compound>");

		// c/PROCESS_SCHEDULER.PRCSMULTI.GBL
		// s.append("c/"+this.getData().getCaledControlName());
		s.append("c/PROCESS_SCHEDULER.PRCSMULTI.GBL");

		s.append("</var.compound></param>");

		s.append("</psjob_runvals>");

		s.append("<psjob_extrunvals />");

		s.append("<longdescr>BlueHouse Converted Job</longdescr>");
		s.append(
				"<parmlist> -CT %%DBTYPE%% -CS %%SERVER%% -CD %%DBNAME%% -CA %%ACCESSID%% -CAP %%ACCESSPSWD%% -RP %%PRCSNAME%% -I %%INSTANCE%% -R %%RUNCNTLID%% -CO %%OPRID%% -OT %%OUTDESTTYPE%% -OP %%OUTDEST%% -OF %%OUTDESTFORMAT%% -LG %%LANGUAGECD%% </parmlist>");

		// String tblone =this.getTable().getTableOne().replace("\"","").replace(" ","");
		// String tbltwo =this.getTable().getTableTwo().replace("\"","").replace(" ","");
		String tblone = "";
		String tbltwo = "PRCSRUNCNTL";

		if (!tblone.isEmpty()) {
			s.append("<rctable1>" + tblone + "</rctable1>");
		} else {
			s.append("<rctable1/>");
		}

		if (!tbltwo.isEmpty()) {
			s.append("<rctable2>" + tbltwo + "</rctable2>");
		} else {
			s.append("<rctable2/>");
		}

		s.append("<psjob_instance />");
		s.append("<psjob_runstatus />");
		s.append("<psjob_cmdline />");
		s.append("<psjob_runstart />");
		s.append("<psjob_runfinish />");
		s.append("<psjob_parmlist />");

		s.append("</step>");
		s.append("</steps>");

		s.append("<cmd>" + this.getPsObject().getBmcProcessName() + " (" + this.getPsObject().getBmcRunControl() + ")</cmd>");

		s.append("</jobdef>");

		return s.toString();
	}

	@Data
	public class PSObject {

		private String bmcUserid;
		private String bmcRunControl;
		private String bmcConProfile;
		private String bmcServerName;
		private String bmcProcessType;
		private String bmcProcessName;
		private String bmcApOut;
		private String bmcDescription;
		private String bmcProcessJ1P1Description;
		private String bmcProcessJ1P1ProcessName;
		private String bmcProcessJ1P1ProcessType;

	}

	@Override
	public String getVarPrefix() {
		return "%%PS8-";
	}
}
