package com.bluehouseinc.dataconverter.parsers.tivoli.model;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.CpuData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.resource.ResourceData;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TivoliObject extends BaseJobOrGroupObject {
	private boolean isGroupFlag = false;
	
	private CpuData cpuData;
	private ResourceData resourceData;
	
	private String doCommand;
	private String streamLogon;
	private String description;
	private TaskType taskType;
	private String recovery;
	private String returnCodeSucess;
	private String scriptName;
	private String afterJob;
	private String abendPrompt;
	
	@Override
	public boolean isGroup() {
		if (!this.getChildren().isEmpty()) {
			isGroupFlag = true;
		}
		return isGroupFlag;
	}

	public enum TaskType {
		UNIX, WINDOWS, UNIT
	}
}
