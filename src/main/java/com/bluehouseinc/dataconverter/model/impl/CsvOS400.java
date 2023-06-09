package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.JobType;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class CsvOS400 extends BaseCsvJobObject {

	@CsvBindByName
	String pge1CommandToRun;
	@CsvBindByName
	String pge1JobName;
	@CsvBindByName
	String pge1JobDescription;
	@CsvBindByName
	String pge1JobDescriptionLibary;
	@CsvBindByName
	String pge1JobQueue;
	@CsvBindByName
	String pge1JobQueueLibrary;
	@CsvBindByName
	String pge1JobPriorityOnJobQ;
	@CsvBindByName
	String pge1OutputPriorityOnOutQ;
	@CsvBindByName
	String pge1OutputQueue;
	@CsvBindByName
	String pge1OutputQueueLibrary;
	@CsvBindByName
	String pge1PrintDevice;
	@CsvBindByName
	String pge2User;
	@CsvBindByName
	String pge2PrintText;
	@CsvBindByName
	String pge2RoutingData;
	@CsvBindByName
	String pge2RequestDataOrCommand;
	@CsvBindByName
	String pge3SystemLibraryList;
	@CsvBindByName
	String pge3CurrentLibrary;
	@CsvBindByName
	String pge3InitalLibraryList;
	@CsvBindByName
	String pge3MessageLoggingLevel;
	@CsvBindByName
	String pge3MessageLoggingSeverity;
	@CsvBindByName
	String pge3MessageLoggingText;
	@CsvBindByName
	String pge3LogCLProgramCommands;
	@CsvBindByName
	String pge3InqueryMessageReply;
	@CsvBindByName
	String pge3HoldOnJobQueue;
	@CsvBindByName
	String pge3JpobSwitches;
	@CsvBindByName
	String pge3AllowDisplayByWRKSBMJOB;
	@CsvBindByName
	String pge4MessageQueue;
	@CsvBindByName
	String pge4MessageQueueLibrary;
	@CsvBindByName
	String pge4SortSequence;
	@CsvBindByName
	String pge4SortSequenceLibrary;
	@CsvBindByName
	String pge4LanguageID;
	@CsvBindByName
	String pge4SubmittedFor;
	@CsvBindByName
	String pge4CountryRegionID;
	@CsvBindByName
	String pge4User;
	@CsvBindByName
	String pge4CodedCharSetId;
	@CsvBindByName
	String pge4Number;
	@CsvBindByName
	String pge4JobMessageQueueMaxSize;
	@CsvBindByName
	String pge4FullAction;
	@CsvBindByName
	String pge4CopyEnvironmentVariables;
	@CsvBindByName
	String pge4AllowMultipleThreads;
	@CsvBindByName
	String pge4InitialASPGroup;
	@CsvBindByName
	String pge4SpoolFileAction;

	@Override
	public JobType getType() {
		return JobType.OS400JOB;
	}

}
