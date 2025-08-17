package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvJobClass;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.jobclass.JobClass;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class JobClassExecutor extends AbstractAPIExecutor {

	public JobClassExecutor(TidalAPI tdl, TidalDataModel mdl, ConfigurationProvider cfgProvider) {
		super(tdl, mdl, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {
		getDataModel().getJobClasses().forEach(f -> {
			executor.submit(() -> {
				doProcessJobClass(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getJobClasses().forEach(f -> {
			doProcessJobClass(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "Job Class Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getJobClasses().size();
	}



	protected void doProcessJobClass(CsvJobClass jobclass, ProgressBar bar) {
		try {
			JobClass existing = getTidalApi().getJobClassByName(jobclass.getName());

			if(existing != null) {
			 log.debug("doProcessJobClass[" + existing.getName() + "] Skipping already exist");
			}else {
				JobClass jobClassToAdd = new JobClass();
				jobClassToAdd.setName(jobclass.getName());

				TesResult res = doCreate(jobClassToAdd);

				int newid = res.getResult().getTesObjectid();
				jobClassToAdd.setId(newid);
				getTidalApi().getJobClass().add(jobClassToAdd);
				log.debug("doProcessJobClass [" + jobClassToAdd.getName() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");
			}

		} finally {
			bar.step();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {
		return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().jobClass();
	}

}
