package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvActionEmail;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.actions.email.EmailAction;
import com.bluehouseinc.tidal.api.model.owners.Owner;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class EmailActionExecutor extends AbstractAPIExecutor {

	public EmailActionExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getEmailActions().forEach(f -> {
			executor.submit(() -> {
				doProcessEmailActions(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getEmailActions().forEach(f -> {
			doProcessEmailActions(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "EmailAction Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getEmailActions().size();
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.emailaction", "0"));
	}

	protected void doProcessEmailActions(CsvActionEmail action, ProgressBar bar) {

		try {
			if (action != null) {

				String name = action.getName();

				if (!StringUtils.isBlank(name)) {
					if (name.length() > 62) {
						name = name.substring(0, 62).trim();
						log.debug("doProcessEmailActions Name is too long , now 62 characters[" + name + "]");
					}

					log.debug("doProcessEmailActions getEmailActionByName[" + name + "]");
					EmailAction add = getTidalApi().getEmailActionByName(name);

					if (add == null) {

						add = new EmailAction();

						add.setName(name);
						add.setToaddresses(action.getToEmailAddresses());
						add.setSubject(action.getSubject());
						add.setMessage(action.getMessage());

						if (action.getOwner() != null) {

							Owner own = getTidalApi().getOwnerByName(action.getOwner().getOwnerName());

							if (own == null) {
								throw new TidalException("doProcessEmailActions Unable to find owner with NAME[" + action.getOwner().getOwnerName() + "] in TIDAL must be added prior to processing.");
							} else {
								add.setOwner(own);
							}
						} else {
							add.setOwner(getTidalApi().getDefaultOwner());
						}

						TesResult res = getTidalApi().getSession().getServiceFactory().emailAction().create(add);
						int newid = res.getResult().getTesObjectid();
						add.setId(newid);
						getTidalApi().getEmailActions().add(add);

						log.debug("doProcessEmailActions Name[" + name + "] Response ID[" + newid + "][" + res.getResponseData() + "]");

					} else {
						log.debug("doProcessEmailActions getEmailActionByName[" + name + "] Skipping already exists");
					}

				}
			}
		} finally {
			bar.step();
		}
	}

}
