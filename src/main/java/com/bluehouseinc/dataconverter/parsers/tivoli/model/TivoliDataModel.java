package com.bluehouseinc.dataconverter.parsers.tivoli.model;


import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.parsers.tivoli.TivoliConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.transform.ITransformer;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TivoliDataModel extends BaseParserDataModel<TivoliObject,TivoliConfigProvider> implements IParserModel {

	public TivoliDataModel(ConfigurationProvider cfgProvider) {
		super(new TivoliConfigProvider(cfgProvider));
	}

	@Override
	public BaseVariableProcessor<TivoliObject> getVariableProcessor(TidalDataModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Process the concret object that is bound
	 */
	@Override
	public void doProcessData(List<TivoliObject> dataObjects) {

	}

	@Override
	public ITransformer<List<TivoliObject>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return null;
	}

}
