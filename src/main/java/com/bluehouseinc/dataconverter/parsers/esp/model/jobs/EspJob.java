package com.bluehouseinc.dataconverter.parsers.esp.model.jobs;

import java.util.List;


// Represents "Element" interface
public interface EspJob {
	void processData(EspJobVisitor espJobVisitor, List<String> lines);
}
