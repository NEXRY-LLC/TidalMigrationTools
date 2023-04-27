package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Application implements Serializable {
	private String applicationId;
	private CommonData commonData = new CommonData();
	private List<OperationData> operations = new ArrayList<>();
	private List<InternalOperationLogic> internalOperationLogic = new ArrayList<>();
	private List<RuleBasedRunCycleInformation> ruleBasedRunCycleInformation = new ArrayList<>();

	public Application(String applicationId) {
		this.applicationId = applicationId;
	}
}
