package com.bluehouseinc.dataconverter.api.importer.exec;

import java.util.concurrent.ExecutorService;

import com.bluehouseinc.dataconverter.api.importer.TidalAPI;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvJobTag;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.impl.atom.response.TesResult;
import com.bluehouseinc.tidal.api.model.YesNoType;
import com.bluehouseinc.tidal.api.model.jobclass.JobClass;
import com.bluehouseinc.tidal.api.model.owners.Owner;
import com.bluehouseinc.tidal.api.model.tag.Tag;
import com.bluehouseinc.tidal.api.model.tag.TagType;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBar;

@Log4j2
public class JobTagExecutor extends AbstractAPIExecutor {

	public JobTagExecutor(TidalAPI tdl, TidalDataModel mdl, ConfigurationProvider cfgProvider) {
		super(tdl, mdl, cfgProvider);
	}

	@Override
	public void doExecute(ExecutorService executor, ProgressBar bar) {
		getDataModel().getJobTags().keySet().forEach(f -> {
			executor.submit(() -> {
				doProcessJobTag(f, bar);
			});
		});
	}

	@Override
	public void doExecute(ProgressBar bar) {
		getDataModel().getJobTags().keySet().forEach(f -> {
			doProcessJobTag(f, bar);
		});
	}

	@Override
	public String getProgressBarName() {
		return "Job Tag Data";
	}

	@Override
	public int getProgressBarTotal() {
		return getDataModel().getJobTags().size();
	}

	protected void doProcessJobTag(CsvJobTag cvstag, ProgressBar bar) {
		try {
			Tag existing = getTidalApi().getJobTagByName(cvstag.getName());

			if (existing != null) {
				log.debug("doProcessJobTag[" + existing.getName() + "] Skipping already exist");
				return;
			} else {
				Tag jobtagtoadd = new Tag();
				jobtagtoadd.setName(cvstag.getName());
				jobtagtoadd.setType(TagType.BUSINESSACTIVITY);

				Owner owner = getTidalApi().getOwnerByName(getDataModel().getDefaultOwner().getOwnerName());

				if (owner == null) {
					throw new TidalException("Unable to add Job Tag, Unable to locate Owner["+getDataModel().getDefaultOwner().getOwnerName()+"]");
				}

				jobtagtoadd.setOwner(owner);
				jobtagtoadd.setActive("Y");
				jobtagtoadd.setPub(YesNoType.YES);
				TesResult res = getTidalApi().getSession().getServiceFactory().tag().create(jobtagtoadd);

				int newid = res.getResult().getTesObjectid();
				jobtagtoadd.setId(newid);
				getTidalApi().getTags().add(jobtagtoadd);
				log.debug("doProcessJobTag [" + jobtagtoadd.getName() + "] Response ID[" + newid + "][" + res.getResponseData() + "]");
			}

		} finally {
			bar.step();
		}
	}

}
