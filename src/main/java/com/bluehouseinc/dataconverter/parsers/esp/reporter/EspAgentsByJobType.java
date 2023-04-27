package com.bluehouseinc.dataconverter.parsers.esp.reporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspDataModel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EspAgentsByJobType implements IReporter {
	EspDataModel espmodel;
	
	Map<String, List<String>> jobcount = new HashMap<>();

	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		espmodel = (EspDataModel) model;
		
		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());
		
		espmodel.getDataObjects().forEach(f -> doCount(f));

		jobcount.keySet().forEach(f ->{
			
			String key = f;
			
			log.trace("{}", key);
			
			jobcount.get(key).forEach(n ->{
				log.trace("\t{}", n);
			});
			

		});
		
		log.trace("#######################################{}#######################################", this.getClass().getSimpleName());
		
	}

	
	public void doCount(EspAbstractJob job) {

		String key = job.getClass().getSimpleName();
		String agent = job.getAgent();
		
		if(agent == null) {
			agent = "UNKNOWN";
		}
		
		if (jobcount.containsKey(key)) {
			if(!jobcount.get(key).contains(agent)) {
				jobcount.get(key).add(agent);
			}
		} else {
			List<String> data = new ArrayList<>();
			data.add(agent);
			jobcount.put(key, data);
		}

		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(c -> doCount((EspAbstractJob) c));
		}
	}

}
