package com.bluehouseinc.dataconverter.api.reporters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class JobByTypeValidator extends TidalReporter {
	Map<String, List<BaseCsvJobObject>> cnt = new HashMap<>();

	@Override
	public void doReport(TidalDataModel model) {

		log.trace(" Total Jobs By Type  Total Jobs[" + model.getTotalJobGroupCount() + "] ");

		model.getModelJobs().forEach(f -> doCount(f));

		for (String key : cnt.keySet()) {
			int counter = cnt.get(key).size();
			log.trace("JOB TYPE[" + key + "] COUNT[" + counter + "]");

		}
		log.trace(" Total Jobs By Type  Total Jobs[" + model.getTotalJobGroupCount() + "] ");
	}

	private void doCount(BaseCsvJobObject obj) {

		String key = obj.getClass().getTypeName();

		if (cnt.containsKey(key)) {

			cnt.get(key).add(obj);
		} else {
			List<BaseCsvJobObject> l = new ArrayList<>();
			l.add(obj);
			cnt.put(key, l);
		}

		if (!obj.getChildren().isEmpty()) {
			obj.getChildren().forEach(f -> doCount((BaseCsvJobObject) f));
		}
	}
}
