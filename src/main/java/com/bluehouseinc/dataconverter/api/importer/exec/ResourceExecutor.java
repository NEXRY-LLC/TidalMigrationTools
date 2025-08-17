package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvResource;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.owners.Owner;
import com.bluehouseinc.tidal.api.model.resource.virtual.VirtualResource;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class ResourceExecutor extends AbstractAPIExecutor {

	public ResourceExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getResource().forEach(f -> {
			executor.submit(() -> {
				doProcessResource(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getResource().forEach(f -> {
			doProcessResource(f, bar);
		});
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.resource", "0"));
	}

	@Override
	public String getProgressBarName() {
		return "Resource Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getResource().size();
	}

	protected void doProcessResource(CsvResource resource, ProgressBar bar) {

		try {
			if (resource == null) {
				log.debug("doProcessResource getResourceByName is Null");

				return;
			}

			String name = resource.getName();

			if (!StringUtils.isBlank(name)) {
				log.debug("doProcessResource getResourceByName[" + name + "]");

				VirtualResource existing = getTidalApi().getResourceByName(name.trim());

				if (existing == null) {
					VirtualResource add = new VirtualResource();

					add.setName(resource.getName());

					Owner own = getTidalApi().getOwnerByName(resource.getOwner().getOwnerName());

					if (own == null) {
						add.setOwner(getTidalApi().getDefaultOwner());
					} else {
						add.setOwner(own);
					}
					add.setPublicflag("N");
					add.setResourcetype("1");
					add.setActive("Y");
					add.setLimit(Integer.toString(resource.getLimit()));
					
					TesResult res = doCreate(add);
					int newid = res.getResult().getTesObjectid();
					add.setId(newid);
					getTidalApi().getResources().add(add);
					
					log.debug("doProcessResource [" + add.getName() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");
				}
			} else {
				log.debug("doProcessResource getResourceByName[" + name + "] Skipping already exists");
			}

		} finally {
			bar.step();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {
		return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().virtualResource();
	}

}
