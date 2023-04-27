package com.bluehouseinc.dataconverter.parsers.autosys.model.jobs.util;

/**
 * b/BOX = group or box, c/CMD = command line, f/FW = FileWatcher, FT = FileTrigger, OMS=Windows Service Monitoring
 * NOTE: Lowercase enum constants are used for backward compatibility of old versions of AUTOSYS WA system. Therefore, use them as well in code. More info at:
 * https://techdocs.broadcom.com/us/en/ca-enterprise-software/intelligent-automation/autosys-workload-automation/12-0-01/reference/ae-job-information-language/jil-job-definitions/job-type-attribute-specify-job-type.html
 */
public enum AutosysJobType {
	b, BOX, c, CMD, f, FW, FT, OMS, SQLAGENT
}
