package com.bluehouseinc.dataconverter.parsers.bmc.model.jobs;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.parsers.bmc.model.BMCJobTypes;
import com.bluehouseinc.dataconverter.parsers.bmc.model.JobRunTimeInfo;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.CaptureData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.ControlResourceData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoActionData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoCondData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoForceJobData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoMailData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoSetVarData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoShoutData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoSysoutData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.InConditionData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.JobData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.JobTagData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.OnData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.OutConditionData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.QuantitativeResourceData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.SetVarData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.ShoutData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.TagData;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.modified.RuleBasedCalendar;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Brian Hayes
 *
 */

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, doNotUseGetters = false)
public abstract class BaseBMCJobOrFolder extends BaseJobOrGroupObject {

	JobData jobData;

	/*
	 * We do not want people accessing this directly, need to use helper method.
	 */
	/**
	 * There are two types of variables in the data.. ones specific to the job type and others are local variables on
	 * the job.
	 * The XML doesn't show this difference so we need to rely on each job to tell us the prefix for job specific.
	 */
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	List<SetVarData> jobSpecificVariables = new ArrayList<>();

	/*
	 * This data is specific to jobs, where a job has variables that is used at runtime not specific to the job type .
	 */
	/**
	 * This data is localized variables to the job or group.. From what I know right now (March 11, 20220) only smart
	 * folders,
	 * subfolders and OS jobs can have these variables. OS jobs use them in BMC as environment variables at runtime.
	 */
	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PROTECTED)
	List<SetVarData> localJobVariables = new ArrayList<>();

	List<QuantitativeResourceData> quantitativeResourceData = new ArrayList<>();
	List<DoActionData> doActionData = new ArrayList<>();
	List<CaptureData> captureData = new ArrayList<>();
	List<DoMailData> doMailData = new ArrayList<>();
	List<DoShoutData> doShoutData = new ArrayList<>();
	List<ShoutData> shoutData = new ArrayList<>();
	List<TagData> tagData = new ArrayList<>();
	List<JobTagData> jobTagData = new ArrayList<>();
	List<DoForceJobData> doForceJobData = new ArrayList<>();
	List<DoSysoutData> doSysoutData = new ArrayList<>();
	List<DoCondData> doCondData = new ArrayList<>();
	List<ControlResourceData> controlResourceData = new ArrayList<>();
	List<RuleBasedCalendar> ruleBasedCalendar = new ArrayList<>();
	List<InConditionData> inConditionData = new ArrayList<>();
	List<OutConditionData> outConditionData = new ArrayList<>();
	List<OnData> onData = new ArrayList<>();
	List<DoSetVarData> doSetVarData = new ArrayList<>();

	JobRunTimeInfo runTimeInfo;
	BMCJobTypes bmcJobType;

	public BMCJobTypes getBMCJobType() {
		throw new RuntimeException("This is a BMC Base Job, do not call this method ");
	}

	public String getPlaceHolderData() {
		StringBuilder b = new StringBuilder();

		this.getJobSpecificVariables().stream().forEach(data -> {

			b.append(data.getNAME());
			b.append("=");
			b.append(data.getVALUE());
			b.append("\n");
		});

		return b.toString();
	}

	/**
	 * Helper Method for our var data as every job type will have a different need
	 * for this. 1, Adds the element to the List of values and calls doProcesVarData
	 * for our downstream classes. We also capture out the pre and post command
	 *
	 * @param data
	 */
	public void addSetVarData(SetVarData data) {

		String pre = getVarPrefix();
		if (!StringUtils.isBlank(pre) && data.getNAME().startsWith(pre)) {
			this.jobSpecificVariables.add(data);
			doProcesJobSpecificVarData(data);
		} else {
			this.localJobVariables.add(data);
		}

	}

	public List<SetVarData> getLocalVariables() {
		return this.localJobVariables;
	}

	public List<SetVarData> getJobSpecificVariables() {
		return this.jobSpecificVariables;
	}

	/**
	 * Job specific var's..
	 *
	 * @param data
	 */
	public abstract void doProcesJobSpecificVarData(SetVarData data);

	/**
	 * We need to know the job specific prefix in order to not include them in our local job related variables.
	 *
	 * @return
	 */
	public abstract String getVarPrefix();

	public String getNodeName() {

		String node = this.jobData.getNODEID();

		if (node == null) {
			BaseBMCJobOrFolder curparent = (BaseBMCJobOrFolder) this.parent;

			while (curparent != null) {
				node = curparent.getNodeName();// .toLowerCase();

				if (node != null) {
					break;
				}

				curparent = (BaseBMCJobOrFolder) curparent.parent;
			}

		}

		node = node == null ? "UNKNOWN" : node;
		return node;
	}

	@Override
	public boolean isGroup() {
		return !this.getChildren().isEmpty();
	}

}
