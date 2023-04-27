package com.bluehouseinc.dataconverter.api.reporters;

import com.bluehouseinc.dataconverter.model.TidalDataModel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class NodeValidator extends TidalReporter {

	@Override
	public void doReport(TidalDataModel model) {

		log.trace("Total Agents To Process [" + model.getNodes().size() + "]");

		log.trace(" Total Agents Processed [" + model.getNodes().size() + "] ");

		model.getNodes().stream().forEach(s -> log.trace(s));

		log.trace(" Total Agents Processed [" + model.getNodes().size() + "] ");
	}

	// /**
	// * Recurse all data.
	// * @param data
	// * @param cur
	// * @return
	// */
	// private int getTotalJobsGroups(List<AbstractJobOrGroup> data, List<Calendar> cals) {
	//
	// for (AbstractJobOrGroup d : data) {
	// if (d instanceof JobGroup) {
	// cur++;
	// getTotalJobsGroups(((JobGroup) d).getChildren(), cur);
	// } else {
	// cur++;
	// }
	// }
	//
	// return cur;
	// }
}
