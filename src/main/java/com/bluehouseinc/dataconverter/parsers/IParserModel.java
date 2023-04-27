package com.bluehouseinc.dataconverter.parsers;

import com.bluehouseinc.dataconverter.model.TidalDataModel;

public interface IParserModel {
	TidalDataModel convertToDomainDataModel();
}
