package com.bluehouseinc.dataconverter.parsers.autosys.model.job_dependencies;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class AutosysBaseDependency {

	@Getter(value = AccessLevel.PRIVATE)
	private static final AtomicInteger count = new AtomicInteger(Integer.MAX_VALUE);

	@Getter(value = AccessLevel.PUBLIC)
	@Setter(value = AccessLevel.NONE)
	@EqualsAndHashCode.Include
	private int id;

	String dependencyName;

	public AutosysBaseDependency(String dependencyName) {
		this.id = count.decrementAndGet();
		this.dependencyName = dependencyName;
	}
}
