package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspAgentMonitorJob extends EspAbstractJob {

	int msgQLen;
	int statIntV;

	public EspAgentMonitorJob(String name) {
		super(name);
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.AGENT_MONITOR;
	}

}
