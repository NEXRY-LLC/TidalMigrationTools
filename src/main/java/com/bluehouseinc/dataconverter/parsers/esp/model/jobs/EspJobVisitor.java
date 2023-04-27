package com.bluehouseinc.dataconverter.parsers.esp.model.jobs;

import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspAbstractJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAgentMonitorJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAixJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspAs400Job;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspDStrigJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspFileTriggerJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspFtpJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspLinuxJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspZosJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSAPBwpcJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSapEventJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSapJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSecureCopyJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspServiceMonitorJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspSftpJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspTextMonJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspUnixJob;
import com.bluehouseinc.dataconverter.parsers.esp.model.jobs.impl.EspWindowsJob;

// Represents "Visitor" interface
public interface EspJobVisitor {
	
	
	<E extends EspAbstractJob> void  doProcess(E job, List<String> lines);


}
