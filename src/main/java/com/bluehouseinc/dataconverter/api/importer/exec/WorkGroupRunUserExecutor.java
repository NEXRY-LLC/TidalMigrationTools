package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.owners.Owner;
import com.bluehouseinc.tidal.api.model.users.Users;
import com.bluehouseinc.tidal.api.model.workgroup.WorkGroup;
import com.bluehouseinc.tidal.api.model.workgroup.runuser.WorkGroupRunUser;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class WorkGroupRunUserExecutor extends AbstractAPIExecutor {

	public WorkGroupRunUserExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getRuntimeusers().forEach(f -> {
			executor.submit(() -> {
				doProcessWorkGroupRunUser(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getRuntimeusers().forEach(f -> {
			doProcessWorkGroupRunUser(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "WorkGroup Runtime User";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getRuntimeusers().size();
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.workgroup.runtime", "0"));
	}

	/**
	 * Need to add our runtime users to our workgroup ownership.
	 * 
	 * @param rte
	 * @param bar
	 */
	protected void doProcessWorkGroupRunUser(CsvRuntimeUser rte, ProgressBar bar) {

		try {
			// Hack but our owner ID is not unique , it matches a workgroup or user record ID's based on type.
			// We can assume that our default owner is a workgroup at all times or force failure by design.
			Owner workgroup = getTidalApi().getDefaultOwner();

			Users existing = getTidalApi().getFirstRunTimeUserByNameAndDomain(rte.getRunTimeUserName(),rte.getRunTimeUserDomain());
			

			if (existing != null) {

				int groupid = workgroup.getId();
				int usrid = existing.getId();
				WorkGroupRunUser wrkuser = getTidalApi().getWorkGroupRunUserById(groupid,usrid);

				if (wrkuser == null) {
					WorkGroupRunUser wu = new WorkGroupRunUser();

					WorkGroup hack = new WorkGroup();
					hack.setId(workgroup.getId()); // Just set this real quick.
					wu.addUserToWorkGroup(existing, hack);

					log.debug("doProcessWorkGroupRunUser Adding[" + rte.getRunTimeUserName() + "][" + workgroup.getName() + "]");

					TesResult res = getTidalApi().getSf().workGroupRunUser().create(wu);
					int newid = res.getResult().getTesObjectid();
					wu.setId(newid);
					getTidalApi().getWorkRunUsers().add(wu);

				} else {
					log.debug("doProcessWorkGroupRunUser[" + rte.getRunTimeUserName() + "][" + workgroup.getName() + "] Skipping Already Exist");
				}
			} else {
				log.error("doProcessWorkGroupRunUser[" + rte.getRunTimeUserName() + "] Not Found in System.");

			}

		} finally {
			bar.step();
		}
	}
}
