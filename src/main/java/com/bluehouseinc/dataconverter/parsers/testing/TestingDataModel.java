package com.bluehouseinc.dataconverter.parsers.testing;

import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.transform.ITransformer;

public class TestingDataModel extends BaseParserDataModel<TestingBaseObject,TestingConfigProvider> implements IParserModel {

	public TestingDataModel(ConfigurationProvider cfgProvider) {
		super(new TestingConfigProvider(cfgProvider));
	}

	@Override
	public void doProcessData(List<TestingBaseObject> dataObjects) {
	}

	@Override
	public BaseVariableProcessor<TestingBaseObject> getVariableProcessor(TidalDataModel model) {
		return null;
	}

	@Override
	public ITransformer<List<TestingBaseObject>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return null;
	}



}
