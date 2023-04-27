package com.bluehouseinc.dataconverter.parsers.orsyp;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.parsers.orsyp.model.ExportObjects;
import com.bluehouseinc.dataconverter.parsers.orsyp.model.Identifier;
import com.bluehouseinc.dataconverter.parsers.orsyp.model.Session;
import com.bluehouseinc.dataconverter.parsers.orsyp.model.SyntaxRules;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

public class OrsypParser extends AbstractParser<ExportObjects> {

	public OrsypParser(ConfigurationProvider cfgProvider) {
		super(new ExportObjects(cfgProvider));
	}

	@Override
	public void parseFile() throws Exception {
		String filePath = this.getParserDataModel().getConfigeProvider().getOrsypPath();
		FileInputStream is = new FileInputStream(filePath);

		XStream xstream = new XStream();
		// config
		xstream.addPermission(NoTypePermission.NONE); // forbid everything
		xstream.addPermission(NullPermission.NULL); // allow "null"
		xstream.addPermission(PrimitiveTypePermission.PRIMITIVES); // allow primitive types
		xstream.addPermission(AnyTypePermission.ANY);
		xstream.ignoreUnknownElements();
		xstream.setMode(XStream.NO_REFERENCES);
		// map classes
		xstream.alias("com.orsyp.ExportObjects", ExportObjects.class);
		xstream.alias("Session", Session.class);
		xstream.alias("SessionId", Identifier.class);
		xstream.alias("com.orsyp.api.syntaxerules.OwlsSyntaxRules", SyntaxRules.class);

		ObjectInputStream in = xstream.createObjectInputStream(is);

		in.readInt(); // first line
		ExportObjects data = (ExportObjects) in.readObject();

	}

	@Override
	public IModelReport getModelReporter() {
		return new OrsypReporter();
	}
}
