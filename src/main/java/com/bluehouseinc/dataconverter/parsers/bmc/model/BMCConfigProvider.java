package com.bluehouseinc.dataconverter.parsers.bmc.model;

import java.io.File;

import com.bluehouseinc.dataconverter.model.AbstractConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

public class BMCConfigProvider extends AbstractConfigProvider {

	private static final String BMC_GLOBALVARPREFIX = "BMC.GlobalVariablePrefixes";
	private static final String BMC_INCLUDECONDITIONS = "BMC.IncludeConditions";

	private static final String BMC_INCLUDEQUANTIATIVERESOURCES = "BMC.IncludeQuantiativeResources";
	private static final String BMC_INCLUDEQUANTIATIVERESOURCESCOUNT = "BMC.QuantiativeResourcesCount";
	private static final String BMC_INCLUDEQUANTIATIVERESOURCESPREFIX = "BMC.QuantiativeResourcePrefix";

	private static final String BMC_INCLUDECONTROLRESOURCES = "BMC.IncludeControlResources";
	private static final String BMC_INCLUDECONTROLRESOURCESPREFIX = "BMC.ControlResourcePrefix";
	private static final String BMC_INCLUDECONTROLRESOURCESCOUNT = "BMC.ControlResourceCount";
	private static final String BMC_ConvertTimeSeqToTidalRerun = "BMC.ConvertTimeSeqToTidalRerun";

	private static final String BMC_APPENDJOBTYPETOAGENT = "BMC.AppendJobTypeToAgentName";

	private static final String BMC_AccountFileFolder = "BMC.AccountFileFolder";
	private static final String BMC_IncludeEmailActions = "BMC.IncludeEmailActions";

	private static final String BMC_ReportDoSetVarData = "BMC.ReportDoSetVarData";

	private static final String BMCGroupByDataCenter = "BMC.GroupByDataCenter";
	private static final String BMCGroupByApplication = "BMC.GroupByApplication";
	private static final String BMCProcessPreAndPostData = "BMC.ProcessPreAndPostData";
	private static final String BMCIncludeDisabledJobs = "BMC.IncludeDisabledJobs";

	public BMCConfigProvider(ConfigurationProvider provider) {
		super(provider);
	}

	@Override
	public void checkAndThrowEarly() throws TidalException {
		getBMCFilePath();
	}

	public File getBMCFilePath() {
		return new File(getProvider().getOrThrow("bmc.path"));

	}

	public String getBMCGlobalPrefixes() {
		return this.getProvider().getConfigurations().getOrDefault(BMC_GLOBALVARPREFIX, null);
	}

	public String getAccountFolder() {
		return this.getProvider().getConfigurations().getOrDefault(BMC_AccountFileFolder, null);
	}


	public boolean includeConditions() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMC_INCLUDECONDITIONS, "false");
		return Boolean.valueOf(tf);
	}

	public boolean appendJobTypeToAgentName() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMC_APPENDJOBTYPETOAGENT, "true");
		return Boolean.valueOf(tf);
	}

	public boolean convertTimeSeqToTidalRerun() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMC_ConvertTimeSeqToTidalRerun, "false");
		return Boolean.valueOf(tf);
	}

	public boolean includeQauntitativeResources() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMC_INCLUDEQUANTIATIVERESOURCES, "false");
		return Boolean.valueOf(tf);
	}

	public String qauntitativeResourcePrefix() {
		return this.getProvider().getConfigurations().getOrDefault(BMC_INCLUDEQUANTIATIVERESOURCESPREFIX, "");
	}

	public int qauntitativeResourceCount() {
		return Integer.valueOf(this.getProvider().getConfigurations().getOrDefault(BMC_INCLUDEQUANTIATIVERESOURCESCOUNT, "1"));
	}

	public boolean includeControlResources() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMC_INCLUDECONTROLRESOURCES, "false");
		return Boolean.valueOf(tf);
	}

	public String controlResourcePrefix() {
		return this.getProvider().getConfigurations().getOrDefault(BMC_INCLUDECONTROLRESOURCESPREFIX, "");
	}

	public int controlResourceCount() {
		return Integer.valueOf(this.getProvider().getConfigurations().getOrDefault(BMC_INCLUDECONTROLRESOURCESCOUNT, "1"));
	}

	public boolean includeEmailActions() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMC_IncludeEmailActions, "false");
		return Boolean.valueOf(tf);
	}

	public String reportDoSetVarDataLocation() {
		return this.getProvider().getConfigurations().getOrDefault(BMC_ReportDoSetVarData, null);
	}

	public boolean groupByDataCenter() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMCGroupByDataCenter, "false");
		return Boolean.valueOf(tf);
	}

	public boolean groupByApplication() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMCProcessPreAndPostData, "true");
		return Boolean.valueOf(tf);
	}

	public boolean processPreandPost() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMCProcessPreAndPostData, "false");
		return Boolean.valueOf(tf);
	}

	public boolean processDisabledJobs() {
		String tf = this.getProvider().getConfigurations().getOrDefault(BMCIncludeDisabledJobs, "true");
		return Boolean.valueOf(tf);
	}

}
