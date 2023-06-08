package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data;

import java.util.List;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspSetVarStatement;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspDataObjectJob extends EspAbstractJob {

	List<EspSetVarStatement> variables;

	public EspDataObjectJob(String name) {
		super(name);
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.DATA_OBJECT;
	}
}
