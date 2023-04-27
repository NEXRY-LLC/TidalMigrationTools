package com.bluehouseinc.dataconverter.model;

/**
 * Not sure what to call this one as I need to report on stuff converted and so validation, to me it's both in one.
 *
 * @author Brian Hayes
 *
 */
public interface IReporter {

	<B extends BaseParserDataModel<?,?>> void doPrint(B model);

}
