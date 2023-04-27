package com.bluehouseinc.dataconverter.parsers.bmc.reporters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCDataModel;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOS400Job;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.util.ObjectUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BMCListOS400MultiStepCount implements IReporter {


	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		BMCDataModel bmc = (BMCDataModel) model;

		log.trace("############################################### OS400 MULTISTEP JOBS");

		List<BaseBMCJobOrFolder> bmcjobs = ObjectUtils.toFlatListByUnknownType(bmc.getDataObjects(), BMCOS400Job.class);

		log.trace("Total OS400 Jobs + " + bmcjobs.size());

		final List<String> cnt = new ArrayList<>();

		List<BaseBMCJobOrFolder> multisteps = bmcjobs.stream().filter(f -> f.getFullPath().contains("MULTISTEP-OS400")).collect(Collectors.toList());

		multisteps.forEach(j -> {

			if(((BMCOS400Job) j).getCommands().size() > 1) {
				cnt.add(j.getFullPath());
			}

		});

		log.trace("Total OS400 with Multiple Steps + " + cnt.size() );

		cnt.forEach(f -> log.trace("\t" + f ));

		log.trace("############################################### OS400 MULTISTEP JOBS");

		log.trace("############################################### OS400 JOBS POST COMMANDS");

		cnt.clear();

		bmcjobs.forEach(j -> {
			BMCOS400Job os = (BMCOS400Job) j;

			if (os.getPostCommands().size() > 1) {
				cnt.add(j.getFullPath());
			}
		});

		log.trace("Total OS400 with POST COMMAND Steps + " + cnt.size() );

		cnt.forEach(f -> log.trace("\t" + f ));

		log.trace("############################################### OS400 JOBS POST COMMANDS" );

		StringBuilder b = new StringBuilder();
		final List<String> users = new ArrayList<>();
		bmcjobs.forEach(j -> {
			BMCOS400Job os = (BMCOS400Job) j;

			String runas = os.getJobData().getRUNAS() == null ? "UNKNOWN" : os.getJobData().getRUNAS();

			if (!users.contains(runas)) {
				users.add(runas);
			}
		});

		users.stream().forEach(u -> {
			String newu = u;
			if (newu.length() > 9) {
				newu = newu.substring(0, 9);
			}

			b.append(u + "=$" + newu);
			b.append(",");
		});

		log.trace(b.toString());

	}

}
