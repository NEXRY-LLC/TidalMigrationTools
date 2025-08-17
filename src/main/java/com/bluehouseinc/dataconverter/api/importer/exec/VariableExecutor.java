package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.variable.Variable;
import com.bluehouseinc.tidal.api.model.variable.VariableType;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class VariableExecutor extends AbstractAPIExecutor {

	public VariableExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getVariables().forEach(f -> {
			executor.submit(() -> {
				doProcessVariable(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getVariables().forEach(f -> {
			doProcessVariable(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "Variable Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getVariables().size();
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.variable", "0"));
	}

	protected void doProcessVariable(CsvVariable var, ProgressBar bar) {
		try {
			Optional<Variable> existing = getTidalApi().getVariables().stream().filter(f -> f.getName().equalsIgnoreCase(var.getVarName())).findFirst();

			if (existing.isPresent()) {
				log.debug("doProcessVariable[" + existing.get().getName() + "] Skipping already exist");
			} else {
				// Add the calendar to TIDAL.
				Variable add = new Variable();
				add.setName(var.getVarName());
				add.setInnervalue(var.getVarValue());
				add.setPub(YesNoType.NO);
				add.setType(VariableType.STRING);
				add.setOwner(getTidalApi().getDefaultOwner());
				TesResult res = doCreate(add);
				int newid = res.getResult().getTesObjectid();
				add.setId(newid); // Why are we not setting this on create??
				getTidalApi().getVariables().add(add); // Add our new variable back to tidal.
				log.debug("doProcessVariable [" + add.getName() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");

			}
		} finally {
			bar.step();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {
		return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().variable();
	}

}
