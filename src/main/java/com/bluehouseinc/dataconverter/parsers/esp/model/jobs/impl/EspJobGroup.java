package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspAppEndData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data.EspLinkProcessData;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchEventElement;
import com.bluehouseinc.dataconverter.parsers.esp.model.statements.EspStatementObject;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspJobType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EspJobGroup extends EspAbstractJob {

	List<EspLinkProcessData> linkProcessDataList = new ArrayList<>();

	SchEventElement eventData;

	List<String> runtimes = new ArrayList<>();

	// Used to create loops and such.
	EspAppEndData applicationEndData;

	public EspJobGroup(String name) {
		super(name);
	}

	// String name; Name is defined in Base Object. this was root of breaking code.
	List<String> amNotifyList = new ArrayList<>();; // AMNOTIFY statement

	List<String> tags = new ArrayList<>();
	Map<String, String> variables = new HashMap<>();

	@Override
	public boolean isGroup() {
		return !this.children.isEmpty();
	}

	@Override
	public EspJobType getJobType() {
		return EspJobType.GROUP;
	}
	
	
	public void addEspStatementObject(EspStatementObject data) {
		this.getStatementObject().getEspAfterStatements().addAll(data.getEspAfterStatements());
		this.getStatementObject().getEspNoRunStatements().addAll(data.getEspNoRunStatements());
		this.getStatementObject().getEspNotWithStatements().addAll(data.getEspNotWithStatements());
		this.getStatementObject().getEspReleasedJobDependencies().addAll(data.getEspReleasedJobDependencies());
		this.getStatementObject().getEspRunStatements().addAll(data.getEspRunStatements());
		this.getStatementObject().getExitCodeStatements().addAll(data.getExitCodeStatements());
		this.getStatementObject().setPrerequisite(data.getPrerequisite());
	}
}
