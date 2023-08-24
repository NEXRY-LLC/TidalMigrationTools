package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on;

public class RunOnRequest extends RunOn {
	// ITS NOTHING BUT STATING ON DEMAND
	
	@Override
	public RunOnType getRunOnType() {
		return RunOnType.REQUEST;
	}
}
