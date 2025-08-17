package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model;

import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.TivoliMainframeOPCConfigProvider;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl.CA7BaseJobObject;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

/**
 * Data model for Tivoli Mainframe OPC parser.
 * Contains all parsed applications, operations, dependencies, resources, and user fields.
 */
@Log4j2
@Data
@EqualsAndHashCode(callSuper = false)
public class TivoliMainframeOPCDataModel extends BaseParserDataModel<CA7BaseJobObject, TivoliMainframeOPCConfigProvider> implements IParserModel {

	public TivoliMainframeOPCDataModel(ConfigurationProvider cfgProvider) {
		super(new TivoliMainframeOPCConfigProvider(cfgProvider));
	}

	@Override
	public void doPostJobDependencyJobObject(List<CA7BaseJobObject> jobs) {

		TidalDataModel tidal = getTidal();
		
		List<BaseCsvJobObject> current = tidal.getAndResetJobObject();
		// List<BaseCsvJobObject> newlist = new ArrayList<BaseCsvJobObject>();
		//getTidal().getJobOrGroups().clear();

		// Group by First level.
		current.forEach(group -> {
			String name = group.getName();

			if (name.startsWith("#")) {
				name = name.substring(1, name.length());
				group.setName(name);
				group.getFullPath();
			}

			long hashCount = name.chars().filter(ch -> ch == '#').count();

			if (hashCount >= 1) {

				int firstHashIndex = name.indexOf('#');

				String newgroup = name.substring(0, firstHashIndex);
				String renamed = name.substring(firstHashIndex + 1); // Skip the # character

				CsvJobGroup newparent = tidal.findGroupByName(newgroup);

				if (newparent == null) {
					newparent = new CsvJobGroup();



					// Duplicate data.
					// ObjectUtils.copyMatchingFields(group, newparent);
					group.setName(renamed);
					newparent.setName(newgroup);
					newparent.setAgentName(group.getAgentName());

					CsvCalendar cal = group.getCalendar();

					if (cal == null) {
						this.getTidal().addCalendarToJobOrGroup(newparent, new CsvCalendar("Daily"));
					} else {
						newparent.setCalendar(group.getCalendar());
					}
					group.setInheritAgent(true);
					group.setInheritCalendar(true);
					group.setCalendar(null);
					group.setAgentListName(null);
					
					tidal.addJobToModel(newparent);

				}

				newparent.addChild(group);
				newparent.getFullPath();
				group.getFullPath(); // ? Needed

			} else {
				tidal.addJobToModel(group);
			}

		});

	}

	@Override
	public BaseVariableProcessor<CA7BaseJobObject> getVariableProcessor(TidalDataModel model) {
		return null;
	}

	@Override
	public ITransformer<List<CA7BaseJobObject>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return new TivoliToTidalTransformer(model, this);
	}

	@Override
	public void doProcessJobDependency(List<CA7BaseJobObject> jobs) {
		jobs.forEach(j -> j.getChildren().forEach(c -> doProcessJobDepdency(j, (CA7BaseJobObject) c)));

	}

	public void doProcessJobDepdency(CA7BaseJobObject myapp, CA7BaseJobObject job) {

		final BaseCsvJobObject jobwithdep = this.getTidal().findFirstJobByFullPath(job.getFullPath());

		if (jobwithdep == null) {
			log.error("Unable to find matching job for dependency creation[" + job.getFullPath() + "]");
			return;
		}

		job.getDependencies().forEach(d -> {
			// PREADID == Look into this application vs my own.
			String preadid = d.getPredecessorApplicationId();
			Integer id = d.getPredecessorOperationNumber();

			if (StringUtils.isBlank(preadid)) {
				CA7BaseJobObject jobdependsonme = myapp.findJobByOpNo(id);

				if (jobdependsonme == null) {
					// issues.
					log.error("CA7 - Unable to find dependency for job ID[" + id + "] for job[" + jobwithdep.getFullPath() + "] in my current location");
				} else {
					BaseCsvJobObject cvsjobdependsonme = this.getTidal().findFirstJobByFullPath(jobdependsonme.getFullPath());

					if (cvsjobdependsonme == null) {
						log.error("CVS - Unable to find dependency job[" + jobdependsonme.getFullPath() + "] in my current location");
					} else {
						// We have our dependency
						getTidal().addJobDependencyForJobCompletedNormal(jobwithdep, cvsjobdependsonme, null);
					}
				}
			} else {
				// go Find me in this group.
				CA7BaseJobObject lookinthisapp = this.getBaseObjectByName(preadid);

				if (lookinthisapp == null) {
					log.error("Unable to find an Application with name[" + preadid + "] ");
					return;
				} else {

					String objectpath = null;

					if (id == null) {
						objectpath = lookinthisapp.getFullPath(); // We want the group.
					} else {

						CA7BaseJobObject foundremotejob;
						foundremotejob = lookinthisapp.findJobByOpNo(id);

						if (foundremotejob == null) {
							log.error("Unable to find dependency for job ID[" + id + "] in Application [" + lookinthisapp.getFullPath() + "]n");
							return;
						} else {
							objectpath = foundremotejob.getFullPath();
						}
					}

					if (objectpath == null) {
						// Unable to find an object based on path.
						log.error("ERROR - Unable any job that matches " + d.toString());

					} else {
						BaseCsvJobObject mycvsjobdep = this.getTidal().findFirstJobByFullPath(objectpath);

						if (mycvsjobdep == null) {
							log.error("CVS - Unable to find Application -> Job[" + lookinthisapp.getFullPath() + "]");
						} else {
							// We have our dependency
							getTidal().addJobDependencyForJobCompletedNormal(jobwithdep, mycvsjobdep, null);
						}
					}

				}
			}
		});
	}

	@Override
	public void doPostTransformJobObjects(List<CA7BaseJobObject> jobs) {
		// TODO Auto-generated method stub

	}
}
