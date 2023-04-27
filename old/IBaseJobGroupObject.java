package com.dataconverter.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface IBaseJobGroupObject  {
	static final AtomicInteger count = new AtomicInteger(0);

	int getId();

	String getName();

	String getCustomStartTimes();

	Integer getRerunInterval();

	Integer getRerunMax();

	String getNotes();

	ICalendar getCalendar();

	IOwner getOwner();

	IRunTimeUser getRuntimeUser();



	void setId(int id);

	void setName(String name);

	void setCustomStartTimes(String times);

	void setRerunInterval(Integer interval);

	void setRerunMax(Integer reruns);

	void setNotes(String notes);

	void setCalendar(ICalendar cal);

	void setOwner(IOwner own);

	void setRuntimeUser(IRunTimeUser run);



	String getFullPath();

	IBaseJobGroupObject getParent();

	List<IBaseJobGroupObject> getChildren();

	void setParent(IBaseJobGroupObject parent);

	void setChildren(List<IBaseJobGroupObject> children);

	void addChild(IBaseJobGroupObject child);


	void setStartTime(String id);

	String getStartTime();

	void setEndTime(String id);

	String getEndTime();


	Boolean getRequireOperatorRelease();
	
	void setRequireOperatorRelease(Boolean tf);


}
