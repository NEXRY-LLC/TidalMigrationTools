package com.bluehouseinc.dataconverter.api.reporters;

import java.util.List;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class JobCountValidator extends TidalReporter {

	@Override
	public void doReport(TidalDataModel model) {

		long flatcount = model.getTotalJobGroupCount();

		log.trace("Total Jobs and Groups To Process Flat List[" + flatcount + "]");

		// getTotalJobsGroups(model.getJobOrGroups(), 0);
		// model.getJobOrGroups().stream().flatMap( parents ->
		// parents.getChildren().stream()).collect(Collectors.toList()).size();

		log.trace(" Total Jobs and Groups Processed [" + flatcount + "] ");

		int total = getTotalJobsGroups(model.getModelJobs(), 0);
		log.trace(" Total Jobs and Groups Processed [" + total + "] ");

	}

	/**
	 * Recurse all data.
	 *
	 * @param data
	 * @param cur
	 * @return
	 */
	private int getTotalJobsGroups(List<BaseCsvJobObject> data, int cur) {

		Integer count = dumpList(cur, data);
		// log.info(String.format("Returning count - %s\n", count));
		return count;
	}

	private static Integer dumpList(Integer count, Iterable<BaseCsvJobObject> list) {

		for (BaseCsvJobObject item : list) {

			if (!item.getChildren().isEmpty()) {

				count += dumpList(1, item.getChildren());

			} else {
				++count;
			}
			// System.out.println(String.format("%d - %s", count, item));
		}
		// System.out.println(String.format("Returning count - %s", count));
		return count;
	}

}
