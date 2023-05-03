package com.bluehouseinc.dataconverter.parsers.esp.model.jobs;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;

// Represents "Visitor" interface
public interface EspJobVisitor {


	<E extends EspAbstractJob> void  doProcess(E job, List<String> lines);


}
