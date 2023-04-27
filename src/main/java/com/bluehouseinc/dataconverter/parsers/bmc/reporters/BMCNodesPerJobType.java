package com.bluehouseinc.dataconverter.parsers.bmc.reporters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCDataModel;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BMCNodesPerJobType implements IReporter {

	Map<String, List<String>> rtejobs = new HashedMap<>();


	StringBuilder builder = new StringBuilder();



	private String getNodeName(BaseBMCJobOrFolder obj) {

		if (obj instanceof BaseBMCJobOrFolder) {
			BaseBMCJobOrFolder bobj = obj;
			String node = bobj.getNodeName();

			if (node == null) {
				// System.out.println("NODE IS NOT DEFINED FOR JOB ["+ bobj.getJobData().getJOBNAME() + "]");
				return "NODE IS NOT DEFINED FOR JOB " + bobj.getJobData().getJOBNAME();
			} else {
				return node.toLowerCase();
			}
		}

		return "UNKNOWN";
	}

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {
		BMCDataModel bmc = (BMCDataModel) model;

		log.trace("############################################### Nodes per job type");

		bmc.getJobtypes().keySet().stream().forEach(f -> {

			String jobtype = f.toString();
			log.trace("Job Type [" + jobtype + "]");

			List<String> names = new ArrayList<>();

			bmc.getJobtypes().get(f).forEach(r -> {
				String rtename = getNodeName(r);

				if (!names.contains(rtename)) {
					names.add(rtename);
					builder.append(rtename + "=" + jobtype + "Agent,");
				}
			});

			names.forEach(n -> {
				log.trace("\t" + n);
			});

			log.trace("\n");
			log.trace(builder.toString());

			builder.setLength(0);

		});

		log.trace("\n############################################### Nodes per job type");
	}
}
