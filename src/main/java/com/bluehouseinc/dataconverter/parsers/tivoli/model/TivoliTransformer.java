package com.bluehouseinc.dataconverter.parsers.tivoli.model;

import java.util.List;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.cpu.CpuData;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.job.TivoliJobObject;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

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

		CpuData cpu =  job.getCpuData();
		
		if(cpu != null) {
			datamodel.addNodeToJobOrGroup(out , cpu.getNodeName() == null ? "TIVOLI-NOTSET" : cpu.getNodeName());
			
			String domain = cpu.getDomain();
			String runtime = cpu.getForUser();
			
			if(!StringUtils.isBlank(runtime)) {
				CsvRuntimeUser rte = new CsvRuntimeUser(runtime, domain);
				datamodel.addRunTimeUserToJobOrGroup(tempjob, rte);
			}else {
				datamodel.addRunTimeUserToJobOrGroup(out, new CsvRuntimeUser("runtime-notset"));
			}
		}else {
			// Error with cpu data, this object doesnt not have a default
			datamodel.addNodeToJobOrGroup(out, "TIVOLI-NOTSET");
		}
		
		datamodel.addOwnerToJobOrGroup(out, datamodel.getDefaultOwner());

//
//		SchedualData schedata = job.getScheduleData();
//
//		if (schedata != null) {
//
//			JobRunTime runtime = job.getScheduleData().getAtTime();
//
//			if (runtime != null) {
//				String runat = job.getScheduleData().getAtTime().getAtTime();
//				if (!StringUtils.isBlank(runat)) {
//					out.setStartTime(runat);
//				}
//			}
//			
//			JobRunTime dealine = job.getScheduleData().getDeadline();
//
//			if (dealine != null) {
//				String runtill = job.getScheduleData().getDeadline().getAtTime();
//				if (!StringUtils.isBlank(runtill)) {
//					out.setStartTime(runtill);
//				}
//			}
//
//			job.getScheduleData().getNeeds().forEach(n -> {
//
//				Integer resneed = n.getAmount();
//				CsvResource res = new CsvResource(n.getName(), datamodel.getDefaultOwner());
//				res.setLimit(resneed);
//				datamodel.addResourceToJob(out, res);
//
//			});
//
//			datamodel.addCalendarToJobOrGroup(out, new CsvCalendar("Daily"));
//
//			// Need to build some calendars using the schedule data.
//			job.getScheduleData().getRunOn().forEach(r -> {
//
//				r.getType();
//			});

//		} else {
//			// why are we missing data?
//			out.getFullPath();
//
//		}

		return out;
	}

	private void processJob(TivoliJobObject in, CsvJobGroup out) {

	}

	private void processJob(TivoliJobObject in, CsvOSJob out) {

		String cmddata = in.getDoCommand() == null ? in.getScriptName() : in.getDoCommand();

		if (cmddata == null) {
			cmddata = "NOTSET";
		}

		if (cmddata.startsWith("\"")) {
			cmddata = cmddata.substring(1);
		}

		if (cmddata.endsWith("\"")) {
			cmddata = cmddata.substring(0, cmddata.length() - 1);
		}

		String[] cmddata_split = cmddata.split(" ", 2);

		if (cmddata_split.length == 2) {
			out.setCommandLine(cmddata_split[0]);
			out.setParamaters(cmddata_split[1]);
		} else {
			out.setCommandLine(cmddata_split[0]);
		}

	}
}
