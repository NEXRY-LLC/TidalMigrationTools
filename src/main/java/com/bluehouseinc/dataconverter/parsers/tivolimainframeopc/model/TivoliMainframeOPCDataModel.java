package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.bluehouseinc.dataconverter.model.BaseJobOrGroupObject;
import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.CA7JobNameParser;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.CA7JobNameParser.ParsedJobName;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.TivoliMainframeOPCConfigProvider;
import com.bluehouseinc.dataconverter.parsers.tivolimainframeopc.model.jobs.impl.CA7BaseJobObject;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
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

	// @Override
	// public void doPostJobDependencyJobObject(List<CA7BaseJobObject> jobs) {
	//
	// TidalDataModel tidal = getTidal();
	//
	// List<BaseCsvJobObject> current = tidal.getAndResetJobObject();
	// // List<BaseCsvJobObject> newlist = new ArrayList<BaseCsvJobObject>();
	// // getTidal().getJobOrGroups().clear();
	//
	// // Group by First level.
	// current.forEach(group -> {
	// String name = group.getName();
	//
	// if (name.startsWith("#")) {
	// name = name.substring(1, name.length());
	// group.setName(name);
	// group.getFullPath();
	// }
	//
	// long hashCount = name.chars().filter(ch -> ch == '#').count();
	//
	// if (hashCount >= 1) {
	//
	// if (name.contains("CLAMBU2#REQ1#02")) {
	// name.getBytes();
	// }
	//
	// int firstHashIndex = name.indexOf('#');
	//
	// String[] parts = name.split("#");
	//
	// ParsedJobName parsedname = CA7JobNameParser.parseJobName(name);
	//
	// String groupshortname = name.substring(0, 3);
	// String containername = name.substring(0, firstHashIndex);
	// String renamed = name.substring(firstHashIndex + 1); // Skip the # character
	//
	// group.setName(renamed);
	// CsvJobGroup newcontainershortname = tidal.findGroupByName(groupshortname);
	// CsvJobGroup newcontainer = tidal.findGroupByName(containername);
	//
	// if (newcontainershortname == null) {
	// newcontainershortname = new CsvJobGroup();
	// newcontainershortname.setName(groupshortname);
	// newcontainershortname.setInheritAgent(false);
	// newcontainershortname.setInheritCalendar(false);
	// this.getTidal().addCalendarToJobOrGroup(newcontainershortname, new CsvCalendar("Daily"));
	// tidal.addJobToModel(newcontainershortname);
	//
	// }
	//
	// if (newcontainer == null) {
	// newcontainer = new CsvJobGroup();
	//
	// // Duplicate data.
	// // ObjectUtils.copyMatchingFields(group, newparent);
	//
	// newcontainer.setName(containername);
	// newcontainer.setAgentName(group.getAgentName());
	//
	// CsvCalendar cal = group.getCalendar();
	//
	// if (cal == null) {
	// this.getTidal().addCalendarToJobOrGroup(newcontainer, new CsvCalendar("Daily"));
	// } else {
	// newcontainer.setCalendar(group.getCalendar());
	// }
	// group.setInheritAgent(true);
	// group.setInheritCalendar(true);
	// group.setCalendar(null);
	// group.setAgentListName(null);
	//
	// newcontainershortname.addChild(newcontainer);
	//
	// }
	//
	// newcontainer.addChild(group);
	//
	// } else {
	// tidal.addJobToModel(group);
	// }
	//
	// });
	//
	// }

	@Override
	public void doPostJobDependencyJobObject(List<CA7BaseJobObject> jobs) {
		TidalDataModel tidal = getTidal();
		List<BaseCsvJobObject> currentContainers = tidal.getAndResetJobObject();

		for (BaseCsvJobObject container : currentContainers) {
			processContainer(container, tidal);
		}
	}

	/**
	 * Processes a single container, creating hierarchy if needed
	 */
	private void processContainer(BaseCsvJobObject container, TidalDataModel tidal) {
		String containerName = cleanContainerName(container.getName());

		if (containerName.contains("AIMRAC54")) {
			containerName.getBytes();
		}
		if (!hasHashDelimiter(containerName)) {
			// Simple container with no hierarchy - add directly
			tidal.addJobToModel(container);
			return;
		}

		try {
			// Normalize container name (remove trailing segments if more than 2 hashes)
			//String normalizedName = normalizeContainerName(containerName);

			// Parse the hierarchy from normalized name
			String topgroupname = parseContainerHierarchy(containerName);

			// Create the hierarchy: topLevel -> parent -> finalContainer
			CsvJobGroup topLevelGroup = getOrCreateTopLevelGroup(topgroupname, tidal);
			
			setParentGroup(topgroupname, topLevelGroup, container, tidal);
			//CsvJobGroup finalContainer = getOrCreateFinalContainer(hierarchy.containerName, parentGroup, container, tidal);

		} catch (Exception e) {
			log.error("Error processing container [{}]: {}", container.getName(), e.getMessage());
			// Fallback: add as simple container
			tidal.addJobToModel(container);
		}
	}

	/**
	 * Removes leading # if present
	 */
	private String cleanContainerName(String name) {
		if (name != null && name.startsWith("#")) {
			return name.substring(1);
		}
		return name;
	}

	/**
	 * Checks if container name contains hash delimiters
	 */
	private boolean hasHashDelimiter(String name) {
		return name != null && name.contains("#");
	}

//	/**
//	 * Normalizes container name by removing everything after the second hash
//	 * Examples:
//	 * - "ACHM#EXRY#01" -> "ACHM#EXRY"
//	 * - "ACHM#EXRY#02" -> "ACHM#EXRY"
//	 * - "ACHM#EXRY" -> "ACHM#EXRY" (unchanged)
//	 * - "SIMPLE" -> "SIMPLE" (unchanged)
//	 */
//	private String normalizeContainerNames(String containerName) {
//		if (containerName == null || containerName.isEmpty()) {
//			return containerName;
//		}
//
//		// Check configuration setting
//		boolean consolidateVariants = false;
//
//		if (!consolidateVariants) {
//			return containerName; // Return original name unchanged
//		}
//
//		// Count hashes
//		long hashCount = containerName.chars().filter(ch -> ch == '#').count();
//
//		if (hashCount <= 1) {
//			return containerName; // Keep as-is if 0 or 1 hash
//		}
//
//		// Find second hash position
//		int firstHashPos = containerName.indexOf('#');
//		int secondHashPos = containerName.indexOf('#', firstHashPos + 1);
//
//		if (secondHashPos == -1) {
//			return containerName; // Only 1 hash found, keep as-is
//		}
//
//		// Return everything up to (but not including) the second hash
//		return containerName.substring(0, secondHashPos);
//	}

	/**
	 * Parses container hierarchy from normalized name
	 */
	private String parseContainerHierarchy(String normalizedName) {
		if (normalizedName == null || normalizedName.trim().isEmpty()) {
			throw new IllegalArgumentException("Container name cannot be null or empty");
		}

		int firstHashPos = normalizedName.indexOf('#');

		if (firstHashPos == -1) {
			throw new IllegalArgumentException("Expected hash delimiter in: " + normalizedName);
		}

		if (firstHashPos == 0) {
			throw new IllegalArgumentException("Container name cannot start with #: " + normalizedName);
		}

		if (firstHashPos == normalizedName.length() - 1) {
			throw new IllegalArgumentException("Container name cannot end with #: " + normalizedName);
		}

		String beforeHash = normalizedName.substring(0, firstHashPos);


		return beforeHash;
	}

	/**
	 * Gets or creates the top-level group (e.g., "ACH")
	 */
	private CsvJobGroup getOrCreateTopLevelGroup(String groupName, TidalDataModel tidal) {
		CsvJobGroup existing = tidal.findGroupByName(groupName);

		if (existing != null) {
			return existing;
		}

		// Create new top-level group
		CsvJobGroup topGroup = new CsvJobGroup();
		topGroup.setName(groupName);
		topGroup.setInheritAgent(false);
		topGroup.setInheritCalendar(false);
		tidal.addNodeToJobOrGroup(topGroup, "ASYS");
		tidal.addCalendarToJobOrGroup(topGroup, new CsvCalendar("Daily"));
		tidal.addJobToModel(topGroup);

		return topGroup;
	}

	/**
	 * Gets or creates the parent group (e.g., "ACHM")
	 */
	private void setParentGroup(String groupName, CsvJobGroup topLevelGroup, BaseCsvJobObject sourceContainer, TidalDataModel tidal) {
//		CsvJobGroup existing = tidal.findGroupByName(groupName);
//
//		if (existing != null) {
//			return;
//		}

		// Create new parent group
		//CsvJobGroup parentGroup = new CsvJobGroup();
		//parentGroup.setName(groupName);
		//parentGroup.setInheritAgent(true);
		//parentGroup.setInheritCalendar(true);
		//tidal.addCalendarToJobOrGroup(parentGroup, new CsvCalendar("Daily"));
		// Add to top-level group
		topLevelGroup.addChild(sourceContainer);
	}

//	/**
//	 * Gets or creates the final container (e.g., "EXRY")
//	 */
//	private CsvJobGroup getOrCreateFinalContainer(String containerName, CsvJobGroup parentGroup, BaseCsvJobObject sourceContainer, TidalDataModel tidal) {
//		// Look for existing container in parent group
//		if (parentGroup.getChildren() != null) {
//			for (Object child : parentGroup.getChildren()) {
//				if (child instanceof CsvJobGroup) {
//					CsvJobGroup childGroup = (CsvJobGroup) child;
//					if (containerName.equals(childGroup.getName())) {
//						// Move all jobs from the original container to the final consolidated container
//						moveAllJobs(sourceContainer, childGroup, tidal);
//						return childGroup;
//					}
//				}
//			}
//		}
//		// If we don't already exist in our dataset, simply rename the inbound container to the name we want.
//
//		CsvJobGroup finalContainer = (CsvJobGroup) sourceContainer;
//		finalContainer.setName(containerName);
//
//		// // Create new final container
//		// CsvJobGroup finalContainer = new CsvJobGroup();
//		// finalContainer.setName(containerName);
//		//
//		// if(Objects.isNull(sourceContainer.getAgentName())) {
//		// finalContainer.setInheritAgent(true);
//		// }else {
//		// finalContainer.setAgentName(sourceContainer.getAgentName());
//		// finalContainer.setInheritAgent(false);
//		// }
//		//
//		// // Handle calendar and inheritance
//		// CsvCalendar calendar = sourceContainer.getCalendar();
//		//
//		// if (calendar != null) {
//		// tidal.addCalendarToJobOrGroup(finalContainer, calendar);
//		// } else {
//		// tidal.addCalendarToJobOrGroup(finalContainer, new CsvCalendar("Daily"));
//		// }
//		//
//		// finalContainer.setInheritCalendar(false);
//
//		// Add to parent group
//		parentGroup.addChild(finalContainer);
//
//		return finalContainer;
//	}

//	private void doSetupAgentJobLogic(BaseCsvJobObject job, CsvJobGroup targetContainer, TidalDataModel tidal) {
//
//		if (targetContainer.getAgentName() != null) {
//			if (job.getAgentName() == null || job.getAgentName().equalsIgnoreCase(targetContainer.getAgentName())) {
//				job.setAgentName(null);
//				job.setInheritAgent(true);
//			}
//		} else {
//			targetContainer.setInheritAgent(true);
//			if (job.getAgentName() == null) {
//				job.setInheritAgent(true);
//			}
//		}
//
//	}

	/**
	 * Moves all jobs from source container to target container
	 */

//	private void moveAllJobs(BaseCsvJobObject sourceContainer, CsvJobGroup targetContainer, TidalDataModel tidal) {
//
//		if (sourceContainer.getChildren() != null && !sourceContainer.getChildren().isEmpty()) {
//			for (BaseJobOrGroupObject job : sourceContainer.getChildren()) {
//
//				BaseCsvJobObject cvsobject = (BaseCsvJobObject) job;
//
//				if (cvsobject.getName().contains("OPCOGDGA")) {
//					cvsobject.getName();
//				}
//
//				doSetupAgentJobLogic(cvsobject, targetContainer, tidal);
//
//				if (!targetContainer.addChildTest(job)) {
//					String name = cvsobject.getName();
//					String ch = parseContainerHierarchy(sourceContainer.getName());
//					cvsobject.setName(ch + "_" + name);
//
//					targetContainer.addChild(cvsobject);
//				}
//			}
//
//			log.debug("Moved {} jobs from [{}] to consolidated container [{}]", sourceContainer.getChildren().size(), sourceContainer.getName(), targetContainer.getName());
//
//		}
//	}

//	/**
//	 * Simple data class to hold container hierarchy components
//	 */
//	private static class ContainerHierarchy {
//		final String topLevelGroupName; // "ACH" from "ACHM#EXRY"
//		final String parentGroupName; // "ACHM" from "ACHM#EXRY"
//		final String containerName; // "EXRY" from "ACHM#EXRY"
//
//		ContainerHierarchy(String topLevel, String parent, String container) {
//			this.topLevelGroupName = topLevel;
//			this.parentGroupName = parent;
//			this.containerName = container;
//		}
//	}

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
