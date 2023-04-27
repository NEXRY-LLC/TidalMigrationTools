package com.bluehouseinc.dataconverter.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;

import com.bluehouseinc.dataconverter.importers.AbstractCsvImporter;
import com.bluehouseinc.dataconverter.model.csv.NameNewNamePairCsvMapping;
import com.bluehouseinc.dataconverter.model.csv.NameValuePairCsvMapping;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.BaseCvsDependency;
import com.bluehouseinc.dataconverter.model.impl.CsvActionEmail;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvJobClass;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CsvOwner;
import com.bluehouseinc.dataconverter.model.impl.CsvResource;
import com.bluehouseinc.dataconverter.model.impl.CsvRuntimeUser;
import com.bluehouseinc.dataconverter.model.impl.CsvTimeZone;
import com.bluehouseinc.dataconverter.model.impl.CsvVariable;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyJob;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.model.dependency.job.DepLogic;
import com.bluehouseinc.tidal.api.model.dependency.job.DependentJobStatus;
import com.bluehouseinc.tidal.api.model.dependency.job.Operator;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public class TidalDataModel {

	private static final String MAP_RTE = "TIDAL.MapRunTimeUsers";
	private static final String MAP_DEFOWN = "TIDAL.DefaultOwner";
	private static final String MAP_AGT = "TIDAL.MapAgents";
	private static final String TIDALMapAgentDataFile = "TIDAL.MapAgentDataFile";
	private static final String TIDALMapAgentListDataFile = "TIDAL.MapAgentListDataFile";
	private static final String TIDALMapCalendarDataFile = "TIDAL.MapCalendarDataFile";
	private static final String TidalVariableDataFile = "TIDAL.VariableDataFile";

	private static final String MAP_CALNAME = "TIDAL.MapCalendarName";
	private static final String MAP_DEFZONEID = "TIDAL.DefaultTimeZoneID";
	private static final String MAP_TIMZONE = "TIDAL.MapTimeZoneID";

	private static final String TIDALCalendarPrefix = "TIDAL.CalendarPrefix";

	private CsvOwner defaultOwner;

	@Getter(value = AccessLevel.PUBLIC)
	private AbstractConfigProvider cfgProvider;

	@Getter(value = AccessLevel.PUBLIC)
	@Setter(value = AccessLevel.PUBLIC)
	private List<BaseCsvJobObject> jobOrGroups = new ArrayList<>();

	@Getter(value = AccessLevel.PUBLIC)
	private Set<CsvJobClass> jobClasses = new HashSet<>();

	@Getter(value = AccessLevel.PUBLIC)
	private Set<CsvVariable> variables = new HashSet<>();
	@Getter(value = AccessLevel.PUBLIC)
	private Set<CsvCalendar> calendars = new HashSet<>();
	@Getter(value = AccessLevel.PUBLIC)
	private Set<CsvOwner> owners = new HashSet<>();
	@Getter(value = AccessLevel.PUBLIC)
	private List<BaseCvsDependency> dependencies = new ArrayList<>();
	@Getter(value = AccessLevel.PUBLIC)
	private Set<CsvRuntimeUser> runtimeusers = new HashSet<>();
	@Getter(value = AccessLevel.PUBLIC)
	private Set<String> nodes = new HashSet<>();
	@Getter(value = AccessLevel.PUBLIC)
	private Set<String> agentList = new HashSet<>();
	@Getter(value = AccessLevel.PUBLIC)
	private Set<CsvResource> resource = new HashSet<>();
	@Getter(value = AccessLevel.PUBLIC)
	private Set<CsvActionEmail> emailActions = new HashSet<>();
	@Getter(value = AccessLevel.PUBLIC)
	private Set<CsvTimeZone> timeZones = new HashSet<>();

	private Map<String, BaseCsvJobObject> jobOrGroupsMap = null;
	// private Map<String, BaseCsvJobObject> registeredBaseJobs = new HashedMap<>();

	private Map<CsvResource, List<BaseCsvJobObject>> jobResourceJoins = new HashedMap<>();
	private int jobResourceJoinsCounter = 0;

	@Getter(value = AccessLevel.PUBLIC)
	@Setter(value = AccessLevel.PROTECTED)
	List<BaseCsvJobObject> compoundDependnecyJobs = new ArrayList<>(); // Need to register our jobs with compound deps.

	public static TidalDataModel model;

	public TidalDataModel(AbstractConfigProvider cfgProvider) {
		this.cfgProvider = cfgProvider;
		loadVariableData(); // Do this early to make sure they are fully for reference in jobs.
	}

	/**
	 * Singlton of our data model so we can get it from everywhere.. New Concept??
	 * @param cfgProvider
	 * @return
	 */
	public static TidalDataModel instance(AbstractConfigProvider cfgProvider) {

		if(model == null) {
			model = new TidalDataModel(cfgProvider);
		}

		return model;
	}

	public void loadVariableData() {
		String vardatafile = this.cfgProvider.getProvider().getConfigurations().getOrDefault(TidalVariableDataFile, null);

		if (vardatafile != null) {

			File file = new File(vardatafile);

			log.debug("Loading Variable Data File for Processing["+vardatafile+ "]");

			AbstractCsvImporter.fromFile(file, NameValuePairCsvMapping.class).forEach(f ->{
				CsvVariable var = new CsvVariable(f.getName());
				var.setVarValue(f.getValue());

				log.debug("Adding Variable to Data model ["+var.getVarName()+ "]");
				this.addVariable(var);
			});


		} else {
			throw new TidalException("Property Missing: " + TidalVariableDataFile);
		}


	}
	/*
	 * Helper method to add a variable to our model if not existing.
	 *
	 * @param var
	 */
	public void addVariable(CsvVariable var) {
		if (var == null) {
			return;
		}

		if (!this.variables.contains(var)) {
			this.variables.add(var);
		}
	}

	private List<NameNewNamePairCsvMapping> calendarmappingdata;

	/**
	 * Helper method to add a calendar to a job and register it to the datamodel for
	 * you
	 *
	 * @param ajob
	 * @param cal
	 */
	public void addCalendarToJobOrGroup(BaseCsvJobObject ajob, CsvCalendar cal) {
		if (cal == null) {
			return;
		}

		if (StringUtils.isBlank(cal.getCalendarName())) {
			log.error("addCalendarToJobOrGroup Name is Blank, SKIPPING");
			return;
		}

		String calprefix = this.cfgProvider.getProvider().getConfigurations().getOrDefault(TIDALCalendarPrefix, null);

		String calname = cal.getCalendarName();

		if (!StringUtils.isBlank(calprefix)) {
			calname = calprefix + calname;
		}

		if (calname.length() > 64) {
			calname = calname.substring(0, 64);
			log.debug(
					"addCalendarToJobOrGroup Renaming Calendar STUB to 64 characters plus counter [" + calname + "]\n");
		}

		cal.setCalendarName(calname);

		String mapcal =  this.cfgProvider.getProvider().getConfigurations().getOrDefault(MAP_CALNAME, null);
		String calmapping =  this.cfgProvider.getProvider().getConfigurations().getOrDefault(TIDALMapCalendarDataFile, null);

		if (calmapping != null) {

			File file = new File(calmapping);

			if (calendarmappingdata == null) {
				calendarmappingdata = AbstractCsvImporter.fromFile(file, NameNewNamePairCsvMapping.class);
			}

			if (!calendarmappingdata.isEmpty()) {
				final String whyfinalagent = cal.getCalendarName().trim();
				NameNewNamePairCsvMapping mapping = calendarmappingdata.stream()
						.filter(f -> f.getName().trim().equalsIgnoreCase(whyfinalagent)).findAny().orElse(null);

				if (mapping != null) {
					log.debug("addCalendarToJobOrGroup Mapping[" + whyfinalagent + "] to Value[" + mapping.getNewName()
							+ "]");
					cal.setCalendarName(mapping.getNewName());
				}
			}

		} else {
			throw new TidalException("Property Missing: " + TIDALMapCalendarDataFile);
		}

		if (mapcal != null) {
			calname = doReplace(cal.getCalendarName(), mapcal);
			cal.setCalendarName(calname);
		}

		ajob.setCalendar(cal);

		if (!this.calendars.contains(cal)) {
			this.calendars.add(cal);
		}
	}

	private List<NameNewNamePairCsvMapping> agentmappingdata;
	private List<NameNewNamePairCsvMapping> agentlistmappingdata;

	public void addNodeToJobOrGroup(BaseCsvJobObject ajob, String agentname) {
		if (agentname == null) {
			return;
		}

		agentname = doHandleAgentDataMapping(ajob, agentname);

		String mapagt =  this.cfgProvider.getProvider().getConfigurations().getOrDefault(MAP_AGT, null);

		if (mapagt != null) {
			agentname = doReplace(agentname, mapagt);
		}

		String agentlistname = doHandleAgentListDataMapping(ajob, agentname);

		if (agentlistname != null) {
			ajob.setAgentListName(agentlistname);

			if (!this.agentList.contains(agentlistname)) {
				this.agentList.add(agentlistname);
			}

		} else {
			ajob.setAgentName(agentname);

			if (!this.nodes.contains(agentname)) {
				this.nodes.add(agentname);
			}
		}
	}

	private String doHandleAgentDataMapping(BaseCsvJobObject ajob, String agentname) {
		String agentmapping =  this.cfgProvider.getProvider().getConfigurations().getOrDefault(TIDALMapAgentDataFile, null);

		if (agentmapping != null) {
			File file = new File(agentmapping);
			if (agentmappingdata == null) {
				agentmappingdata = AbstractCsvImporter.fromFile(file, NameNewNamePairCsvMapping.class);
			}

			if (!agentmappingdata.isEmpty()) {
				final String whyfinalagent = agentname.trim();

				NameNewNamePairCsvMapping mapping = agentmappingdata.stream()
						.filter(f -> f.getName().trim().equalsIgnoreCase(whyfinalagent)).findAny().orElse(null);

				if (mapping != null) {
					log.debug("doHandleAgentDataMapping Mapping[" + agentname + "] to Value[" + mapping.getNewName()
							+ "]");
					agentname = mapping.getNewName().trim();
				}
			}

		} else {
			throw new TidalException("Property Missing: " + TIDALMapAgentDataFile);
		}

		return agentname;
	}

	private String doHandleAgentListDataMapping(BaseCsvJobObject ajob, String agentname) {

		String agentlistmapping =  this.cfgProvider.getProvider().getConfigurations().getOrDefault(TIDALMapAgentListDataFile,
				null);

		if (agentlistmapping != null) {
			File file = new File(agentlistmapping);
			if (agentlistmappingdata == null) {
				agentlistmappingdata = AbstractCsvImporter.fromFile(file, NameNewNamePairCsvMapping.class);
			}

			if (!agentlistmappingdata.isEmpty()) {
				final String whyfinalagent = agentname.trim();

				NameNewNamePairCsvMapping mapping = agentlistmappingdata.stream()
						.filter(f -> f.getName().trim().equalsIgnoreCase(whyfinalagent)).findAny().orElse(null);

				if (mapping != null) {
					log.debug("addNodeToJobOrGroup Agent List Mapping[" + agentname + "] to Value["
							+ mapping.getNewName() + "]");
					agentname = mapping.getNewName().trim();
					return agentname;
				}
			}

		}

		return null;
	}

	public void addOwnerToJobOrGroup(BaseCsvJobObject ajob, CsvOwner owner) {
		if (ajob == null) {
			return;
		}

		ajob.setOwner(owner);

		if (!this.owners.contains(owner)) {
			this.owners.add(owner);
		}
	}

	public void addResourceToJob(BaseCsvJobObject ajob, CsvResource resource) {
		if (ajob == null) {
			return;
		}

		ajob.getResources().add(resource);

		if (resource.getOwner() == null) {
			resource.setOwner(ajob.getOwner()); /// Just in case.
		}

		if (!this.resource.contains(resource)) {
			this.resource.add(resource);
		}

		if (this.jobResourceJoins.containsKey(resource)) {
			this.jobResourceJoins.get(resource).add(ajob);
		} else {
			List<BaseCsvJobObject> jobs = new ArrayList<>();
			jobs.add(ajob);
			this.jobResourceJoins.put(resource, jobs);
		}

		jobResourceJoinsCounter++;
	}

	public List<BaseCsvJobObject> getModelJobs() {
		return this.jobOrGroups;
	}

	public void addJobToModel(BaseCsvJobObject job) {
		this.jobOrGroups.add(job);
		registerCompoundDependencyJob(job);
	}

	public void registerCompoundDependencyJob(BaseCsvJobObject job) {

		if(job.getCompoundDependency() == null) {
			return;
		}

		if (!this.getCompoundDependnecyJobs().contains(job)) {
			this.getCompoundDependnecyJobs().add(job);
		}
	}

	public CvsDependencyJob addJobDependencyForJobCompletedNormal(BaseCsvJobObject myjob, BaseCsvJobObject depensonme, Integer dateOffset) {
		return this.addJobDependencyForJob(myjob, depensonme, DepLogic.MATCH, Operator.EQUAL, DependentJobStatus.COMPLETED_NORMAL, dateOffset);
	}

	public CvsDependencyJob addJobDependencyForJob(BaseCsvJobObject myjob, BaseCsvJobObject depensonme, DepLogic logic, Operator operator,
			DependentJobStatus status, Integer dateOffset) {

		// Do we not do it this way?
		List<BaseCvsDependency> deps = this.dependencies.stream()
				.filter(dep -> Objects.equals(dep.getJobObject().getId(), myjob.getId()))
				.collect(Collectors.toList());

		// this.dependencies.add(dep);
		// I have existing dependency objects so add to my existing
		// List<BaseDependency> deps = this.dependencies.stream().filter(f ->
		// f.getJobObject().getFullPath().equals(myjob.getFullPath())).collect(Collectors.toList());

		boolean duplicate = false;

		CvsDependencyJob dep = new CvsDependencyJob();
		dep.setJobObject(myjob);
		dep.setDependsOnJob(depensonme);
		dep.setOperator(operator);
		dep.setJobStatus(status);
		dep.setLogic(logic);
		dep.setDateOffset(dateOffset);

		if (deps.isEmpty()) {
			this.dependencies.add(dep);
		} else {

			for (BaseCvsDependency f : deps) {
				if (((CvsDependencyJob) f).getDependsOnJob().getId().equals(depensonme.getId())) {
					// if (((DependencyJob)
					// f).getDependsOnJob().getFullPath().equals(depensonme.getFullPath())) {
					// TODO: Determine if I should throw an error here because this is a job dep loop.
					duplicate = true; /// Cant depend on myself.
					break;
				}
			}

			if (!duplicate) {
				this.dependencies.add(dep);
			}
		}

		return dep;
	}

	public CsvOwner getDefaultOwner() {
		if (this.defaultOwner == null) {
			String name =  this.cfgProvider.getProvider().getConfigurations().getOrDefault(MAP_DEFOWN, "Schedulers");
			this.defaultOwner = new CsvOwner(name);
		}

		return this.defaultOwner;
	}

	private List<String> runtimeuserwithDoamin = new ArrayList<>();

	/**
	 * Helper method to add a run time user to a job that only adds if the user and
	 * domain name are different
	 *
	 * @param ajob
	 * @param rt
	 */
	public void addRunTimeUserToJobOrGroup(BaseCsvJobObject ajob, CsvRuntimeUser rt) {
		if (rt == null) {
			return;
		}


		if (StringUtils.isBlank(rt.getRunTimeUserName())) {
			log.error("Runtime User is blank for JOB[]" + ajob.getFullPath() + "");
			return;
		}
		
		// Handle what we can for domain name splits. This is 
		if( rt.getRunTimeUserName().contains("\\")) {
			String temp = rt.getRunTimeUserName().replace("\\", "/");
			String[] d = temp.split("/");
			rt.setRunTimeUserDomain(d[0]);
			rt.setRunTimeUserName(d[1]);
		}
		

		String maprte =  this.cfgProvider.getProvider().getConfigurations().getOrDefault(MAP_RTE, null);

		if (maprte != null) {
			// Changed this to support user@domain for mappings
			if (StringUtils.isBlank(rt.getRunTimeUserDomain())) {
				rt.setRunTimeUserName(doReplace(rt.getRunTimeUserName(), maprte));
			} else {
				// Do domain version mapping
				String newData = doReplace(rt.getUserAndDomainName(), maprte);

				if (newData.contains("@")) {
					String[] d = newData.split("@");
					rt.setRunTimeUserName(d[0]);
					rt.setRunTimeUserDomain(d[1]);
				}
			}

		}

		ajob.setRuntimeUser(rt);

		if (!this.runtimeuserwithDoamin.contains(rt.getUserAndDomainName())) {
			this.runtimeusers.add(rt);
			this.runtimeuserwithDoamin.add(rt.getUserAndDomainName());
		}

	}

	private String doReplace(String name, String mapdata) {
		if (mapdata == null || name == null) {
			return name;
		}

		List<String> data = new ArrayList<>();

		data = Arrays.asList(mapdata.split("\\s*,\\s*"));

		for (String f : data) {
			String[] nv = f.split("=");
			if (nv.length == 2) {
				if (nv[0].equalsIgnoreCase(name)) {
					log.debug("doReplace [" + name + "] to Value[" + nv[1] + "]");
					return nv[1];

				}
			}
		}
		return name;
	}

	public CsvJobGroup findGroupByName(String name) {
		return findGroupByName(name, this.jobOrGroups);
	}

	private CsvJobGroup findGroupByName(String name, List<BaseCsvJobObject> list) {

		CsvJobGroup found = null;

		for (BaseCsvJobObject c : list) {
			if (c instanceof CsvJobGroup) {
				CsvJobGroup group = (CsvJobGroup) c;
				String gname = group.getName();
				if (gname.equalsIgnoreCase(name)) {
					found = group;
					return found;
				}

				if (!group.getChildren().isEmpty()) {
					found = findGroupByName(name, group.getChildren());

					if (found != null) {
						return found;
					}
				}

			}
		}

		return found;
	}

	public void updateBaseCsvDependencyID(BaseCvsDependency dep, int newId) {
		final String oldId = Integer.toString(dep.getId());
		final String newIds = Integer.toString(newId);
		dep.setId(newId); // Update to the new ID

		// now we need to process every damn one of them.
		compoundDependnecyJobs.forEach(f -> {
			if (f.getCompoundDependencyBuilder().containsValue(oldId)) {
				// We have a reference , update from old id to new id
				f.getCompoundDependencyBuilder().replaceValue(oldId, newIds);
			}
		});

	}

	/*
	 * Ugly but we have to track ID changes on jobs to depdenency as this is the
	 * only ares so far I know of.
	 */
	public void updateBaseCsvJobObjectID(BaseCsvJobObject job, int newid) {
		int oldid = job.getId();
		String path = job.getFullPath();

		// Look for dependy refs.

		if (getDependencies() != null) {
			log.debug("updateBaseCsvJobObjectID OLDID[" + oldid + "] NEW ID[" + newid + "]  BaseDependency COUNT["
					+ getDependencies().size() + "] starting ");

			// List<BaseCvsDependency> mydeps = getDependencies().stream().filter(f ->
			// f.getJobObject().getId() == oldid).collect(Collectors.toList());

			for (BaseCvsDependency dep : getDependencies()) {
				// hmm. need id and depjob id
				int jobdepid = dep.getJobObject().getId();
				if (jobdepid == oldid) {
					log.debug("updateBaseCsvJobObjectID getJobObject[" + oldid + "] NEW ID[" + newid + "] for Job["
							+ path + "]");
					dep.getJobObject().setId(newid); // set to new id
				}

				if (dep instanceof CvsDependencyJob) {
					CvsDependencyJob jd = (CvsDependencyJob) dep;
					int depjobid = jd.getDependsOnJob().getId();
					if (depjobid == oldid) {
						log.debug("updateBaseCsvJobObjectID getDependsOnJob[" + oldid + "] NEW ID[" + newid
								+ "] for Job[" + path + "]");
						jd.getDependsOnJob().setId(newid);
					}
				} else {
					log.debug("UNKNOWN updateBaseCsvJobObjectID getDependsOnJob[" + oldid + "] NEW ID[" + newid
							+ "] for Job[" + path + "]");
				}
			}

			log.debug("updateBaseCsvJobObjectID  OLDID[" + oldid + "] NEW ID[" + newid + "]  BaseDependency COUNT["
					+ getDependencies().size() + "] Completed ");

		} else {
			log.debug("updateBaseCsvJobObjectID DEP Data is missing.");
		}

		log.debug("updateBaseCsvJobObjectID OLDID[" + oldid + "] NEWID[" + newid + "] for Job[" + path + "]");
		job.setId(newid);
		log.debug("updateBaseCsvJobObjectID OLDID[" + oldid + "] NEWID[" + newid + "] for Job[" + path
				+ "] Updating Parent");

		if (!job.getChildren().isEmpty()) {
			job.getChildren().forEach(c -> c.setParentId(newid));
		}

		log.debug("updateBaseCsvJobObjectID OLDID[" + oldid + "] NEWID[" + newid + "] for Job[" + path
				+ "] Updating Parent Complete");
	}

	/**
	 * Returns all registered IBaseJobGroupObject to the model of Class type <J>.
	 *
	 * @param <J>
	 * @param type
	 * @return
	 */
	public <J extends BaseCsvJobObject> List<J> getJobsByType(Class<J> type) {
		List<J> list2 = toFlatListFiltered(getJobOrGroups(), type);
		return list2;
	}

	public BaseCsvJobObject findFirstJobByFullPath(String path) {
		if (jobOrGroupsMap == null) {
			this.jobOrGroupsMap = new HashMap<>();
			ObjectUtils.toFlatStream(this.jobOrGroups).forEach(jobOrGroup -> {
				if (this.jobOrGroupsMap.containsKey(jobOrGroup.getFullPath())) {
					log.debug("already contains key: " + jobOrGroup.getFullPath());
				}
				this.jobOrGroupsMap.put(jobOrGroup.getFullPath(), jobOrGroup);
			});
		}

		if (jobOrGroupsMap.containsKey(path)) {
			return jobOrGroupsMap.get(path);
		}

		return null;
		// return toFlatStream(this.jobOrGroups).filter(f ->
		// f.getFullPath().equals(path)).findFirst().orElse(null);
	}

	public Long getTotalJobGroupCount() {
		return ObjectUtils.toFlatStream(getJobOrGroups()).count();
	}

	/**
	 * Returns every <J> object in the supplied collection , the Node is recursive
	 * returning all objects.
	 *
	 * @param <J>
	 * @param collection
	 * @param type
	 * @return
	 */
	private <J extends BaseCsvJobObject> List<J> toFlatListFiltered(Collection<BaseCsvJobObject> collection,
			Class<J> clazz) {
		return ObjectUtils.toFlatStream(collection).filter(clazz::isInstance).map(clazz::cast)
				.collect(Collectors.toList());
	}

	// /**
	// * Returns a flat stream of all jobs / groups all levels deep.
	// *
	// * @param collection
	// * @return
	// */
	// private Stream<BaseCsvJobObject> toFlatStream(Collection<BaseCsvJobObject>
	// collection) {
	// Stream<BaseCsvJobObject> result = collection.stream().flatMap(parent -> {
	// if ((parent.getChildren().size() > 0)) {
	// return Stream.concat(Stream.of(parent), toFlatStream(parent.getChildren()));
	// } else {
	// return Stream.of(parent);
	// }
	// });
	//
	// return result;
	// }

	public void addOwnerToAction(CsvActionEmail action, CsvOwner owner) {
		if (action == null) {
			return;
		}
		action.setOwner(owner);

		if (!this.owners.contains(owner)) {
			this.owners.add(owner);
		}
	}

	public void addTimeZoneToJob(BaseCsvJobObject job, CsvTimeZone zone) {
		if (zone == null) {
			return;
		}

		if (StringUtils.isBlank(zone.getName())) {
			throw new TidalException("TimeZone name must be set");
		}

		// Not we did not get a supplied zone id , lets setup our default
		if (StringUtils.isBlank(zone.getTimezoneId())) {
			String zoneid =  this.cfgProvider.getProvider().getConfigurations().getOrDefault(MAP_DEFZONEID, "America/New_York");
			zone.setTimezoneId(zoneid);
		}

		String mapzone =  this.cfgProvider.getProvider().getConfigurations().getOrDefault(MAP_TIMZONE, null);

		if (mapzone != null) {
			String zoneidname = doReplace(zone.getName(), mapzone);
			zone.setTimezoneId(zoneidname);
		}

		job.setTimeZone(zone);

		if (!this.timeZones.contains(zone)) {
			this.timeZones.add(zone);
		}
	}

	public void addJobClassToJob(BaseCsvJobObject ajob, CsvJobClass jobclass) {

		if (jobclass == null) {
			return;
		}

		if (StringUtils.isBlank(jobclass.getName())) {
			log.error("JobClass is blank for JOB[" + ajob.getFullPath() + "]");
			return;
		}

		ajob.setJobClass(jobclass);

		if (!this.jobClasses.contains(jobclass)) {
			this.jobClasses.add(jobclass);
		}
	}

}
