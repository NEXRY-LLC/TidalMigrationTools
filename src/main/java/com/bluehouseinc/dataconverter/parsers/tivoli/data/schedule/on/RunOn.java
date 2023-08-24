package com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on;

import lombok.Data;

//ON RUNCYCLE APTMON DESCRIPTION "10th of Month" "FREQ=MONTHLY;INTERVAL=1;BYMONTHDAY=10"
//ON RUNCYCLE CALENDAR1 LN4THWD
//ON RUNCYCLE RULE1 "FREQ=WEEKLY;BYDAY=SA"
//ON RUNCYCLE RULE2 "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;"

public abstract class RunOn {

	RunOnType type;


	public abstract RunOnType getRunOnType();
}
