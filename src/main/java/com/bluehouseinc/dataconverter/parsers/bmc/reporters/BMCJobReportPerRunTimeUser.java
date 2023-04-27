package com.bluehouseinc.dataconverter.parsers.bmc.reporters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCDataModel;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOS400Job;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.util.ObjectUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BMCJobReportPerRunTimeUser implements IReporter {

	Map<String, List<String>> rtejobs = new HashedMap<>();

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		BMCDataModel bmc = (BMCDataModel) model;

		log.trace("############################################### Jobs per Runtime User for OS400");

		List<BaseBMCJobOrFolder> data = ObjectUtils.toFlatListByUnknownType(bmc.getDataObjects(), BMCOS400Job.class);
		log.trace("Todal OS400 Jobs + " + data.size());

		data.forEach(j -> {
			BMCOS400Job os = (BMCOS400Job) j;
			String jobname = os.getName();
			String user = os.getJobData().getRUNAS();

			if (!rtejobs.containsKey(user)) {
				List<String> jobs = new ArrayList<>();
				jobs.add(jobname);
				rtejobs.put(user, jobs);
			} else {
				rtejobs.get(user).add(jobname);
			}

		});

		for (String user : rtejobs.keySet()) {
			List<String> jobs = rtejobs.get(user);
			log.trace("USER[" + user + "]"+ "\n");
			jobs.stream().forEach(j -> log.trace("\t" + j ));
		}

		log.trace("############################################### Jobs per Runtime User for OS400");

	}
}
