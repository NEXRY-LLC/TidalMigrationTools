package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCvsDependency;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyJob;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.dependency.job.JobDependency;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class DependencyExecutor extends AbstractAPIExecutor {

	public DependencyExecutor(TidalAPI tidal, TidalDataModel model, ConfigurationProvider cfgProvider) {
		super(tidal, model, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {

		getDataModel().getDependencies().forEach(f -> {
			executor.submit(() -> {
				doProcessDep(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getDependencies().forEach(f -> {
			doProcessDep(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "Dependency Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getDependencies().size();
	}

	@Override
	protected int getThreadCount() {
		return Integer.valueOf(this.cfgProvider.getConfigurations().getOrDefault("tidal.threadcount.dependency", "0"));
	}

	protected void doProcessDep(BaseCvsDependency adep, ProgressBar bar) {

		try {
			int me = adep.getJobObject().getId();
			Integer depson;
			String mepath = adep.getJobObject().getFullPath();
			
			if (mepath.toUpperCase().contains("AIPPD040")) {
				mepath.getBytes();
			}
			
			String deppath;
			if (adep instanceof CvsDependencyJob) {
				CvsDependencyJob jdep = ((CvsDependencyJob) adep);
				depson = jdep.getDependsOnJob().getId();
				deppath = jdep.getDependsOnJob().getFullPath();

				JobDependency existing = getTidalApi().findDependencyByJobId(me, depson);

				if (existing != null) {
					log.debug("doProcessDep DEPID[" + existing.getId() + "] JOB ID[" + me + "] JOB[" + mepath + "] depends on  ID[" + existing.getDepjobid() + "][" + deppath + "] Skipping");
					getDataModel().updateBaseCsvDependencyID(jdep, existing.getId());
					return;
				}

				JobDependency dep = new JobDependency();

				dep.setJobid(me);
				dep.setDepjobid(depson);
				dep.setLogic(jdep.getLogic());
				dep.setOperator(jdep.getOperator());
				dep.setStatus(jdep.getJobStatus());
				dep.setIgnoredep("Y");

				if (jdep.getDateOffset() != null) {
					// If we have a offset then we need to set relative to group to No to allow for the offset
					// Positive number is looking back in time.
					dep.setDateoffset(jdep.getDateOffset());
					dep.setIngroup(YesNoType.NO);
				}

				if(jdep.hasExitCodeLogic()) {
					dep.setExitCode(jdep.getExitCodeOperator(), jdep.getExitcodeStart(), jdep.getExitcodeEnd());
				}

				try {

					log.debug("doProcessDep[" + mepath + "] depends on  [" + deppath + "]");
					TesResult res = getTidalApi().getSession().getServiceFactory().jobDependency().create(dep);
					int id = res.getResult().getTesObjectid();
					dep.setId(id);
					getDataModel().updateBaseCsvDependencyID(jdep, id);
					getTidalApi().getJobdep().add(dep);

					log.debug("doProcessDep[" + mepath + "] depends on  [" + deppath + "] ID[" + id + "] Response[" + res.getTesMessage() + "]");
				} catch (Exception e) {
					String localmessage = e.getLocalizedMessage();
					// String data = Arrays.toString(e.getStackTrace());

					if (localmessage.contains("job dependency will result in a loop")) {
						log.error("doProcessDep ERROR CREATING DEPENDENCY => JOB[" + mepath + "] HAS DEPENDENCY ON [" + deppath + "] LOOP DETECTED");
					} else {
						log.error("ERROR doProcessDep[" + mepath + "] depends on  [" + deppath + "]");
						log.error(localmessage );
					}
				}
			}
		} finally {
			bar.step();
		}
	}

}
