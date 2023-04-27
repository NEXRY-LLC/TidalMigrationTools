package com.bluehouseinc.dataconverter.parsers.bmc.transformers;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvOS400;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOS400Job;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BMCOS400Job.OSCommand;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;
import com.bluehouseinc.transform.TransformationException;

public class BMCOS400JobTransformer implements ITransformer<BMCOS400Job, BaseCsvJobObject> {

	TidalDataModel datamodel;

	public BMCOS400JobTransformer(TidalDataModel dm) {
		this.datamodel = dm;
	}

	@Override
	public BaseCsvJobObject transform(BMCOS400Job in) throws TransformationException {
		return doHandleOS400MultiCommands(in);
	}

	/**
	 * TODO: Finish this code Multi command and pre or post command OS400 jobs need
	 * to be broken into indiviual jobs.
	 *
	 * @param osj
	 */
	private BaseCsvJobObject doHandleOS400MultiCommands(BMCOS400Job bmcjob) {

		final String jobname = bmcjob.getName();

		if (jobname.equals("INFFTPSTS")) {
			// System.out.println();
		}

		CsvOS400 template = new CsvOS400();
		template.setName(jobname);
		/*
		 * Defaults from our API Unit testing
		 */
		template.setPge1JobName("*JOBD");
		template.setPge1JobDescription("*USRPRF");
		template.setPge1OutputQueue("*CURRENT");
		template.setPge2RequestDataOrCommand("*CMD");
		template.setPge2RoutingData("QCMDB");
		template.setPge3AllowDisplayByWRKSBMJOB("*YES");
		template.setPge4CopyEnvironmentVariables("*NO");

		CsvRuntimeUser rte = null;

		if (bmcjob.getJobOwner() != null) {
			rte = new CsvRuntimeUser(bmcjob.getJobOwner());
		} else if (bmcjob.getJobData().getRUNAS() != null) {
			rte = new CsvRuntimeUser(bmcjob.getJobData().getRUNAS());
		} else {
			rte = new CsvRuntimeUser("UNKNOWN-OS400-User");
		}

		rte.setPasswordForFtpOrAS400("tidal");
		datamodel.addRunTimeUserToJobOrGroup(template, rte);

		// Copy from BMC job data
		template.setPge1JobDescription(bmcjob.getJobDescription());
		template.setPge1JobDescriptionLibary(bmcjob.getJobDescriptionLibrary());

		template.setPge1JobQueue(bmcjob.getJobQueue());
		template.setPge1JobQueueLibrary(bmcjob.getJobQueueLibrary());
		template.setPge1OutputQueue(bmcjob.getOutQueueName());
		template.setPge1OutputQueueLibrary(bmcjob.getOutQueueLibrary());

		template.setPge1JobName(bmcjob.getJobName());
		template.setPge1JobPriorityOnJobQ(bmcjob.getJobPriorityOnQueue());

		template.setPge2PrintText(bmcjob.getPrintText());

		if (!StringUtils.isBlank(bmcjob.getJobDescription()) && !StringUtils.isBlank(bmcjob.getJobDescriptionLibrary())) {
			template.setPge3InitalLibraryList("*JOBD"); // Must be set in TIDAL if these two are set.
		} else {
			template.setPge3InitalLibraryList(bmcjob.getInitidalLibrary());
		}

		template.setPge3CurrentLibrary(bmcjob.getCurrentLibrary());
		template.setPge3HoldOnJobQueue(bmcjob.getHoldOnJobQueue());
		template.setPge3SystemLibraryList(bmcjob.getSystemLibraryList());
		template.setPge3InqueryMessageReply(bmcjob.getInqueryMessageReply());

		template.setPge4InitialASPGroup(bmcjob.getInitalASPGroup());
		template.setPge4MessageQueue(bmcjob.getMessageQueue());
		template.setPge4MessageQueueLibrary(bmcjob.getMessageQueueLibrary());
		template.setPge4FullAction(bmcjob.getFullAction());
		template.setPge4JobMessageQueueMaxSize(bmcjob.getJobMessageQueueMaxSize());

		template.setPge3MessageLoggingLevel(bmcjob.getMessageLogLevel());
		template.setPge3MessageLoggingSeverity(bmcjob.getMessageLogSeverity());
		template.setPge3MessageLoggingText(bmcjob.getMessageLogText());
		// osj.getJobAuthor() TODO: Where does this mapp too.

		BaseCsvJobObject base = null;

		/*
		 * We want to cleanup multi step jobs with a single step, so we only care about
		 * size > 1. Any post command means we need a group to group things together
		 * regardless of what the job type is.. Meaning you can have a single job, not
		 * multipstep have post commands, if true then we need a group with a job, group
		 * with other jobs for the post commands.
		 */
		if (bmcjob.getCommands().size() > 1 || !bmcjob.getPostCommands().isEmpty()) {

			// If we are here our base is a group period.
			base = new CsvJobGroup();

			// copyMatchingFieldsExcludeID(template, base); // Copy our data over to our
			// group.

			bmcjob.setName(jobname + "-MULTISTEP-OS400");
			base.setName(bmcjob.getName());

			// Handle our multiple commands or one.
			CsvOS400 priorjob = null; // Needed for dependency mapping.

			if (bmcjob.getCommands().size() > 1) {

				doSetupCommands((CsvJobGroup) base, bmcjob.getCommands(), bmcjob.getParams(), template);

			} else {
				String command = getCommand(bmcjob);
				template.setPge1CommandToRun(command);
				base.addChild(template);
				priorjob = template; // SO I can setup deps.
			}

			if (!bmcjob.getPostCommands().isEmpty()) {
				CsvJobGroup postgroup = new CsvJobGroup();
				postgroup.setName(jobname + "-MULTISTEP-OS400-POSTCOMMANDS");
				datamodel.addJobDependencyForJobCompletedNormal(postgroup, priorjob, null); // Make us dependent on the
																							// last known job

				doSetupCommands(postgroup, bmcjob.getPostCommands(), bmcjob.getParams(), template);
				base.addChild(postgroup);
			}

		} else {
			// Single command stuff so just set base to template and move on.
			String command = getCommand(bmcjob);
			template.setPge1CommandToRun(command);
			base = template;
		}

		return base;
	}

	private String getCommand(BMCOS400Job bmcjob) {
		String command = "";

		if (bmcjob.getObjectType() == BMCOS400Job.ObjectTypes.PGM) {
			command = "CALL PGM(" + bmcjob.getMemoryName() + ")";// This is what PGM needs to call.
		} else {
			// Just standard OS400 job.EG. CMDLINE, but handle the empty command data.. E.G
			// bad data or a multi command with no commands
			command = String.join(" ", bmcjob.getCommands().stream().map(p -> p.getValue()).collect(Collectors.toList()));
		}
		// TODO: Double check that our param data does not need to match our multi
		// command data.
		if (!bmcjob.getParams().isEmpty()) {
			String data = String.join(" ", bmcjob.getParams());
			command = command + " PARM(" + data + ")";
		}

		return command;
	}

	private void copyMatchingFieldsExcludeID(BaseCsvJobObject from, BaseCsvJobObject too) {

		try {
			int id = too.getId();
			ObjectUtils.copyMatchingFields(from, too);
			too.setId(id);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} // Copy our data over to our group.
	}

	private void doSetupCommands(CsvJobGroup postgroup, List<OSCommand> commands, List<String> params, BaseCsvJobObject template) {

		final AtomicInteger count = new AtomicInteger(0);

		if (!commands.isEmpty()) {
			BaseCsvJobObject priorjob = null;

			for (OSCommand osc : commands) {
				int cnt = count.incrementAndGet();

				CsvOS400 commandjob = new CsvOS400();
				copyMatchingFieldsExcludeID(template, commandjob);

				commandjob.setName(postgroup.getName() + "-XXXXStep-" + cnt);

				if (!params.isEmpty()) {
					String data = String.join(" ", params);
					String command = osc.getValue() + " PARM(" + data + ")";
					commandjob.setPge1CommandToRun(command);
				} else {
					commandjob.setPge1CommandToRun(osc.getValue());
				}

				if (cnt == 1) {
					priorjob = commandjob; // First one so just set to prior
				} else {
					datamodel.addJobDependencyForJobCompletedNormal(commandjob, priorjob, null);
					priorjob = commandjob; // Set after we register.
				}

				postgroup.addChild(commandjob);
			}
		}
	}
}
