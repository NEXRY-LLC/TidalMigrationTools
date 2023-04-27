package com.bluehouseinc.dataconverter.api.reporters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AgentTypeAndJobCountValidator extends TidalReporter {

	Map<String, JobType> cnt = new HashMap<>();


	@Override
	public void doReport(TidalDataModel model) {

		log.trace(" Total Jobs By Type and Agent");

		model.getModelJobs().forEach(f -> doCount(f));

		for (String type : cnt.keySet()) {

			log.trace("Job Type[" + type + "]");

			for (String agt : cnt.get(type).agt.keySet()) {
				int tt = cnt.get(type).agt.get(agt).size();
				log.trace("\tAgent Name [" + agt + "] Total Jobs[" + tt + "]");
			}

		}

		log.trace(" Total Jobs By Type and Agent");
	}

	private void doCount(BaseCsvJobObject obj) {

		String key = obj.getClass().getTypeName();

		if (cnt.containsKey(key)) {
			cnt.get(key).addJobToAgent(obj.getAgentName(), obj.getName());
		} else {
			JobType t = new JobType(key);
			t.addJobToAgent(obj.getAgentName(), obj.getName());
			cnt.put(key, t);
		}

		if (!obj.getChildren().isEmpty()) {
			obj.getChildren().forEach(f -> doCount((BaseCsvJobObject) f));
		}
	}

	public class JobType {

		private String name;

		private Map<String, List<String>> agt = new HashMap<>();

		public JobType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void addJobToAgent(String agent, String job) {
			if (agt.containsKey(agent)) {
				agt.get(agent).add(job);
			} else {
				List<String> jobs = new ArrayList<>();
				jobs.add(job);

				agt.put(agent, jobs);
			}
		}

		@Override
		public boolean equals(Object obj) {
			return this.getName() == ((JobType) obj).getName();
		}

		@Override
		public String toString() {
			return this.getName();
		}
	}

}
