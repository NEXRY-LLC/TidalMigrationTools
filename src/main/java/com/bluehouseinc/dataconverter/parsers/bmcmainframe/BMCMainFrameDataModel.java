package com.bluehouseinc.dataconverter.parsers.bmcmainframe;

import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.transform.ITransformer;

public class BMCMainFrameDataModel extends BaseParserDataModel<BMCMainFrameJobOrGroup, BMCMainFrameConfigProvider> {

	public BMCMainFrameDataModel(ConfigurationProvider configeProvider) {
		super(new BMCMainFrameConfigProvider(configeProvider));
	}

	@Override
	public void doPostTransformJobObjects(List<BMCMainFrameJobOrGroup> jobs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doProcessJobDependency(List<BMCMainFrameJobOrGroup> jobs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BaseVariableProcessor<BMCMainFrameJobOrGroup> getVariableProcessor(TidalDataModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITransformer<List<BMCMainFrameJobOrGroup>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		// TODO Auto-generated method stub
		return null;
	}

}
