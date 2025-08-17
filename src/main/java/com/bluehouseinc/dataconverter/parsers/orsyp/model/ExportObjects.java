package com.bluehouseinc.dataconverter.parsers.orsyp.model;

import java.util.LinkedList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.parsers.orsyp.OrsypConfigProvider;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.transform.ITransformer;

public class ExportObjects extends BaseParserDataModel<BaseJobOrGroupObject, OrsypConfigProvider> {
	LinkedList<Sessions> sessions;
	LinkedList<Triggers> triggers;
	LinkedList<Runbooks> runbooks;
	LinkedList<Calendars> calendars;
	LinkedList<BusinessViews> businessViews;
	LinkedList<JobChains> jobChains;
	LinkedList<JobEvents> jobEvents;
	LinkedList<DqmJobs> dqmJobs;
	private String _id;

	@Override
	public BaseVariableProcessor<BaseJobOrGroupObject> getVariableProcessor(TidalDataModel model) {
		return null;
	}

	@Override
	public ITransformer<List<BaseJobOrGroupObject>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return null;
	}

	public ExportObjects(ConfigurationProvider cfgProvider) {
		super(new OrsypConfigProvider(cfgProvider));
	}

	// Getter Methods

	public LinkedList<Sessions> getSessions() {
		return sessions;
	}

	public void setSessions(LinkedList<Sessions> sessionsObject) {
		this.sessions = sessionsObject;
	}

	public LinkedList<Triggers> getTriggers() {
		return triggers;
	}

	public void setTriggers(LinkedList<Triggers> triggersObject) {
		this.triggers = triggersObject;
	}

	public LinkedList<Runbooks> getRunbooks() {
		return runbooks;
	}

	public void setRunbooks(LinkedList<Runbooks> runbooksObject) {
		this.runbooks = runbooksObject;
	}

	public LinkedList<Calendars> getCalendars() {
		return calendars;
	}

	public void setCalendars(LinkedList<Calendars> calendarsObject) {
		this.calendars = calendarsObject;
	}

	public LinkedList<BusinessViews> getBusinessViews() {
		return businessViews;
	}

	// Setter Methods

	public void setBusinessViews(LinkedList<BusinessViews> businessViewsObject) {
		this.businessViews = businessViewsObject;
	}

	public LinkedList<JobChains> getJobChains() {
		return jobChains;
	}

	public void setJobChains(LinkedList<JobChains> jobChainsObject) {
		this.jobChains = jobChainsObject;
	}

	public LinkedList<JobEvents> getJobEvents() {
		return jobEvents;
	}

	public void setJobEvents(LinkedList<JobEvents> jobEventsObject) {
		this.jobEvents = jobEventsObject;
	}

	public LinkedList<DqmJobs> getDqmJobs() {
		return dqmJobs;
	}

	public void setDqmJobs(LinkedList<DqmJobs> dqmJobsObject) {
		this.dqmJobs = dqmJobsObject;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	@Override
	public void doPostTransformJobObjects(List<BaseJobOrGroupObject> jobs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doProcessJobDependency(List<BaseJobOrGroupObject> jobs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doPostJobDependencyJobObject(List<BaseJobOrGroupObject> jobs) {
		// TODO Auto-generated method stub
		
	}
}
