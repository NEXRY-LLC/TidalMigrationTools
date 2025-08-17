package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvOwner;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.owners.Owner;
import com.bluehouseinc.tidal.api.model.owners.OwnerType;
import com.bluehouseinc.tidal.api.model.workgroup.RuntimeUserListType;
import com.bluehouseinc.tidal.api.model.workgroup.WorkGroup;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class OwnerExecutor extends AbstractAPIExecutor {

	public OwnerExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getOwners().forEach(f -> {
			executor.submit(() -> {
				doProcessOwners(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getOwners().forEach(f -> {
			doProcessOwners(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "Owner Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getOwners().size();
	}

	@Override
	public int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.owner", "0"));
	}

	protected void doProcessOwners(CsvOwner owner, ProgressBar bar) {
		try {
			Optional<WorkGroup> existing = getTidalApi().getWorkgroups().stream().filter(f -> f.getName().equalsIgnoreCase(owner.getOwnerName())).findFirst();

			if (existing.isPresent()) {
				log.debug("doProcessOwners[" + existing.get().getName() + "] Skipping already exist");
			} else {

				// Add to TIDAL.
				WorkGroup add = new WorkGroup();
				add.setName(owner.getOwnerName());
				add.setAllagents(YesNoType.YES);
				add.setAllowmemberrunusers(RuntimeUserListType.EveryUserAndWorkGroup);
				Owner own = new Owner();
				own.setId(1); // Set to one for now, eventually need to query model
				add.setOwner(own);
				TesResult res = doCreate(add);
				int newid = res.getResult().getTesObjectid();
				add.setId(newid); // Why are we not setting this on create??
				getTidalApi().getWorkgroups().add(add); // add
				log.debug("doProcessOwners [" + add.getName() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");

				Owner newowner = new Owner();
				newowner.setId(add.getId()); // ID matches always
				newowner.setName(add.getName());
				newowner.setType(OwnerType.WORKGROUP); // User or Workgroup
				log.debug("doProcessOwners [" + existing.get().getName() + "]");
				getTidalApi().getOwners().add(newowner);
			}
		} finally {
			bar.step();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {
		return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().workGroup();
	}

}
