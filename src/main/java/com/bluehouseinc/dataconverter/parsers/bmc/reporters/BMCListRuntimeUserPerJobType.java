package com.bluehouseinc.dataconverter.parsers.bmc.reporters;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCDataModel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BMCListRuntimeUserPerJobType implements IReporter {

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {
		BMCDataModel bmc = (BMCDataModel) model;

		log.trace("############################################### Run Time users per job type" );

		bmc.getJobtypes().keySet().stream().forEach(f -> {

			log.trace("Job Type [" + f.toString() + "]" );

			List<String> names = new ArrayList<>();

			bmc.getJobtypes().get(f).forEach(r -> {
				String rtename = r.getJobData().getRUNAS();

				if (!names.contains(rtename)) {
					names.add(rtename);
				}
			});

			names.forEach(n -> log.trace("\t" + n ));
		});

		log.trace("############################################### Run Time users per job type" );

	}
}
