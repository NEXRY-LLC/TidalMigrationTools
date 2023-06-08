package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspLIEData extends EspAbstractJob {



	public EspLIEData(String name) {
		super(name);
	}



	@Override
	public EspJobType getJobType() {
		return EspJobType.LIE;
	}

}
