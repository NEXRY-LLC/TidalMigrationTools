package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvJobTag;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.job.BaseJob;
import com.bluehouseinc.tidal.api.model.jobclass.JobClass;
import com.bluehouseinc.tidal.api.model.owners.Owner;
import com.bluehouseinc.tidal.api.model.tag.Tag;
import com.bluehouseinc.tidal.api.model.tag.TagType;
import com.bluehouseinc.tidal.api.model.tagmap.TagMap;
import com.bluehouseinc.tidal.api.model.tagmap.TagMapType;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class JobTagMapExecutor extends AbstractAPIExecutor {

	public JobTagMapExecutor(TidalAPI tdl, TidalDataModel mdl, ConfigurationProvider cfgProvider) {
		super(tdl, mdl, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {
		getDataModel().getJobTags().keySet().forEach(f -> {
			executor.submit(() -> {
				getDataModel().getJobTags().get(f).forEach(j -> doProcessJobTag(f, j, bar));
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getJobTags().keySet().forEach(f -> {
			getDataModel().getJobTags().get(f).forEach(j -> doProcessJobTag(f, j, bar));
		});
	}

	@Override
	public String getProgressBarName() {
		return "Job Tag Map Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getJobTagsMapCounter();
	}

	protected void doProcessJobTag(CsvJobTag cvstag, BaseCsvJobObject job, ProgressBar bar) {
		try {
			Tag existingtag = getTidalApi().getJobTagByName(cvstag.getName());

			if (existingtag == null) {
				throw new TidalException("doProcessJobTag[" + cvstag.getName() + "] Missing, must already exist.");

			} else {

				BaseJob foundjob = getTidalApi().findJobByPathToLower(job.getFullPath());

				if (foundjob != null) {
					log.debug("doProcessJobTag Adding Tag[" + existingtag.getName() + "] to Job[" + foundjob.getFullpath() + "]");

					// we are in our system.
					int jobid = foundjob.getId();
					TagMap existingmap = getTidalApi().getTagMap().stream().filter(t -> t.getDataid() == jobid).findAny().orElse(null);

					if (existingmap == null) {
						TagMap jobtag = new TagMap();
						jobtag.setDataid(foundjob.getId());
						jobtag.setTableid(TagMapType.JOB);
						jobtag.setTagid(existingtag.getId());

						TesResult res = getTidalApi().getSession().getServiceFactory().tagmap().create(jobtag);

						int newid = res.getResult().getTesObjectid();
						jobtag.setId(newid);
						getTidalApi().getTagMap().add(jobtag);
						log.debug("doProcessJobTag Added Tag[" + existingtag.getName() + "] to Job[" + foundjob.getFullpath() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");
					} else {
						log.debug("doProcessJobTag SKIPPING Tag[" + existingtag.getName() + "] to Job[" + job.getFullPath() + "] already exists");
					}
				} else {
					log.error("doProcessJobTag UNABLE TO ADD Tag[" + existingtag.getName() + "] to Unable to locate Job[" + job.getFullPath() + "]");
				}

			}

		} finally {
			bar.step();
		}
	}

}
