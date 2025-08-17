package com.bluehouseinc.dataconverter.model.dependency;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;

import lombok.Data;

@Data
public class DependenciesRef {
	private List<Integer> jobIds = new ArrayList<>();
	private List<BaseCsvJobObject> jobs = new ArrayList<>();

	public void addJob(Integer jobId, BaseCsvJobObject job) {
		jobIds.add(jobId);
		jobs.add(job);
	}

	public boolean isEmpty() {
		return jobIds.isEmpty();
	}

	public int size() {
		return jobIds.size();
	}
}