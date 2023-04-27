package com.bluehouseinc.dataconverter.api.importer.transformers;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.impl.CsvOS400;
import com.bluehouseinc.tidal.api.model.job.JobType;
import com.bluehouseinc.tidal.api.model.job.os400.OS400Job;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

public class OS400JobTransformer implements ITransformer<CsvOS400, OS400Job> {

	OS400Job base;
	TidalAPI tidal;

	public OS400JobTransformer(OS400Job base, TidalAPI tidal) {
		this.base = base;
		this.tidal = tidal;
	}

	@Override
	public OS400Job transform(CsvOS400 in) throws TransformationException {

		this.base.setType(JobType.OS400JOB);

		String cmd = in.getPge1CommandToRun();
		if (cmd != null) {
			if (cmd.trim().isEmpty()) {
				this.base.setPge1command("ERROR");
			} else {
				this.base.setPge1command(in.getPge1CommandToRun());
			}
		} else {
			this.base.setPge1command("ERROR");
		}

		this.base.setPge1jobdescription(in.getPge1JobDescription());
		this.base.setPge1jobdescriptionlib(in.getPge1JobDescriptionLibary());
		this.base.setPge1jobname(in.getPge1JobName());
		this.base.setPge1jobpriority(in.getPge1JobPriorityOnJobQ());
		this.base.setPge1jobqueue(in.getPge1JobQueue());
		this.base.setPge1jobqueuelib(in.getPge1JobQueueLibrary());

		this.base.setPge1outputqueue(in.getPge1OutputQueue());
		this.base.setPge1outputqueuelib(in.getPge1OutputQueueLibrary());

		this.base.setPge1outputpriority(in.getPge1OutputPriorityOnOutQ());
		this.base.setPge1printdevice(in.getPge1PrintDevice());

		this.base.setPge2printtext(in.getPge2PrintText());
		this.base.setPge2reqdatacmd(in.getPge2RequestDataOrCommand());
		this.base.setPge2routedata(in.getPge2RoutingData());
		this.base.setPge2user(in.getPge2User());

		this.base.setPge3currentlib(in.getPge3CurrentLibrary());
		this.base.setPge3displaysbmjob(in.getPge3AllowDisplayByWRKSBMJOB());
		this.base.setPge3holdjobqueue(in.getPge3HoldOnJobQueue());
		this.base.setPge3initiallib(in.getPge3InitalLibraryList());
		this.base.setPge3inqmsgreply(in.getPge3InqueryMessageReply());
		this.base.setPge3jobswitches(in.getPge3JpobSwitches());
		this.base.setPge3mloglevel(in.getPge3MessageLoggingLevel());
		this.base.setPge3mlogseverity(in.getPge3MessageLoggingSeverity());
		this.base.setPge3mlogtext(in.getPge3MessageLoggingText());
		this.base.setPge3progcommand(in.getPge3LogCLProgramCommands());
		this.base.setPge3systemlib(in.getPge3SystemLibraryList());

		this.base.setPge4allowmultithreads(in.getPge4AllowMultipleThreads());
		this.base.setPge4charsetid(in.getPge4CodedCharSetId());
		this.base.setPge4copyenvvariables(in.getPge4CopyEnvironmentVariables());
		this.base.setPge4countryid(in.getPge4CountryRegionID());
		this.base.setPge4initialaspgroup(in.getPge4InitialASPGroup());
		this.base.setPge4jobmqueuefullaction(in.getPge4FullAction());
		this.base.setPge4jobmqueuesize(in.getPge4JobMessageQueueMaxSize());
		this.base.setPge4languageid(in.getPge4LanguageID());
		this.base.setPge4msgqueue(in.getPge4MessageQueue());
		this.base.setPge4msgqueuelib(in.getPge4MessageQueueLibrary());
		this.base.setPge4number(in.getPge4Number());
		this.base.setPge4sortsequence(in.getPge4SortSequence());
		this.base.setPge4sortsequencelib(in.getPge4SortSequenceLibrary());
		this.base.setPge4spoolfileaction(in.getPge4SpoolFileAction());
		this.base.setPge4submittedfor(in.getPge4SubmittedFor());
		this.base.setPge4user(in.getPge4User());

		return this.base;
	}

}
