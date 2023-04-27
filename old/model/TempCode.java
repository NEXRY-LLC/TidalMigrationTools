package com.dataconverter.csv.model;

import java.util.ArrayList;
import java.util.List;

public class TempCode {

	private static CvsCalendar CAL = new CvsCalendar("CALENDER");
	
	public static List<CvsJobGroup> getJobGroups() {
		List<CvsJobGroup> list = new ArrayList<>();

		list.add(getJobGroup());
		list.add(getJobGroup());
		list.add(getJobGroup());

		return list;
	}

	public static List<CvsOSJob> getJobs() {
		List<CvsOSJob> list = new ArrayList<>();

		list.add(getNextJob());
		list.add(getNextJob());
		list.add(getNextJob());

		return list;
	}

	public static CvsJobGroup getJobGroup() {
		CvsJobGroup group = getNextGroup();

		group.addChildJobOrGroup(getNextJob());
		group.addChildJobOrGroup(getNextJob());
		group.addChildJobOrGroup(getNextJob());
		group.addChildJobOrGroup(getNextJob());

		return group;
	}

	public static void getGroups() {
		CvsJobOrGroupDependency dep = new CvsJobOrGroupDependency();
		CvsJobGroup group = getNextGroup();
		
		group.addChildJobOrGroup(getNextJob());
		group.addChildJobOrGroup(getNextJob());
		group.addChildJobOrGroup(getNextJob());
		group.addChildJobOrGroup(getNextJob());

		CvsJobGroup grouptwo = getNextGroup();
		dep.setJob(grouptwo);
		dep.setDependsOnJob(group);
		//grouptwo.addDependency(dep); // Register the dependency to group two
		dep.setDependsOnJob(group); // Set that group depend
		grouptwo.addChildJobOrGroup(getNextJob());
		grouptwo.addChildJobOrGroup(getNextJob());
		
		
		CvsOSJob jobone = getNextJob();
		
		dep = new CvsJobOrGroupDependency();

		CvsOSJob jobtwo = getNextJob();
		dep.setJob(jobtwo);
		dep.setDependsOnJob(jobone);

		
		
	}

	private static int num = 0;

	private static int getNextInt() {
		return num++;
	}

	private static CvsOSJob getNextJob() {
		CvsOSJob job = new CvsOSJob();
		job.setId(getNextInt());
		job.setName("Job" + job.getId());
		job.setCommand("cmd.exe");
		job.setParams("echo foo bar");
		job.setCalender(CAL);
		return job;
	}

	private static CvsJobGroup getNextGroup() {
		CvsJobGroup group = new CvsJobGroup();

		group.setId(getNextInt());
		group.setName("Group" + group.getId());
		group.setCalender(CAL);
		return group;
	}
}
