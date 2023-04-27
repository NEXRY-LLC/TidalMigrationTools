package com.bluehouseinc.dataconverter.parsers.xml;

import org.w3c.dom.Node;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.parsers.IParser;

public interface IXMLParser extends IParser {

	void onNodeExplored(BaseParserDataModel dataModel, Node node, BaseCsvJobObject currentFolder, BaseCsvJobObject currentJob) throws Exception;
}
