package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.calendar.Calendar;
import com.bluehouseinc.tidal.api.model.calendar.CalendarType;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class CalendarExecutor extends AbstractAPIExecutor {

	public CalendarExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getCalendars().forEach(f -> {
			executor.submit(() -> {
				doProcessCalendar(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getCalendars().forEach(f -> {
			doProcessCalendar(f, bar);
		});
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.calendar", "0"));
	}

	@Override
	public String getProgressBarName() {
		return "Calendar Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getCalendars().size();
	}

	protected void doProcessCalendar(CsvCalendar cal, ProgressBar bar) {

		try {
			Optional<Calendar> existing = getTidalApi().getCalendars().stream().filter(f -> f.getName().trim().equalsIgnoreCase(cal.getCalendarName().trim())).findFirst();

			if (existing.isPresent()) {
				log.debug("doProcessCalendar SKIPPING Calendar [" + existing.get().getName() + "] exists in TIDAL");
			} else {
				// Add the calendar to TIDAL.
				Calendar add = new Calendar();
				
				add.setType(CalendarType.GROUP);
				add.setCalendarname(cal.getCalendarName());
				add.setName(cal.getCalendarName());
				add.setPub(YesNoType.NO);
				add.setDescription(cal.getCalendarNotes());
				add.setOwner(getTidalApi().getDefaultOwner());

				TesResult res =doCreate(add);// getTidalApi().getSession().getServiceFactory().calendar().create(add);
				int newid = res.getResult().getTesObjectid();
				add.setId(newid); // Why are we not setting this on create??
				getTidalApi().getCalendars().add(add); // add to our calendars
				log.debug("doProcessCalendar Name[" + add.getCalendarname() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");

			}
		} finally {
			bar.step();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {
		return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().calendar();
	}

}
