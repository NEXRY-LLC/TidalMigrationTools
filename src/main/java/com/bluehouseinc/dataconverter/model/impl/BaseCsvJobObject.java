package com.bluehouseinc.dataconverter.model.impl;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.job.ConcurrentType;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.NearOutageType;
import com.bluehouseinc.tidal.utils.DependencyBuilder;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.google.api.client.util.Key;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import com.opencsv.bean.CsvRecurse;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * Abstract to forces implementation to set the type of job we are. Decided to use the api objects for this
 *
 * @author Brian Hayes
 */

@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public abstract class BaseCsvJobObject extends BaseJobOrGroupObject {
	// static final AtomicInteger count = new AtomicInteger(0);

	public abstract JobType getType();

	@Override
	public boolean isGroup() {
		return !this.getChildren().isEmpty();
	}

	/**
	 * Setup this object to belong to this container.
	 */
	// @CsvIgnore
	// protected String container; // Helper .. We will use this to set the jobs in a top level group or container
	// using this data.

	@CsvRecurse
	CsvOwner owner;

	@CsvRecurse
	CsvCalendar calendar;

	@CsvRecurse
	CsvRuntimeUser runtimeUser;

	@CsvRecurse
	CsvJobRerunLogic rerunLogic;

	@CsvBindByName
	String notes;

	@CsvRecurse
	CsvJobClass jobClass;

	@CsvBindByName
	String startTime;

	@CsvBindByName
	String endTime;

	@CsvBindByName
	Boolean requireOperatorRelease;

	@CsvBindByName
	String agentName;

	@CsvBindByName
	String agentListName;

	@CsvBindByName
	String environmentFile;

	@CsvBindByName
	String alternativeOutputFile;

	@CsvIgnore
	List<CsvVariable> variables = new ArrayList<>();

	@CsvIgnore
	List<CsvResource> resources = new ArrayList<>();

	@CsvRecurse
	CsvTimeZone timeZone;

	@CsvBindByName
	Boolean dependencyOrlogic = false; // If true then we are doing OR Logic, default is And

	@CsvBindByName
	Boolean operatorRelease = false;// Default

	@CsvBindByName
	Boolean inheritCalendar = false;// Default is no but if you want to force a job to inherit a group calenendar set this to yes.

	@CsvBindByName
	Boolean inheritAgent = false;// Default is no.

	@CsvBindByName
	String compoundDependency; // If this is set we will look up dependency objects by the ID's listed.

	@CsvBindByName
	Boolean disableCarryOver = false;// Default

	@CsvBindByName
	Integer calendarOffset;

	@CsvRecurse
	CsvJobTag tag;
	
	@CsvRecurse
	ConcurrentType concurrentIfActiveLogic = ConcurrentType.RUNANYWAY;
	
	@CsvRecurse
	NearOutageType nearOutagType = NearOutageType.RUNANYWAY;
	
	@CsvBindByName
	Integer maxRunTime;
	
	@Setter(value = AccessLevel.PRIVATE)
	DependencyBuilder compoundDependencyBuilder = new DependencyBuilder();

	@CsvBindByName
	YesNoType rerundependency;
	
	public void setCompoundDependency(String data) {
		if (StringUtils.isBlank(data)) {
			this.compoundDependency = null;
		} else {
			//this.compoundDependencyBuilder.expression = ExpressionParser.parse(data);
			this.compoundDependency = data;
		}
	}

	public CsvJobRerunLogic getRerunLogic() {
		if (this.rerunLogic == null) {
			this.rerunLogic = new CsvJobRerunLogic();
		}

		return this.rerunLogic;
	}
}
