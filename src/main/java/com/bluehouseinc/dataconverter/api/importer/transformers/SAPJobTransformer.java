package com.bluehouseinc.dataconverter.api.importer.transformers;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.impl.CsvSAPJob;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.service.ServiceJob;
import com.bluehouseinc.tidal.api.model.service.Service;
import com.bluehouseinc.tidal.api.model.users.Users;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SAPJobTransformer implements ITransformer<CsvSAPJob, ServiceJob> {

	ServiceJob base;
	TidalAPI tidal;
	private static Integer serviceid;
	private static String serviceName;

	public static final String GUID = "{51C57049-3215-44b7-ABE1-C012FF786010}";

	public SAPJobTransformer(ServiceJob base, TidalAPI tidal) {
		this.base = base;
		this.tidal = tidal;
	}

	@Override
	public ServiceJob transform(CsvSAPJob in) throws TransformationException {

		if (in.getName().contains("ZRV60SBAT_FINAL_2355")) {
			in.getName();
		}
		
		this.base.setType(JobType.SAPJOB);
		// String ext = getRunCopy();

		if (serviceid == null) {
			Service adapter = tidal.getAdapterByGUID(GUID);

			if (adapter == null) {
				throw new RuntimeException("Check that the PS adapter is installed in this master");
			}

			serviceid = adapter.getId();
			serviceName = adapter.getFullname();
		}

		this.base.setServiceid(serviceid);
		this.base.setServicename(serviceName);

		log.debug("SAPJobTransformer transform -> processing(" + in.getFullPath() + ") Checking RunMode[" + in.getJobMode() + "]");

		if (in.getJobMode().equals("RUN_COPY")) {
			Users rte = tidal.getUserByAccountNameAndDomain(in.getRuntimeUser().getRunTimeUserName(),null);
			log.debug("SAPJobTransformer transform -> processing(" + in.getFullPath() + ")");

			String programName = in.getProgramName() == null ? "ZBNCHMK1" : in.getProgramName();
			String variant = in.getVariant() == null ? "ZBADRUN1" : in.getVariant();

			String extJobrun = getRunCopyNew().replace("$$RTEID$$", Integer.toString(rte.getId())).replace("$$JOBCLASS$$", in.getJobSAPClass() == null ? "" : in.getJobSAPClass()).replace("$$JOBNAME$$", in.getJobName().toUpperCase())
					.replace("$$VARIANT$$", variant).replace("$$PROGNAME$$", programName).replace("$$PDEST$$", in.getPdest() == null ? "" : in.getPdest()).replace("$$PRCOP$$", in.getPrcop() == null ? "1" : "1")
					.replace("$$PLIST$$", in.getPlist() == null ? "" : in.getPlist()).replace("$$PRTXT$$", in.getPrtxt() == null ? "" : in.getPrtxt()).replace("$$PRBER$$", in.getPrber() == null ? "" : in.getPrber())
					.replace("$$RTENAME$$", in.getRuntimeUser().getRunTimeUserName());

			extJobrun = extJobrun.replaceAll("\\n", "");
			extJobrun = extJobrun.replaceAll("\\r", "");

			this.base.setExtendedinfo(extJobrun);
			this.base.setCommand(programName + "[" + variant + "]");

			log.debug("SAPJobTransformer transform -> processing(" + in.getFullPath() + ") Returning");
		} else {
			this.base.setExtendedinfo(getLinkInfo(in.getJobName()));
			log.debug("SAPJobTransformer transform(" + in.getFullPath() + ") Setting Place Holdedr for [" + in.getJobMode() + "]");
		}

		return this.base;
	}

	private String getLinkInfo(String name) {
		StringBuilder s = new StringBuilder();

		s.append("<sapclone>");
		s.append("<cname>" + name + "</cname>");
		s.append("<trkchild>Y</trkchild>");
		s.append("<externuid />");
		s.append("<jobclass />");
		s.append("<linktype>L</linktype>");
		s.append("<usesapname>Y</usesapname>");
		s.append("</sapclone>");

		return s.toString();
	}

	private String getRunCopyNew() {
		return "<sstps>\n" + "	<connusr>\n" + "		<var.username>$$RTEID$$</var.username>\n" + "		<var.value />\n" + "	</connusr>\n" + "	<password>\n" + "		<var.password>$$RTEID$$</var.password>\n" + "		<var.value />\n"
				+ "	</password>\n" + "	<conndomain>\n" + "		<var.conndomain>$$RTEID$$</var.conndomain>\n" + "		<var.value />\n" + "	</conndomain>\n" + "	<principal>\n" + "		<var.principal>$$RTEID$$</var.principal>\n"
				+ "		<var.value />\n" + "	</principal>\n" + "	<keytab>\n" + "		<var.keytab>$$RTEID$$</var.keytab>\n" + "		<var.value />\n" + "	</keytab>\n" + "	<job>\n" + "		<jname>$$JOBNAME$$</jname>\n"
				+ "		<jobcount />\n" + "	</job>\n" + "	<jobnamealias />\n" + "	<clonetag>N</clonetag>\n" + "	<externuid />\n" + "	<spoollistrecipient>\n" + "		<recipient />\n" + "		<rectype />\n" + "		<copy>\n"
				+ "		</copy>\n" + "		<blindcopy>\n" + "		</blindcopy>\n" + "		<express>\n" + "		</express>\n" + "		<noforwarding>\n" + "		</noforwarding>\n" + "		<noprint>\n" + "		</noprint>\n"
				+ "		<deliver />\n" + "		<mailstatus />\n" + "	</spoollistrecipient>\n" + "	<trkchild>Y</trkchild>\n" + "	<usesapname>Y</usesapname>\n" + "	<cst>1</cst>\n" + "	<jobclass />\n" + "	<linktype>L</linktype>\n"
				+ "	<type>1</type>\n" + "	<incranjob>N</incranjob>\n" + "	<isearliest />\n" + "	<usefirstrun>N</usefirstrun>\n" + "	<sstep1>1</sstep1>\n" + "	<stype1>2</stype1>\n" + "	<ovar1>$$VARIANT$$</ovar1>\n"
				+ "	<abap1>$$PROGNAME$$</abap1>\n" + "	<svarm1>\n" + "		<var.compound />\n" + "	</svarm1>\n" + "	<vvals1>\n" + "		<var.compound />\n" + "	</vvals1>\n" + "	<tempvar1 />\n" + "	<lang1 />\n"
				+ "	<print1>&lt;print&gt;&lt;new&gt;X&lt;/new&gt;&lt;spoolname&gt;$$PLIST$$&lt;/spoolname&gt;&lt;pri&gt;1&lt;/pri&gt;&lt;retain&gt;8&lt;/retain&gt;&lt;rows&gt;65&lt;/rows&gt;&lt;cols&gt;132&lt;/cols&gt;&lt;format&gt;X_65_132&lt;/format&gt;&lt;sapcover&gt;D&lt;/sapcover&gt;&lt;oscover&gt;D&lt;/oscover&gt;&lt;selcover&gt;X&lt;/selcover&gt;&lt;recip&gt;JWANDER&lt;/recip&gt;&lt;dept&gt;341\n"
				+ "		ic&lt;/dept&gt;&lt;/print&gt;</print1>\n" + "	<printcheck1>Y</printcheck1>\n" + "<des1>$$PDEST$$</des1>\n" + "<cop1>$$PRCOP$$</cop1>\n" + "<pas1>$$PRBER$$</pas1>\n" + "<runt1>$$PRTXT$$</runt1>\n" + "	<rel1>\n"
				+ "	</rel1>\n" + "	<pi1>\n" + "	</pi1>\n" + "	<archivecheck1>1</archivecheck1>\n" + "	<sobj1 />\n" + "	<objn1 />\n" + "	<inf1 />\n" + "	<pm1>1</pm1>\n" + "	<cmd>$$PROGNAME$$($$VARIANT$$)</cmd>\n" + "</sstps>";
	}
}
