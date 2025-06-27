package com.bluehouseinc.dataconverter.parsers.bmcmainframe;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.IReporter;

public class BMCMainFrameReporter implements IModelReport {

	@Override
	public List<IReporter> getReporters() {
		return new  ArrayList<IReporter>();
	}

}
