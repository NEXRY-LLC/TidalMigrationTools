package com.bluehouseinc.dataconverter.parsers.tivoli.model;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TivoliObject extends BaseJobOrGroupObject {
	private boolean isGroupFlag = false;
	private String doCommand;
	private String streamLogon;
	private String description;
	private TaskType taskType;
	private String recovery;

	@Override
	public boolean isGroup() {
		if (!this.getChildren().isEmpty()) {
			isGroupFlag = true;
		}
		return isGroupFlag;
	}

	public enum TaskType {
		UNIX, WINDOWS
	}
}
