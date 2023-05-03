package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspAppEndData;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.EspLinkProcessData;
import com.bluehouseinc.dataconverter.parsers.esp.model.schedule.SchEventElement;
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
	List<String> amNotifyList; // AMNOTIFY statement
	List<String> espJobGroupNotifyList;

	List<String> tags;
	Map<String, String> variables;

	@Override
	public boolean isGroup() {
		return !this.children.isEmpty();
	}

	@Override
	public EspJobType getJobType() {

		return EspJobType.GROUP;
	}
}
