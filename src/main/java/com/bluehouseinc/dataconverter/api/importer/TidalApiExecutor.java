package com.bluehouseinc.dataconverter.api.importer;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.exec.AbstractAPIExecutor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import me.tongfei.progressbar.ProgressBar;

public class TidalApiExecutor extends AbstractAPIExecutor {

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

		executor.submit(() -> {
			doProcessWorkGroupRunUsers(bar);
		});
		
		executor.submit(() -> {
			doProcessTagData(bar);
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

		doProcessWorkGroupRunUsers(bar);
		
		doProcessTagData(bar);

	}

	@Override
	public String getProgressBarName() {
		return "API Data Loader";
	}

	@Override
	public int getProgressBarTotal() {
		return 18;
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.executor", "0"));
	}

	private void doProcessJobs(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessJobs");
			String cnt = getTidalApi().getCfgProvider().getConfigurations().getOrDefault("tidal.batch.jobs", null);

			if (cnt == null) {
				getTidalApi().jobs = getTidalApi().getSf().job().getConstructedJobs();
			} else {
				getTidalApi().jobs = getTidalApi().getSf().job().getConstructedJobs(Integer.valueOf(cnt));
			}

		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessVariables(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessVariables");
			String cnt = getTidalApi().getCfgProvider().getConfigurations().getOrDefault("tidal.batch.variables", null);

			if (cnt == null) {
				getTidalApi().variables = getTidalApi().getSf().variable().getList();
			} else {
				getTidalApi().variables = getTidalApi().getSf().variable().getList(Integer.valueOf(cnt));
			}
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessCalendar(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessCalendar");
			getTidalApi().calendars = getTidalApi().getSf().calendar().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessOwners(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessOwners");
			getTidalApi().owners = getTidalApi().getSf().owners().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessWorkGroup(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessWorkGroup");
			getTidalApi().workgroups = getTidalApi().getSf().workGroup().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	void doProcessAgents(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessAgents");
			getTidalApi().nodes = getTidalApi().getSf().node().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessDeps(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessDeps");
			String cnt = getTidalApi().getCfgProvider().getConfigurations().getOrDefault("tidal.batch.dependency", null);

			if (cnt == null) {
				getTidalApi().jobdeps = getTidalApi().getSf().jobDependency().getList();
				getTidalApi().filedeps = getTidalApi().getSf().fileDependency().getList();
			} else {
				getTidalApi().jobdeps = getTidalApi().getSf().jobDependency().getList(Integer.valueOf(cnt));
				getTidalApi().filedeps = getTidalApi().getSf().fileDependency().getList(Integer.valueOf(cnt));
			}
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessAdapters(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessAdapters");
			getTidalApi().adapters = getTidalApi().getSf().adapterService().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessAdapterUsers(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessAdapterUsers");
			getTidalApi().userService = getTidalApi().getSf().userService().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessEmailActions(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessEmailActions");
			getTidalApi().emailActions = getTidalApi().getSf().emailAction().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessUsers(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessUsers");
			getTidalApi().users = getTidalApi().getSf().users().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessVirtualResources(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessVirtualResources");
			getTidalApi().resources = getTidalApi().getSf().virtualResource().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessBuinessUnits(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessBuinessUnits");
			getTidalApi().businessUnits = getTidalApi().getSf().businessUnit().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessAgentList(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessAgentList");
			getTidalApi().agentList = getTidalApi().getSf().agentList().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessJobResJoin(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessJobResJoin");
			getTidalApi().resourcesJobJoin = getTidalApi().getSf().resourceJobJoin().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}

	private void doProcessJobClass(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessJobClass");
			getTidalApi().jobClass = getTidalApi().getSf().jobClass().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}
	
	private void doProcessWorkGroupRunUsers(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessWorkGroupRunUsers");
			getTidalApi().workRunUsers = getTidalApi().getSf().workGroupRunUser().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}
	
	private void doProcessTagData(ProgressBar bar) {
		try {
			bar.setExtraMessage("doProcessTagData");
			getTidalApi().tags = getTidalApi().getSf().tag().getList();
			getTidalApi().tagMap = getTidalApi().getSf().tagmap().getList();
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			bar.step();
		}
	}
}
