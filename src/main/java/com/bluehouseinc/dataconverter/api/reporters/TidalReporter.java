package com.bluehouseinc.dataconverter.api.reporters;

import com.bluehouseinc.dataconverter.model.TidalDataModel;

public abstract class TidalReporter {


	public abstract void doReport(TidalDataModel model);

}
