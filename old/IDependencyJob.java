package com.dataconverter.model;

public interface IDependencyJob {

	IBaseJobGroupObject getDependsOnJob();

	void setDependsOnJob(IBaseJobGroupObject job);
}
