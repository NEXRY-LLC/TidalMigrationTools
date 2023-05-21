package com.bluehouseinc.dataconverter.parsers.tivoli.model;

import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;

public class TivoliVariableProcessor  extends BaseVariableProcessor<TivoliObject>{

	public TivoliVariableProcessor(TidalDataModel model) {
		super(model);
	}

	@Override
	public void processJobVariables(TivoliObject job) {

	}

}
