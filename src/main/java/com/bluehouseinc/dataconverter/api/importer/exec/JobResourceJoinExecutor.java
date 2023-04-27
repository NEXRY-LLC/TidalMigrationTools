package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvResource;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.resource.jobjoin.ResourceJobJoin;
import com.bluehouseinc.tidal.api.model.resource.virtual.VirtualResource;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class JobResourceJoinExecutor extends AbstractAPIExecutor {

	public JobResourceJoinExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		for (CsvResource res : getDataModel().getJobResourceJoins().keySet()) {
			getDataModel().getJobResourceJoins().get(res).forEach(f -> {
				executor.submit(() -> {
					doProcessJobResource(res, f, bar);
				});
			});
		}

	}

	@Override
	public void doExecute(ProgressBar bar) {
		for (CsvResource res : getDataModel().getJobResourceJoins().keySet()) {
			getDataModel().getJobResourceJoins().get(res).forEach(f -> {
				doProcessJobResource(res, f, bar);
			});
		}
	}

	@Override
	public int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.resourcejoin", "0"));
	}

	@Override
	public String getProgressBarName() {
		return "Job Resource Join Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getJobResourceJoinsCounter();
	}

	protected void doProcessJobResource(CsvResource resource, BaseCsvJobObject ajob, ProgressBar bar) {
		try {
			String name = resource.getName().trim();
			Integer limit = resource.getLimit();

			VirtualResource vres = getTidalApi().getResourceByName(name);

			if (vres != null) {

				List<ResourceJobJoin> reslist = getTidalApi().getJobResourcesByJobID(ajob.getId());

				ResourceJobJoin resjoin = reslist.stream().filter(f -> f.getResourcename().trim().equalsIgnoreCase(name)).findAny().orElse(null);

				if (resjoin == null) {
					ResourceJobJoin resjobjoin = new ResourceJobJoin();
					resjobjoin.setJobid(ajob.getId());
					resjobjoin.setResourceid(vres.getId());
					resjobjoin.setResourcename(vres.getName());
					resjobjoin.setUsed(limit);

					TesResult res = getTidalApi().getSession().getServiceFactory().resourceJobJoin().create(resjobjoin);
					int newid = res.getResult().getTesObjectid();
					resjobjoin.setId(newid);
					getTidalApi().getResourcesJobJoin().add(resjobjoin);

					log.debug("doProcessJobResource Job[" + ajob.getFullPath() + "] Resource [" + resjobjoin.getResourcename() + "] LIMIT[" + limit + "] Response ID[" + newid + "][" + res.getResponseData() + "]");

				} else {
					log.debug("doProcessJobResource Job[" + ajob.getFullPath() + "] Resource[" + resjoin.getResourcename() + "] Skipping already exists");
				}
			} else {
				log.error("ERROR doProcessJobResource MISSING RESOURCE[" + name + "]");
			}
		} finally {
			bar.step();
		}
	}

}
