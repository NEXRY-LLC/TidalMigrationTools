package com.bluehouseinc.dataconverter.parsers.reporters;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class GenericJobTypeReporter implements IReporter {

	Map<String, AtomicInteger> jobcount = new HashMap<>();

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

		model.getDataObjects().forEach(f -> doCount(f));

		jobcount.keySet().forEach(f ->{

			String key = f;

			int cnt = jobcount.get(key).get();

			log.trace("{} = {}", key, cnt);

		});

		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());

	}


	public void doCount(BaseJobOrGroupObject job) {

		String key = job.getClass().getSimpleName();

		if (jobcount.containsKey(key)) {
			jobcount.get(key).incrementAndGet();
		} else {
			jobcount.put(key, new AtomicInteger(1));
		}

		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(c -> doCount(c));
		}
	}

}
