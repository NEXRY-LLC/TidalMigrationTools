package com.bluehouseinc.dataconverter.parsers.tivoli.model;

import java.util.List;

import com.bluehouseinc.dataconverter.model.TidalDataModel;

import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

public class TivoliTransformer implements ITransformer<List<TivoliObject>, TidalDataModel>{

	TidalDataModel datamodel;

	public TivoliTransformer(TidalDataModel datamodel) {
		this.datamodel = datamodel;
	}
		
	@Override
	public TidalDataModel transform(List<TivoliObject> in) throws TransformationException {

		return datamodel;
	}

}
