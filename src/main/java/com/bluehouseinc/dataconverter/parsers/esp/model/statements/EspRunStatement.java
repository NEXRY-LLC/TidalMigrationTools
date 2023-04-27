package com.bluehouseinc.dataconverter.parsers.esp.model.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This class is data model of ESP RUN statement which is used later to convert ESP Calendar scheduling into TIDAL Calendar scheduling.
 * More info at following link: shorturl.at/oABGU
 */

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EspRunStatement {

	public String criteria;
	public String jobName; // RUN REF - referencing other job by job name and reuses its run frequency setting(s)

	public EspRunStatement() {
	}
}
