package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.service.Service;
import com.bluehouseinc.tidal.api.model.users.Users;
import com.bluehouseinc.tidal.api.model.users.adapter.UserService;
import com.bluehouseinc.tidal.api.model.users.constants.UserType;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class RunTimeUserExecutor extends AbstractAPIExecutor {

	public RunTimeUserExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getRuntimeusers().forEach(f -> {
			executor.submit(() -> {
				doProcessRunTimeUser(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getRuntimeusers().forEach(f -> {
			doProcessRunTimeUser(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "Runtime User Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getRuntimeusers().size();
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.runtime", "0"));
	}

	protected void doProcessRunTimeUser(CsvRuntimeUser rte, ProgressBar bar) {

		try {
			Users existing = getTidalApi().getFirstRunTimeUserByNameAndDomain(rte.getRunTimeUserName(), rte.getRunTimeUserDomain());

			Collection<UserService> adapterpass = new ArrayList<>();

			Users usr = null;

			if (existing != null) {
				usr = existing;
				adapterpass = getTidalApi().getUserAdapterServices(existing);

				log.debug("doProcessRunTimeUser[" + usr.getName() + "] Skipping Already Exist");
			} else {
				usr = new Users();

				usr.setFullname(rte.getRunTimeUserName());
				usr.setName(rte.getRunTimeUserName());
				usr.setDomain(rte.getRunTimeUserDomain());
				usr.setSuperuser(UserType.RUNTIMEUSER);

				if (StringUtils.isBlank(rte.getPasswordForFtpOrAS400())) {
					usr.setPassword("tidal");
				} else {
					usr.setPassword(rte.getPasswordForFtpOrAS400());
				}

				TesResult res = getTidalApi().getSession().getServiceFactory().users().create(usr);

				int newid = res.getResult().getTesObjectid();
				usr.setId(newid);
				getTidalApi().getUsers().add(usr);

				log.debug("doProcessRunTimeUser [" + usr.getName() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");
			}

			// Do we have Special Passwords?

			if (rte.getPasswordForPeopleSoft() != null) {
				String guid = "{B20EC120-2EB5-4d5f-8133-73FA37225667}";
				UserService existsvc = adapterpass.stream().filter(f -> f.getGuid().equals(guid)).findFirst().orElse(null);

				if (existsvc == null) {
					Service adapter = getTidalApi().getAdapterByGUID(guid);

					existsvc = new UserService();
					existsvc.setGuid(guid);
					existsvc.setUserid(usr.getId());
					existsvc.setServiceid(adapter.getId());
					existsvc.setServicename(adapter.getName());
					existsvc.setPassword(rte.getPasswordForPeopleSoft());
					TesResult res = getTidalApi().getSession().getServiceFactory().userService().create(existsvc);
					int newid = res.getResult().getTesObjectid();
					log.debug("doProcessRunTimeUser [" + usr.getName() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");
				}
			}

			if (rte.getPasswordForSAP() != null) {
				String guid = "{51C57049-3215-44b7-ABE1-C012FF786010}";
				UserService existsvc = adapterpass.stream().filter(f -> f.getGuid().equals(guid)).findFirst().orElse(null);

				if (existsvc == null) {
					Service adapter = getTidalApi().getAdapterByGUID(guid);

					existsvc = new UserService();
					existsvc.setGuid(guid);
					existsvc.setUserid(usr.getId());
					existsvc.setServiceid(adapter.getId());
					existsvc.setServicename(adapter.getName());
					existsvc.setPassword(rte.getPasswordForSAP());
					
					try {
						TesResult res = doCreate(existsvc);

						int newid = res.getResult().getTesObjectid();
						log.debug("doProcessRunTimeUser [" + usr.getName() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");
					} catch (Exception e) {
						// eat
						log.info("doProcessRunTimeUser ERROR");

					}
				}
			}
		} finally {
			bar.step();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {
		return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().userService();
	}
}
