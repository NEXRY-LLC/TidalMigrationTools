package com.bluehouseinc.dataconverter.parsers.bmc.reporters;

import java.util.List;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCDataModel;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.util.ObjectUtils;

import lombok.extern.log4j.Log4j2;

/**
 * In BMC the IN/OUT CON logic is rerun when , so in BMC you can have job A Rerun every X times and it sets an OUTCON
 * Jobs B and C are not setup as rerunning jobs but just dependent on Job A , E.G a INCON , so each time A runs and sets the OUTCON
 * Jobs B and C will rerun. TIDAL is similar but design wise these need to be reviewed as there are cases where you want to setup to have
 * Jobs B and C have matching instances in JAC for clerity.
 *
 * @author Brian Hayes
 *
 */
@Log4j2
public class BMCListRerunningJobs implements IReporter {


	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		BMCDataModel bmc = (BMCDataModel) model;

		log.trace("############################################### RERUN JOBS FOR REVIEW");
		List<BaseBMCJobOrFolder> bmcjobs = ObjectUtils.toFlatStream(bmc.getDataObjects()).collect(Collectors.toList()).stream().filter(f -> f.getRunTimeInfo().isRerunningJob()).collect(Collectors.toList());

		// bmcjobs = bmcjobs.filter(f -> f.getRunTimeInfo().isRerunningJob());

		log.trace("Total Rerunning/cyclic Jobs: " + bmcjobs.size() );

		bmcjobs.forEach(f -> log.trace("\t" + f.getFullPath()));

		log.trace("############################################### RERUN JOBS FOR REVIEW");

	}

}
