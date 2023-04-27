package com.dataconverter.domainmodel;

import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dataconverter.csv.model.CvsAbstractDependency;
import com.dataconverter.csv.model.CvsAbstractJobOrGroup;
import com.dataconverter.csv.model.CvsCalendar;
import com.dataconverter.csv.model.CvsJobGroup;
import com.dataconverter.csv.model.CvsJobOrGroupDependency;
import com.dataconverter.csv.model.CvsOwner;
import com.dataconverter.csv.model.CvsRunTimeUser;
import com.dataconverter.csv.model.CvsVariable;

@Data
public class DataModel {
	private List<CvsAbstractJobOrGroup> joborgroups = new ArrayList<>();
	private Set<CvsVariable> variables = new HashSet<>();
	private Set<CvsCalendar> calendars = new HashSet<>();
	private Set<CvsOwner> owners = new HashSet<CvsOwner>();
	private List<CvsAbstractDependency> dependencies = new ArrayList<>();
	private Set<CvsRunTimeUser> runtimeusers = new HashSet<CvsRunTimeUser>();

	/**
	 * Helper method to add a variable to our model if not existing.
	 * 
	 * @param var
	 */
	public void addVariable(CvsVariable var) {
		if (var == null) {
			return;
		}

		if (!this.variables.contains(var)) {
			this.variables.add(var);
		}
	}

	/**
	 * Helper method to add a group to our Model. Does same level name checks but
	 * only prints for now.
	 * 
	 * @param group
	 */
	public void addGroup(CvsJobGroup group) {
		for (CvsAbstractJobOrGroup c : this.joborgroups) {
			if (c.getName().equals(group.getName())) {
				System.out.println("################## DUP GROUP[" + group.getName() + "] Detected");
			}
		}

		this.joborgroups.add(group);
	}

	/**
	 * Helper method to add a calendar to a job and register it to the datamodel for
	 * you
	 * 
	 * @param ajob
	 * @param cal
	 */
	public void addCalendarToJobOrGroup(CvsAbstractJobOrGroup ajob, CvsCalendar cal) {
		if (cal == null) {
			return;
		}

		ajob.setCalender(cal);

		if (!this.getCalendars().contains(cal)) {
			this.getCalendars().add(cal);
		}
	}

	/**
	 * Helper method to add a run time user to a job
	 * 
	 * @param ajob
	 * @param rt
	 */
	public void addRunTimeUserToJobOrGroup(CvsAbstractJobOrGroup ajob, CvsRunTimeUser rt) {
		if (rt == null) {
			return;
		}

		ajob.setRuntimeuser(rt);

		if (!this.getRuntimeusers().contains(rt)) {
			this.getRuntimeusers().add(rt);
		}
	}

	public CvsJobGroup findGroupByName(String name) {
		return findGroupByName(name, this.joborgroups);
	}

	private CvsJobGroup findGroupByName(String name, List<CvsAbstractJobOrGroup> list) {

		CvsJobGroup found = null;

		for (CvsAbstractJobOrGroup c : list) {
			if (c instanceof CvsJobGroup) {
				CvsJobGroup group = (CvsJobGroup) c;
				String gname = group.getName();
				if (gname.equalsIgnoreCase(name)) {
					found = group;
					return found;
				}

				if (!group.getChildren().isEmpty()) {
					found = findGroupByName(name, group.getChildren());

					if (found != null) {
						return found;
					}
				}

			}
		}

		return found;
	}

	public void addCalendar(CvsCalendar cal) {
		if (this.calendars.contains(cal)) {
			return;
		}
		this.calendars.add(cal);
	}

	/*
	 * Ugly but we have to track ID changes on jobs to depdenency as this is the
	 * only ares so far I know of.
	 * 
	 */
	public void updateJobID(CvsAbstractJobOrGroup job, int id) {
		int oldid = job.getId();

		job.setId(id);
		// Look for dependy refs.

		// dependencies.stream().filter(f -> f.getJobId()==oldid).forEach(f ->
		// f.setJobId(id));

		for (CvsAbstractDependency dep : dependencies) {
			// hmm. need id and depjob id

			if (dep.getJobId() == oldid) {
				dep.setJobId(id); // set to new id

				if (dep instanceof CvsJobOrGroupDependency) {
					CvsJobOrGroupDependency jd = (CvsJobOrGroupDependency) dep;

					if (jd.getDependsOnJobId() == oldid) {
						jd.setDependsOnJobId(id);
					}
				}
			}
		}
	}

	/**
	 * This shoudl return all types including nested children of groups in a flat
	 * list.
	 * 
	 * @param <T>
	 * @param list
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CvsAbstractJobOrGroup> List<T> getJobsByType(List<CvsAbstractJobOrGroup> list, Class<T> type) {
		List<T> childs = new ArrayList<T>();

		for (CvsAbstractJobOrGroup job : list) {
			if (type.isInstance(job)) {
				childs.add((T) job);
			}

			if (job instanceof CvsJobGroup) {
				childs.addAll(getJobsByType(((CvsJobGroup) job).getChildren(), type)

				);

			}
		}

		return childs;
	}
}
