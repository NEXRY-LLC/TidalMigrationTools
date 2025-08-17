package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvTimeZone;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.businessunit.BusinessUnit;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class TimeZoneExecutor extends AbstractAPIExecutor {

	public TimeZoneExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getTimeZones().forEach(f -> {
			executor.submit(() -> {
				doProcessTimeZone(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getTimeZones().forEach(f -> {
			doProcessTimeZone(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "TimeZone Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getTimeZones().size();
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.timezone", "0"));
	}

	protected void doProcessTimeZone(CsvTimeZone zone, ProgressBar bar) {

		try {
			if (zone == null) {
				return;
			}

			String name = zone.getName();

			if (!StringUtils.isBlank(name)) {
				name = name.trim();
				log.debug("doProcessTimeZone getBusinessUnitByName[" + name + "]");

				BusinessUnit existing = getTidalApi().getBusinessUnitByName(name);

				if (existing == null) {
					BusinessUnit add = new BusinessUnit();

					add.setName(name);
					add.setTimezoneid(zone.getTimezoneId());
					
					TesResult res = doCreate(add);

					int newid = res.getResult().getTesObjectid();
					add.setId(newid); // Why are we not setting this on create??
					getTidalApi().getBusinessUnits().add(add);

					log.debug("doProcessTimeZone [" + add.getName() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");
				}
			} else {
				log.debug("doProcessTimeZone getBusinessUnitByName[" + name + "] Skipping already exists");
			}
		} finally {
			bar.step();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {
		return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().businessUnit();
	}
}
