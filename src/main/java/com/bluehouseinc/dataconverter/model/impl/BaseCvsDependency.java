package com.bluehouseinc.dataconverter.model.impl;

import java.util.concurrent.atomic.AtomicInteger;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class BaseCvsDependency {

	@Getter(value = AccessLevel.NONE)
	@Setter(value = AccessLevel.NONE)
	private static final AtomicInteger count = new AtomicInteger(Integer.MAX_VALUE);

	@CsvIgnore
	private int id;

	BaseCvsDependency(){
		id = count.decrementAndGet();
	}

	public String getIdString() {
		return Integer.toString(this.id);
	}

	@CsvIgnore
	BaseCsvJobObject jobObject;

	@CsvBindByName
	@Getter(value = AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private int jobObjectid;

	public void setJobObject(BaseCsvJobObject job) {
		this.jobObject = job;
		this.jobObjectid = job.getId();
	}
}
