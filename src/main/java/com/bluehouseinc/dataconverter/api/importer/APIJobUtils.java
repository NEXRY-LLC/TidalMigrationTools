package com.bluehouseinc.dataconverter.api.importer;

import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.job.BaseJob;
import com.bluehouseinc.tidal.api.model.job.ftp.FTPJob;
import com.bluehouseinc.tidal.api.model.job.group.JobGroup;
import com.bluehouseinc.tidal.api.model.job.os.OSJob;
import com.bluehouseinc.tidal.api.model.job.service.ServiceJob;
import com.bluehouseinc.tidal.utils.JobUtil;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.toolkit.CommandLine;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class APIJobUtils {
	// private static Logger logger = LogManager.getLogger(APIJobUtils.class);

	public static void setDisableInhertOnJob(BaseJob job) {
		job.setInheritagent(YesNoType.NO);
		job.setInheritcalendar(YesNoType.NO);
		job.setInheritoptions(YesNoType.NO);
		job.setInheritrepeat(YesNoType.NO);
		job.setInherittimewindow(YesNoType.NO);
	}

	public static void setEnableInhertOnJob(BaseJob job) {
		job.setInheritagent(YesNoType.YES);
		job.setInheritcalendar(YesNoType.YES);
		job.setInheritoptions(YesNoType.YES);
		job.setInheritrepeat(YesNoType.YES);
		job.setInherittimewindow(YesNoType.YES);
	}

	public static void setStartEndTime(BaseJob newjob, BaseCsvJobObject job) {
		try {


			if (!StringUtils.isBlank(job.getEndTime())) {
				JobUtil.setEndTime(newjob, job.getEndTime()); // If we have one
				newjob.setInherittimewindow(YesNoType.NO);
			}

			if (!StringUtils.isBlank(job.getStartTime())) {
				JobUtil.setStartTime(newjob, job.getStartTime()); // if we have one
				newjob.setInherittimewindow(YesNoType.NO);
			}

		} catch (NumberFormatException e) {
			log.error("##################### Unable to set start[" + job.getStartTime() + "] or end time[" + job.getEndTime() + "] for job[" + job.getFullPath() + "] #########################");
		}
	}


	public static void setRerunFrequency(BaseJob newjob, BaseCsvJobObject job) {

		if (job.getRerunLogic().isReRunningJob()) {

			newjob.setRepeat(job.getRerunLogic().getRepeatType());
			newjob.setRepeatinterval(job.getRerunLogic().getRepeatEvery());
			newjob.setRepeatcount(job.getRerunLogic().getRepeatMaxTimes());
			newjob.setCustomstarttimes(job.getRerunLogic().getRerunData());
		}

	}

	@SuppressWarnings("unchecked")
	public static <T extends BaseJob> T initJobByClass(Class<T> type) {
		BaseJob t = new BaseJob();

		if (type.isAssignableFrom(JobGroup.class)) {
			t = new JobGroup();
		} else if (type.isAssignableFrom(OSJob.class)) {
			t = new OSJob();
		} else if (type.isAssignableFrom(FTPJob.class)) {
			t = new FTPJob();
		} else if (type.isAssignableFrom(ServiceJob.class)) {
			t = new ServiceJob();
		} else {
			throw new TidalException("UnSupport Job Type");
		}

		setJobDefaults(t);

		return (T) t;
	}

	public static void setJobDefaults(BaseJob job) {
		job.setActive(YesNoType.NO);

		if (job.getParent() == null) {
			setDisableInhertOnJob(job);
		} else {
			setEnableInhertOnJob(job);
		}

	}


	public static void setJobCommandDetail(CsvOSJob dest, String command) {

		if (command == null) {
			command = "ERROR NOT SET";
		}
		// quotes are in the XML we get back sometimes.
		command = command.replaceAll("&quot;", "\"");

		if (command.contains("\"\"")) {
			command = command.replace("\"\"", "\"");
		}

		try {

			CommandLine cl = CommandLine.parse(command);
			String exe = cl.getExecutable();

			String params = String.join("\n", cl.getArguments());

			dest.setCommandLine(exe);

			if (!StringUtils.isBlank(params)) {
				dest.setParamaters(params);
			}
		} catch (IllegalArgumentException e) {
			throw new TidalException(e);
		}

	}

}
