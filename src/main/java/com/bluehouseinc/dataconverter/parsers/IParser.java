package com.bluehouseinc.dataconverter.parsers;


import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.TidalDataModel;


public interface IParser {

	TidalDataModel processDataModel() throws Exception;

	public IModelReport getModelReporter();


}
