package com.bluehouseinc.dataconverter.parsers.bmc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class DependencyGraphMapper {

	private Map<String, List<BaseCsvJobObject>> OutConditionDataMap = new ConcurrentHashMap<>();
	private Map<String, BaseCsvJobObject> JobDepMapping = new ConcurrentHashMap<>();

	private TidalDataModel datamodel;


	public DependencyGraphMapper(TidalDataModel datamodel) {
		this.datamodel = datamodel;
	}

	/**
	 * In BMC each job can have both INCON and OUTCON objects. OUTCON is this jobs way of
	 * letting anyone know about its complete status. So any job can set an INCON and its this jobs
	 * way of waiting for anyone to set an OUTCON object.
	 *
	 * When this job has an INCON and there are more than one job setting an OUTCON this is OR logic to
	 * BMC
	 *
	 * @param key
	 * @param job - We match on jobs full path as this is the only way to know 100% unique, if this job does not exist in our set
	 *            we add this job to the set that belongs to this key.
	 */

	public void doProcessJobDepGraph(BaseCsvJobObject job, BaseBMCJobOrFolder bmc) {
		String jobkey = bmc.getFullPath();

		if (!JobDepMapping.containsKey(jobkey)) {
			JobDepMapping.put(jobkey, job); // For quicker lookup when traversing the jobs INCON
		}

		// REG our OUTCON lookup map to simplify out dep graph building.
		bmc.getOutConditionData().forEach(f -> {
			if (f.getSIGN().equals("+")) { // If it is to add the OUTCON ONLY
				String key = f.getNAME(); // My OutConditionName;

				if (OutConditionDataMap.containsKey(key)) {
					OutConditionDataMap.get(key).add(job);
				} else {
					List<BaseCsvJobObject> j = new ArrayList<>();
					j.add(job);
					OutConditionDataMap.put(key, j);
				}
			}
		});

	}

	public void doProcessJobDeps(BaseBMCJobOrFolder base) {

		if (base.getName().contains("DEVDLY")) {
			log.debug("");
		}

		if (!base.getInConditionData().isEmpty()) {

			// Way too slow
			BaseCsvJobObject me = getDatamodel().findFirstJobByFullPath(base.getFullPath());

			if (me == null) {
				// Ignore group with no children or parents.. We know we did not process them.
				if (base.getChildren().isEmpty()) {
					log.debug("WARNING Job[" + base.getFullPath() + "] is missing from Dep Graph");
				} else {
					log.error("doProcessJobDeps ERROR Unable to locate Job[" + base.getFullPath() + "]");
				}
				return;
			}

			base.getInConditionData().forEach(incon -> {

				String key = incon.getNAME();

				List<BaseCsvJobObject> depsonjobs = OutConditionDataMap.get(key);

				if (depsonjobs == null) {
					log.error("doProcessJobDeps ERROR Unable to locate any outconditions that match our incondition[" + key + "] for job[" + base.getFullPath() + "]");
				} else {
					depsonjobs.forEach(dep -> {

						Integer dateOffset = null;
						if (incon.getODATE().equals("ODAT")) {
							// Do nothing , default dep
						} else if (incon.getODATE().equals("PREV")) {
							dateOffset = 1;
						} else if (incon.getODATE().equals("****")) {
							dateOffset = 1;
						} else {
							log.error("doProcessJobDeps Job[" + me.getFullPath() + "] that depends on [" + dep.getFullPath() + "] ODATE is UNKNOWN[" + incon.getODATE() + "]");
						}

						if (me.getId() == dep.getId()) {
							// log.debug("ERROR DEP LOOP Job[" + dep.getFullPath() + "] For Job[" +depsonme.getFullPath() + "]");

						} else {
							log.debug("doProcessJobDeps Registering Dependency for Job[" + me.getFullPath() + "] that depends on [" + dep.getFullPath() + "]");
							getDatamodel().addJobDependencyForJobCompletedNormal(me, dep, dateOffset);
						}

					});
				}
			});

		} else {
			// log.debug("doProcessJobDeps Job[" + base.getFullPath() + "] has no INCON to process, skipping");
		}

		if (!base.getChildren().isEmpty()) {
			log.debug("doProcessJobDeps Job[" + base.getFullPath() + "] has childrenm, processing children");
			base.getChildren().forEach(f -> doProcessJobDeps((BaseBMCJobOrFolder) f));
		}

	}
}
