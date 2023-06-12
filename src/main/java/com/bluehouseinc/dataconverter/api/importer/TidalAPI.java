package com.bluehouseinc.dataconverter.api.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.util.StopWatch;

import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.dataconverter.util.ObjectUtils;
import com.bluehouseinc.tidal.api.ServiceFactory;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.impl.services.session.TidalSession;
import com.bluehouseinc.tidal.api.model.actions.email.EmailAction;
import com.bluehouseinc.tidal.api.model.agentlist.AgentList;
import com.bluehouseinc.tidal.api.model.businessunit.BusinessUnit;
import com.bluehouseinc.tidal.api.model.calendar.Calendar;
import com.bluehouseinc.tidal.api.model.dependency.file.FileDependency;
import com.bluehouseinc.tidal.api.model.dependency.job.JobDependency;
import com.bluehouseinc.tidal.api.model.job.BaseJob;
import com.bluehouseinc.tidal.api.model.job.group.JobGroup;
import com.bluehouseinc.tidal.api.model.job.service.jde.User;
import com.bluehouseinc.tidal.api.model.jobclass.JobClass;
import com.bluehouseinc.tidal.api.model.node.Node;
import com.bluehouseinc.tidal.api.model.owners.Owner;
import com.bluehouseinc.tidal.api.model.resource.jobjoin.ResourceJobJoin;
import com.bluehouseinc.tidal.api.model.resource.virtual.VirtualResource;
import com.bluehouseinc.tidal.api.model.service.Service;
import com.bluehouseinc.tidal.api.model.users.Users;
import com.bluehouseinc.tidal.api.model.users.adapter.UserService;
import com.bluehouseinc.tidal.api.model.variable.Variable;
import com.bluehouseinc.tidal.api.model.workgroup.WorkGroup;
import com.bluehouseinc.tidal.api.model.workgroup.runuser.WorkGroupRunUser;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@Setter(value = AccessLevel.PRIVATE)
@Getter(value = AccessLevel.PUBLIC)
public class TidalAPI {
	private TidalSession session;
	private ServiceFactory sf;
	static final StopWatch sw = new StopWatch();
	protected Collection<BaseJob> jobs;
	protected Collection<Variable> variables;
	protected Collection<Calendar> calendars;
	protected Collection<Owner> owners;
	protected Collection<Users> users;
	protected Collection<WorkGroup> workgroups;
	protected Collection<Node> nodes;
	protected Collection<JobDependency> jobdeps;
	protected Collection<FileDependency> filedeps;
	protected Collection<UserService> userService;
	protected Collection<Service> adapters;
	protected Collection<VirtualResource> resources;
	protected Collection<ResourceJobJoin> resourcesJobJoin;
	protected Collection<EmailAction> emailActions;
	protected Collection<BusinessUnit> businessUnits;
	protected Collection<AgentList> agentList;
	protected Collection<JobClass> jobClass;
	protected Collection<WorkGroupRunUser> workRunUsers;

	private Map<String, BaseJob> pathJobMap;

	protected Owner defaultOwner;

	public static String DEFOWNDERNAME = "Schedulers";
	public static String TIDALDefaultOwner = "TIDAL.DefaultOwner";

	ConfigurationProvider cfgProvider;

	TidalApiExecutor apiExecutor;

	public TidalAPI(TidalSession session, ConfigurationProvider cfgProvider) {
		this.session = session;
		this.sf = session.getServiceFactory();
		this.cfgProvider = cfgProvider;
		this.apiExecutor = new TidalApiExecutor(this, cfgProvider);

	}

	void initdata() {

		apiExecutor.execute();

		log.debug("Processing Job Map");
		sw.start();
		pathJobMap = new ConcurrentHashMap<>();
		this.getJobs().forEach(this::doProcessBaseJobMapToLower); // Build our path map.
		sw.stop();

		log.debug("Processing Job Map Completed in [" + sw.getTotalTimeSeconds() + "]");

		String defuser = getCfgProvider().getConfigurations().getOrDefault(TIDALDefaultOwner, DEFOWNDERNAME);

		log.info("\nSetting Default Owner [" + defuser + "]");

		this.defaultOwner = owners.stream().filter(f -> f.getName().equalsIgnoreCase(defuser)).findFirst().orElseThrow(null);

	}

	Users getLoginUser() {
		this.session.login();

		String userid = this.session.getUserSession().getUserId();
		Users user = this.getSession().getServiceFactory().users().get(userid);
		return user;
	}

	public Service getAdapterByGUID(String guid) {
		Optional<Service> existing = getAdapters().stream().filter(f -> f.getGuid().equalsIgnoreCase(guid)).findFirst();
		return existing.get();
	}

	public Users getUserByAccountNameAndDomain(String name, String domain) {
		Optional<Users> existing;
		final Collection<Users> users = getUsers();

		if (StringUtils.isBlank(domain)) {
			existing = users.stream().filter(user -> user.getName().trim().equalsIgnoreCase(name)).findFirst();
		} else {
			existing = users.stream().filter(user -> {
				String tu = user.getName().trim();
				String td = user.getDomain() == null ? "" : user.getDomain().trim();
				if (tu.equalsIgnoreCase(name.trim()) && td.equalsIgnoreCase(domain.trim())) {
					return true;
				}
				return false;
			}).findFirst();
		}

		return existing.orElse(null);
	}

	public Collection<UserService> getUserAdapterServices(Users user) {
		return getUserService().stream().filter(f -> f.getUserid().equals(user.getId())).collect(Collectors.toList());
	}

	public Owner getOwnerByName(String name) {
		return getOwners().stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public Calendar getCalenderByName(String name) {
		return getCalendars().stream().filter(f -> f.getName().trim().equalsIgnoreCase(name.trim())).findFirst().orElse(null);

	}

	public Node getAgentByName(String name) {
		Node existing = getNodes().stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		return existing;
	}

	public VirtualResource getResourceByName(String name) {
		VirtualResource existing = getResources().stream().filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		return existing;
	}

	private List<JobDependency> findDependencyByJobId(int id) {
		return this.getJobdeps().stream().filter(f -> f.getJobid() == id).collect(Collectors.toList());
	}

	public JobDependency findDependencyByJobId(int jobid, int depjobid) {
		return findDependencyByJobId(jobid).stream().filter(f -> f.getDepjobid() == depjobid).findAny().orElse(null);
	}

	public BaseJob findJobByPathToLower(String path) {
		String key = path.toLowerCase();
		log.debug("findJobByPathToLower[" + key + "]");
		BaseJob job = this.pathJobMap.get(key);

		if (job == null) {
			log.debug("findJobByPathToLower[" + key + "] NOTFOUND");
		} else {
			log.debug("findJobByPathToLower Returning[" + job.getFullpath() + "]");
		}

		return job;
	}

	public void doProcessBaseJobMapToLower(BaseJob job) {
		String key = job.getFullpath().toLowerCase();
		log.debug("doProcessBaseJobMapToLower[" + key + "]");
		this.pathJobMap.put(key, job);
		if (job instanceof JobGroup) {
			((JobGroup) job).getChildren().forEach(this::doProcessBaseJobMapToLower);
		}
	}

	public List<ResourceJobJoin> getJobResourcesByJobID(Integer id) {
		List<ResourceJobJoin> reses = new ArrayList<>();

		for (ResourceJobJoin res : getResourcesJobJoin()) {
			int jobid = res.getJobid();

			if (id == jobid) {
				reses.add(res);

			}
		}

		return reses;
		// return getResourcesJobJoin().stream().filter(f ->
		// f.getJobid()==id).collect(Collectors.toList());
	}

	public EmailAction getEmailActionByName(String name) {
		return getEmailActions().stream().filter(f -> f.getName().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	public BusinessUnit getBusinessUnitByName(String name) {
		return getBusinessUnits().stream().filter(f -> f.getName().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	public AgentList getAgentListtByName(String name) {
		return getAgentList().stream().filter(f -> f.getName().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	public JobClass getJobClassByName(String name) {
		return getJobClass().stream().filter(f -> f.getName().equalsIgnoreCase(name)).findAny().orElse(null);
	}

	public WorkGroupRunUser getWorkGroupRunUserById(int groupid, int userid) {
		return getWorkRunUsers().stream().filter(f -> f.getWorkgroupid() == groupid && f.getUsertableid() == userid).findAny().orElse(null);
	}

	/**
	 * 
	 * @param name
	 * @param domain - Can be null but only returns the first name in the list.
	 * @return
	 */
	public Users getFirstRunTimeUserByNameAndDomain(String name, String domain) {

		if (StringUtils.isBlank(name)) {
			return null;
		}

		final List<Users> found = new ArrayList<>();

		getUsers().forEach(u -> {
			if (u.getName().equalsIgnoreCase(name)) {
				found.add(u);
			}
		});

		// If we have a domain match, return that.
		if (!StringUtils.isBlank(domain)) {
			for (Users d : found) {
				if (!StringUtils.isBlank(d.getDomain())) {
					if (d.getDomain().equalsIgnoreCase(domain)) {
						return d;
					}
				}
			}
		} else {
			// Did we find any?
			if (!found.isEmpty()) {
				return found.get(0); // Just return the first on in the list
			}
		}

		return null;
	}
}
