package com.bluehouseinc.dataconverter.api.importer;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.exec.AbstractAPIExecutor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import me.tongfei.progressbar.ProgressBar;

public class TidalApiExecutor extends AbstractAPIExecutor {

	private int LOAD = 20000;

	public TidalApiExecutor(TidalAPI tidal, ConfigurationProvider cfgProvider) {
		super(tidal, null, cfgProvider);
	}

	public TidalApiExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		executor.submit(() -> {
			doProcessJobs(bar);
		});

		executor.submit(() -> {
			doProcessVariables(bar);
		});

		executor.submit(() -> {
			doProcessCalendar(bar);
		});

		executor.submit(() -> {
			doProcessOwners(bar);
		});

		executor.submit(() -> {
			doProcessWorkGroup(bar);
		});

		executor.submit(() -> {
			doProcessAgents(bar);
		});

		executor.submit(() -> {
			doProcessDeps(bar);
		});

		executor.submit(() -> {
			doProcessAdapters(bar);
		});

		executor.submit(() -> {
			doProcessAdapterUsers(bar);
		});

		executor.submit(() -> {
			doProcessEmailActions(bar);
		});

		executor.submit(() -> {
			doProcessUsers(bar);
		});

		executor.submit(() -> {
			doProcessVirtualResources(bar);
		});

		executor.submit(() -> {
			doProcessBuinessUnits(bar);
		});

		executor.submit(() -> {
			doProcessAgentList(bar);
		});

		executor.submit(() -> {
			doProcessJobResJoin(bar);
		});

		executor.submit(() -> {
			doProcessJobClass(bar);
		});

	}

	@Override
	public void doExecute(ProgressBar bar) {

		doProcessEmailActions(bar);

		doProcessAgentList(bar);

		doProcessBuinessUnits(bar);

		doProcessVirtualResources(bar);

		doProcessUsers(bar);

		doProcessVariables(bar);

		doProcessCalendar(bar);

		doProcessOwners(bar);

		doProcessWorkGroup(bar);

		doProcessAgents(bar);

		doProcessAdapters(bar);

		doProcessAdapterUsers(bar);

		doProcessJobs(bar);

		doProcessDeps(bar);

		doProcessJobResJoin(bar);

		doProcessJobClass(bar);

	}

	@Override
	public String getProgressBarName() {
		return "API Data Loader";
	}

	@Override
	public int getProgressBarTotal() {
		return 15;
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.executor", "0"));
	}

	private void doProcessJobs(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessJobs");
			getTidalApi().jobs = getTidalApi().getSf().job().getConstructedJobs(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessVariables(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessVariables");
			getTidalApi().variables = getTidalApi().getSf().variable().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessCalendar(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessCalendar");
			getTidalApi().calendars = getTidalApi().getSf().calendar().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessOwners(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessOwners");
			getTidalApi().owners = getTidalApi().getSf().owners().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessWorkGroup(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessWorkGroup");
			getTidalApi().workgroups = getTidalApi().getSf().workGroup().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessAgents(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessAgents");
			getTidalApi().nodes = getTidalApi().getSf().node().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessDeps(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessDeps");
			getTidalApi().jobdep = getTidalApi().getSf().jobDependency().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessAdapters(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessAdapters");
			getTidalApi().adapters = getTidalApi().getSf().adapterService().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessAdapterUsers(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessAdapterUsers");
			getTidalApi().userService = getTidalApi().getSf().userService().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessEmailActions(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessEmailActions");
			getTidalApi().emailActions = getTidalApi().getSf().emailAction().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessUsers(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessUsers");
			getTidalApi().users = getTidalApi().getSf().users().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessVirtualResources(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessVirtualResources");
			getTidalApi().resources = getTidalApi().getSf().virtualResource().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessBuinessUnits(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessBuinessUnits");
			getTidalApi().businessUnits = getTidalApi().getSf().businessUnit().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessAgentList(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessAgentList");
			getTidalApi().agentList = getTidalApi().getSf().agentList().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessJobResJoin(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessJobResJoin");
			getTidalApi().resourcesJobJoin = getTidalApi().getSf().resourceJobJoin().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessJobClass(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessJobClass");
			getTidalApi().jobClass = getTidalApi().getSf().jobClass().getList(LOAD);
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}
}
