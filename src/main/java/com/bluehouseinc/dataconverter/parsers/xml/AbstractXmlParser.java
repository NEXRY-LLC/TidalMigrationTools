package com.bluehouseinc.dataconverter.parsers.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bluehouseinc.dataconverter.common.exceptions.MethodNotImplementedException;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;

public abstract class AbstractXmlParser extends AbstractParser implements IXMLParser {

	public AbstractXmlParser(BaseParserDataModel parserDataModel) {
		super(parserDataModel);
	}

	@Override
	public void onNodeExplored(BaseParserDataModel dataModel, Node node, BaseCsvJobObject currentFolder, BaseCsvJobObject currentJob) throws Exception {
		throw new MethodNotImplementedException();
	}

	@Override
	public void parseFile() throws Exception {
		throw new MethodNotImplementedException();
	}


	/**
	 * Reads XML file by provided path
	 *
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	protected Document readXmlFile(String filePath) throws Exception {
		DocumentBuilderFactory factoryBuilder = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factoryBuilder.newDocumentBuilder();
		Document document = builder.parse(new File(filePath));

		document.getDocumentElement().normalize();

		// get root node
		Element root = document.getDocumentElement();
		System.out.println("ROOT NODE NAME: " + root.getNodeName());

		return document;
	}

	protected void traverse(BaseParserDataModel dataModel, NodeList nList, BaseCsvJobObject currentFolder, BaseCsvJobObject currentJob) throws Exception {
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				this.onNodeExplored(dataModel, node, currentFolder, currentJob);
			}
		}
	}

	protected void traverse(BaseParserDataModel dataModel, Node node, BaseCsvJobObject currentFolder, BaseCsvJobObject currentJob) throws Exception {
		this.traverse(dataModel, node.getChildNodes(), currentFolder, currentJob);
	}

	/**
	 * Get attribute value by name
	 *
	 * @param nodeMap
	 * @param name
	 * @return String | Null
	 */
	String getAttributeByName(NamedNodeMap nodeMap, String name) {
		for (int i = 0; i < nodeMap.getLength(); i++) {
			Node tempNode = nodeMap.getNamedItem(name);
			if (tempNode != null) {
				return tempNode.getNodeValue();
			}
		}

		return null;
	}

}
