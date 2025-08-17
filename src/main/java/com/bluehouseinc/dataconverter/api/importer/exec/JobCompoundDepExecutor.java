package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.TidalApi;
import com.bluehouseinc.tidal.api.TidalReadOnlyEntry;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.impl.atom.response.Entry;
import com.bluehouseinc.tidal.api.impl.atom.response.Feed;
import com.bluehouseinc.tidal.api.model.BaseAPIObject;
import com.bluehouseinc.tidal.api.model.job.BaseJob;
import com.bluehouseinc.tidal.api.model.job.DepLogicType;
import com.bluehouseinc.tidal.api.model.job.Job;
import com.bluehouseinc.tidal.utils.DependencyBuilder;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class JobCompoundDepExecutor extends AbstractAPIExecutor {

	public JobCompoundDepExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getCompoundDependnecyJobs().forEach(f -> {
			executor.submit(() -> {
				doProcessJobThreads(f, bar);
			});
		});

	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getCompoundDependnecyJobs().forEach(f -> {
			doProcessJobThreads(f, bar);
		});
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.compoundjobs", "0"));
	}

	@Override
	public String getProgressBarName() {
		return "Job/Group Compound Dependency";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getCompoundDependnecyJobs().size();
	}

	protected void doProcessJobThreads(BaseCsvJobObject ajob, ProgressBar bar) {
		doProcessCompoundDependency(ajob, bar);

	}

	/**
	 * Can be jobs or groups
	 *
	 * @param source
	 */
	private void doProcessCompoundDependency(BaseCsvJobObject source, ProgressBar bar) {
		try {
			String path = source.getFullPath();
			log.debug("doProcessCompoundDependency Job[" + path + "] Type[" + source.getType().toString() + "]");

			BaseJob destination = getTidalApi().findJobByPathToLower(path); // We must exist

			if (destination == null) {
				// Major issue!!!!!!

				log.debug("doProcessCompoundDependency Missing Job [" + source.getFullPath() + "] Type[" + source.getClass().getSimpleName() + "]");
				throw new TidalException("doProcessCompoundDependency Missing Job [" + source.getFullPath() + "] Type[" + source.getClass().getSimpleName() + "]");
			} else {
				int jobid = destination.getId();

				log.debug("doProcessCompoundDependency Processing Compound Dependency for Job [{}] Type[{}]", destination.getFullpath(), destination.getClass().getSimpleName());
				// builder should have been updated so just call this to be safe

				//source.setCompoundDependencyStringFromBuilder();
				String sourcedep = source.getCompoundDependency();
				String destdep = destination.getCompounddependencies();

				if (destdep != null && destdep.equals(sourcedep)) {
					// Do nothing , we are already setup?
					log.debug("doProcessCompoundDependency Skipping Job [" + source.getFullPath() + "] Type[" + source.getClass().getSimpleName() + "] Compound Dependency already setup.");
				} else {
					destination.setCompounddependencies(sourcedep);

					Job updatejob = new Job();
					updatejob.setId(jobid);

					String tokened = DependencyBuilder.tokenizeString(sourcedep);
					updatejob.setCompounddependencies(tokened);
					updatejob.setType(destination.getType());
					updatejob.setDependencylogic(DepLogicType.COMPOUND);
					updatejob.setAgentid(destination.getAgentid());
					updatejob.setInheritagent(destination.getInheritagent());

					getTidalApi().getSession().getServiceFactory().job().update(updatejob);

				}
				return;

			}

		} catch (Exception e) {
			log.error(e);
			// throw new TidalException(e);
		} finally {
			bar.step();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entry<C>, F extends Feed<C, E>, C extends BaseAPIObject, D extends TidalReadOnlyEntry<E, C>> TidalApi<E, F, C, D> getExecutorAPI(C object) {
		return (TidalApi<E, F, C, D>) getTidalApi().getSession().getServiceFactory().job();
	}

}
