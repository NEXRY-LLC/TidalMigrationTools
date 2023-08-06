package com.bluehouseinc.dataconverter.parsers.tivoli.model;

import java.util.List;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CsvResource;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.CpuData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.JobRunTime;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.SchedualData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.on.RunOn;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TivoliTransformer implements ITransformer<List<TivoliJobObject>, TidalDataModel> {

	TidalDataModel datamodel;

	public TivoliTransformer(TidalDataModel datamodel) {
		this.datamodel = datamodel;
	}

	@Override
	public TidalDataModel transform(List<TivoliJobObject> in) throws TransformationException {

		in.stream().forEach(f -> doProcessObjects(f, null));

		return datamodel;
	}

	private void doProcessObjects(TivoliJobObject job, CsvJobGroup parent) {

		if (job.isGroup()) {

			CsvJobGroup jobgroup = (CsvJobGroup) processJobObject(job);

			if (parent == null) { // This cant be true ever with ESP, but it's keeping with the job check... TIDAL does support this
				datamodel.addJobToModel(jobgroup);
			} else {
				parent.addChild(jobgroup);
			}

			job.getChildren().forEach(obj -> doProcessObjects((TivoliJobObject) obj, jobgroup));

		} else {

			BaseCsvJobObject newJob = processJobObject(job);

			if (newJob != null) {
				if (parent == null) {
					datamodel.addJobToModel(newJob);
				} else {
					parent.addChild(newJob);
				}
			}
		}

	}

	private BaseCsvJobObject processJobObject(TivoliJobObject job) {

		if (job == null) {
			throw new TidalException("BaseJobOrGroupObject is null");
		}

		BaseCsvJobObject tempjob = null;

		if (job.isGroup()) {
			tempjob = new CsvJobGroup();
			processJob(job, (CsvJobGroup) tempjob);
		} else {
			tempjob = new CsvOSJob();
			processJob(job, (CsvOSJob) tempjob);
		}

		tempjob.setName(job.getName());

		final BaseCsvJobObject out = tempjob;

		CpuData cpu = job.getCpuData();

		if (cpu != null) {
			datamodel.addNodeToJobOrGroup(out, cpu.getNodeName() == null ? "TIVOLI-NOTSET" : cpu.getNodeName());

			String domain = cpu.getDomain();
			String runtime = cpu.getForUser();

			if (!StringUtils.isBlank(runtime)) {
				CsvRuntimeUser rte = new CsvRuntimeUser(runtime, domain);
				datamodel.addRunTimeUserToJobOrGroup(tempjob, rte);
			} else {
				datamodel.addRunTimeUserToJobOrGroup(out, new CsvRuntimeUser("runtime-notset"));
			}
		} else {
			// Error with cpu data, this object doesnt not have a default
			datamodel.addNodeToJobOrGroup(out, "TIVOLI-NOTSET");
		}

		datamodel.addOwnerToJobOrGroup(out, datamodel.getDefaultOwner());

		return out;
	}

	private void processJob(TivoliJobObject in, CsvJobGroup out) {

		SchedualData schedata = in.getSchedualData();

		setStartTime(out, schedata.getAtTime());

		setEndTime(out, schedata.getDeadline());

		schedata.getNeeds().forEach(n -> {
			addResource(out, n.getName(), n.getAmount());
		});

		datamodel.addCalendarToJobOrGroup(out, new CsvCalendar("Daily"));
		
		if (schedata.getRunOn() == null) {

		} else if (!schedata.getRunOn().isEmpty()) {
			

			StringBuilder b = new StringBuilder();

			
			List<RunOn> runons = schedata.getRunOn();

			if (runons.size() == 1) {

				
			}else {
				schedata.getRunOn().forEach(cal -> {

				});
			}
		}

	}

	private void processJob(TivoliJobObject in, CsvOSJob out) {

		String cmddata = in.getDoCommand() == null ? in.getScriptName().trim() : in.getDoCommand().trim();

		if (StringUtils.isBlank(cmddata)) {
			log.info("Missing command line data for JOB{}",in.getFullPath());
			cmddata = "NOTSET";
		}

		if(cmddata.startsWith("\"") & cmddata.endsWith("\"")) {
			cmddata = cmddata.substring(1, cmddata.length()-1);
			cmddata = cmddata.trim();
		}

		if (cmddata.startsWith("\"")) {
			cmddata = cmddata.substring(1);
		}

		if (cmddata.endsWith("\"")) {
			cmddata = cmddata.substring(0, cmddata.length() - 1);
		}
		
		String[] cmddata_split = cmddata.trim().split("\\s+", 2);

		if (cmddata_split.length == 2) {
			out.setCommandLine(cmddata_split[0]);
			out.setParamaters(cmddata_split[1]);
		} else {
			out.setCommandLine(cmddata_split[0]);
		}

		if (StringUtils.isBlank(out.getCommandLine())) {
			out.setCommandLine("NOTSET");;
		}
		
		setStartTime(out, in.getJobScheduleData().getAtTime());

		setEndTime(out, in.getJobScheduleData().getUntilTime());
		setEndTime(out, in.getJobScheduleData().getDeadline());

		out.setInheritCalendar(true);

		in.getJobScheduleData().getNeeds().forEach(n -> {
			addResource(out, n.getName(), n.getAmount());
		});
	}

	private void addResource(BaseCsvJobObject job, String name, Integer limit) {

		CsvResource res = new CsvResource(name, datamodel.getDefaultOwner());
		res.setLimit(limit);
		datamodel.addResourceToJob(job, res);

	}

	private void setStartTime(BaseCsvJobObject out, JobRunTime runtime) {
		if (runtime != null) {
			String runat = runtime.getAtTime();
			if (!StringUtils.isBlank(runat)) {
				out.setStartTime(runat);
			}

		}
	}

	private void setEndTime(BaseCsvJobObject out, JobRunTime runtime) {
		if (runtime != null) {
			String runat = runtime.getAtTime();
			if (!StringUtils.isBlank(runat)) {
				out.setEndTime(runat);
			}

		}
	}
}
