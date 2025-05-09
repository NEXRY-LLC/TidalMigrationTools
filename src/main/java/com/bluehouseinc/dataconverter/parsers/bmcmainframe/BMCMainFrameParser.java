package com.bluehouseinc.dataconverter.parsers.bmcmainframe;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;

public class BMCMainFrameParser extends AbstractParser<BMCMainFrameDataModel>{

	public BMCMainFrameParser(ConfigurationProvider parserDataModel) {
		super(new BMCMainFrameDataModel(parserDataModel));
		// TODO Auto-generated constructor stub
	}

	@Override
	public IModelReport getModelReporter() {
		return new BMCMainFrameReporter();
	}

	@Override
	protected void parseFile() throws Exception {
		//FINISHME: This is where you start your parsing of a new data file. 
	}
	
}
