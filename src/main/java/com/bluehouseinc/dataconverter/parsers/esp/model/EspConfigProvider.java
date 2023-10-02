package com.bluehouseinc.dataconverter.parsers.esp.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.importers.AbstractCsvImporter;
import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.model.csv.NameValuePairCsvMapping;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.parsers.esp.CsvMappingExcludeJobsInGroup;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EspConfigProvider extends AbstractConfigProvider {
	private static String DATAFILE = "esp.event.datafile";
	private static String EXCLUDE_FILE = "esp.exclude.job.datafile";

	public EspConfigProvider(ConfigurationProvider provider) {
		super(provider);

	}

	@Override
	public void checkAndThrowEarly() throws TidalException {
		getEspDataPath();
		getEspEventDataFile();
	}

	public String getEspDataPath() {
		return getProvider().get("esp.path");
	}

	public String getEspEventDataFile() {
		return getProvider().get(DATAFILE);
	}

	public String getFilesToSkip() {
		return getProvider().getOr("esp.skipfiles", null);
	}

	public String getFilesToInclude() {
		return getProvider().getOr("esp.includefiles", null);
	}

	public String getZosLibPath() {
		return getProvider().get("esp.lib.path");
	}

	public String getZosLibDefaultAgent() {
		return getProvider().get("esp.agent.name");
	}

	public String getZosLibDefaultRuntimeUser() {
		return getProvider().get("esp.zos.runtime.user");
	}

	public String getAS400DefaultRuntimeUser() {
		return getProvider().get("esp.as400.runtime.user");
	}

	public String getAS400RuntimeUserAgentMapData() {
		return getProvider().get("esp.as400.runtime.agentmap");
	}

	public String getEspMailListDataPath() {
		return getProvider().get("esp.mail.datafile");
	}

	public String getEspMailSubject() {
		return getProvider().get("esp.mail.subject");
	}

	public String getEspMailBody() {
		return getProvider().get("esp.mail.body");
	}

	public String getAS400RuntimeUserByAgentName(String node) {

		String mapdata = getAS400RuntimeUserAgentMapData();

		if (StringUtils.isBlank(node) || StringUtils.isBlank(mapdata)) {
			return getAS400DefaultRuntimeUser();
		}

		String newruntime = ObjectUtils.replaceWithData(node, mapdata, true);

		if (StringUtils.isBlank(newruntime)) {
			return getAS400DefaultRuntimeUser();
		}

		return newruntime;
	}

	public String getEspExcludeJobFile() {
		return getProvider().getConfigurations().getOrDefault(EXCLUDE_FILE, null);
	}

	List<String> excludeJobs = null;

	public boolean isExcludedJobFromGroup(String groupname, String jobname) {

		String vardatafile = getEspExcludeJobFile();

		if (vardatafile != null) {

			final String lower_groupname = groupname.trim().toLowerCase();
			final String lower_jobname = jobname.trim().toLowerCase();
			final String searchKey = lower_groupname + "/" + lower_jobname;

			File file = new File(vardatafile);

			log.debug("Loading Exclude Data File for Processing[" + vardatafile + "]");

			if (excludeJobs == null) {
				this.excludeJobs = new ArrayList<>();
				AbstractCsvImporter.fromFile(file, CsvMappingExcludeJobsInGroup.class).forEach(f -> {

					String datakey = f.getGroupName().trim().toLowerCase() + "/" + f.getJobName().trim().toLowerCase();
					
					if (this.excludeJobs.contains(datakey)) {
						log.error("isExcludedJobFromGroup ERROR is duplicate in data,Group{} Job{} ", jobname, groupname);
					} else {
						this.excludeJobs.add(datakey);
					}
				});

				System.out.println("isExcludedJobFromGroup total records {"+this.excludeJobs.size()+"}");
			}

			boolean excludejob = this.excludeJobs.contains(searchKey);

			if (excludejob) {
				log.debug("isExcludedJobFromGroup true, excluding Job{} Group{}", groupname, jobname);
				return true;
			}

		}

		return false;

	}

}
