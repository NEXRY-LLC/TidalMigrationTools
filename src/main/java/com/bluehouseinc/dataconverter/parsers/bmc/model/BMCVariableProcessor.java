package com.bluehouseinc.dataconverter.parsers.bmc.model;

import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;

public class BMCVariableProcessor extends BaseVariableProcessor<BaseBMCJobOrFolder> {

	public BMCVariableProcessor(TidalDataModel model) {
		super(model);
	}

	@Override
	public void processJobVariables(BaseBMCJobOrFolder job) {

	}

}
