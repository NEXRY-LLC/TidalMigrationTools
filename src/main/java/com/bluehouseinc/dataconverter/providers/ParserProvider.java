package com.bluehouseinc.dataconverter.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.exceptions.ParserNotSupportedException;
import com.bluehouseinc.dataconverter.parsers.IParser;
import com.bluehouseinc.dataconverter.parsers.autosys.AutosysParser;
import com.bluehouseinc.dataconverter.parsers.bmc.BMCParser;
import com.bluehouseinc.dataconverter.parsers.esp.EspParser;
import com.bluehouseinc.dataconverter.parsers.opc.OPCParser;
import com.bluehouseinc.dataconverter.parsers.orsyp.OrsypParser;
import com.bluehouseinc.dataconverter.parsers.testing.TestingParser;
import com.bluehouseinc.dataconverter.parsers.tivoli.TivoliParser;

@Component
public class ParserProvider {

	ConfigurationProvider cfgProvider;

	@Autowired
	public void setConfigurationProvider(ConfigurationProvider cfgProvider) {
		this.cfgProvider = cfgProvider;
	}

	public IParser getParser() throws ParserNotSupportedException {

		String migrationType = this.cfgProvider.getConfigurations().getOrDefault(MigrationDataTypes.MigrationDataType, null);

		if (migrationType == null) {
			throw new ParserNotSupportedException();
		}

		MigrationDataTypes type = MigrationDataTypes.valueOf(migrationType);

		switch (type) {
		case BMC:
			return new BMCParser(this.cfgProvider);
		case OPC:
			return new OPCParser(this.cfgProvider);
		case AUTOSYS:
			return new AutosysParser(this.cfgProvider);
		case TIVOLI:
			return new TivoliParser(this.cfgProvider);
		case ORSYP:
			return new OrsypParser(this.cfgProvider);
		case TESTING:
			return new TestingParser(this.cfgProvider);
		case ESP:
			return new EspParser(this.cfgProvider);
		default:
			throw new ParserNotSupportedException();
		}
	}

	public enum MigrationDataTypes {
		BMC, OPC, CSV, AUTOSYS, TIVOLI, ORSYP, ESP, TESTING;

		public final static String MigrationDataType = "MigrationDataType";
	}
}
