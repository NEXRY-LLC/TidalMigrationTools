package com.bluehouseinc.transform;

public interface ITransformer<IN, OUT> {

	public OUT transform(IN in) throws TransformationException;

}
