package com.bluehouseinc.dataconverter.api.importer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.bluehouseinc.dataconverter.api.importer.exec.CalendarExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.DependencyExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.EmailActionExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.JobClassExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.JobCompoundDepExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.JobGroupExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.JobResourceJoinExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.OwnerExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.ResourceExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.RunTimeUserExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.TimeZoneExecutor;
import com.bluehouseinc.dataconverter.api.importer.exec.VariableExecutor;
import com.bluehouseinc.dataconverter.api.reporters.TidalModelReporterData;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.api.impl.Tidal;
import com.bluehouseinc.tidal.api.impl.services.session.Credentials;
import com.bluehouseinc.tidal.api.impl.services.session.TidalSession;
import com.bluehouseinc.tidal.api.model.agentlist.AgentList;
import com.bluehouseinc.tidal.api.model.node.Node;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;
import me.tongfei.progressbar.ProgressBarBuilder;


@Log4j2
@Component
public class TidalImporter {


	private static final String  TIDAL_USE_CONTAINER = "TIDAL.UseContainer";


	static final StopWatch sw = new StopWatch();
	static final ProgressBarBuilder pbb = new ProgressBarBuilder();
	private TidalAPI tidal;

	public TidalImporter() {
	}

	private static TidalSession tidalSession = null;
	ConfigurationProvider cp;

	@Autowired
	public void setConfigurationProvider(ConfigurationProvider cfgProvider) {
		this.cp = cfgProvider;
	}

	private void initAPI() {

		if (tidal == null) {
			cp.readConfigurationFile();
			Credentials access = createCredentials();
			tidalSession = Tidal.gi(access).getTidalSession();
			this.tidal = new TidalAPI(tidalSession, cp);

		}
	}

	private Credentials createCredentials() {
		final String url = cp.getConfigurations().get("tidal.url");
		final String userName = cp.getConfigurations().get("tidal.user");
		final String password = cp.getConfigurations().get("tidal.password");
		final Integer port = Integer.valueOf(cp.getConfigurations().get("tidal.port"));
		final String dsp = cp.getConfigurations().get("tidal.dsp");

		return Credentials.builder().url(url).userName(userName).password(password).port(port).dsp(dsp).build();
	}

	public void testLogin() {

		initAPI();
		this.tidal.getSession().login();

		log.info("Logged in as " + this.tidal.getLoginUser().getFullname());

	}

	/**
	 * Returns the Core API Object holds cached objects on load.
	 *
	 * @return
	 */
	public TidalAPI doInitgetTidal() {
		if (this.tidal == null) {
			initAPI();

			this.tidal.initdata();

		}
		return this.tidal;
	}

	private boolean validateNodeExist(String name) {

		for (Node node : tidal.getNodes()) {
			if (node.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;
		// throw new TidalException("Node missing ["+name+"] please add to your TIDAL
		// environment and try again");
	}

	private boolean validateAgentListExist(String name) {

		for (AgentList list : tidal.getAgentList()) {
			if (list.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}

		return false;

	}

	public void processDataModel(TidalDataModel model) {

		doProcessContainer(model);

		doInitgetTidal();
		// Need to think through functionality of importer.
		// TODO: Add variable replacement functionality.
		// Look too some older project that used reflection to locate variables, I have
		// code for this in the utility project
		// 1. We need to first add or update our dependent objects such as users,
		// variables calendars
		// 2. We need to then build our api model objects using the csv model objects
		// 3. Then we need to process the api model objects

		log.debug("Checking Agents are present in TIDAL");
		StringBuilder b = new StringBuilder();


		model.getNodes().stream().sorted().forEach(n -> {

			if (!validateNodeExist(n)) {
				b.append("\n" + n);
			}

		});

		model.getAgentList().stream().sorted().forEach(l -> {

			if (!validateAgentListExist(l)) {
				b.append("\nLIST -> " + l);
			}
		});

		if (!StringUtils.isBlank(b.toString())) {
			throw new TidalException("The following nodes or agentlist must be added prior to running this migration process" + b.toString() );
		}

		log.debug("Checking Agents and Agent List are present in TIDAL - Completed");

		new TimeZoneExecutor(tidal, model, cp).execute();

		new ResourceExecutor(tidal, model, cp).execute();

		new RunTimeUserExecutor(tidal, model, cp).execute();

		new CalendarExecutor(tidal, model, cp).execute();

		new OwnerExecutor(tidal, model, cp).execute();

		new VariableExecutor(tidal, model, cp).execute();

		model.getEmailActions().forEach(e -> VariableUtil.doProcessVariables(e, tidal.getVariables()));

		new EmailActionExecutor(tidal, model, cp).execute();

		new JobClassExecutor(tidal, model, cp).execute();

		model.getModelJobs().forEach(j -> VariableUtil.doProcessVariables(j, tidal.getVariables()));


		new JobGroupExecutor(tidal, model, cp).execute();

		new DependencyExecutor(tidal, model, cp).execute();

		new JobCompoundDepExecutor(tidal, model, cp).execute();

		new JobResourceJoinExecutor(tidal, model, cp).execute();


		// sw.prettyPrint();

	}

	protected void doProcessContainer(TidalDataModel model) {

		String concount = model.getCfgProvider().getProvider().getConfigurations().getOrDefault(TIDAL_USE_CONTAINER, null);

		Map<String, CsvJobGroup> datamap = new HashMap<>();

		boolean processcontainer = !StringUtils.isBlank(concount);

		if (processcontainer) {
			boolean isnumber = isStringInt(concount);

			if (isnumber) {
				model.getJobOrGroups().forEach(f -> {

					String name = f.getName();
					int le = Integer.valueOf(concount);

					if (name.length() < le) {
						le = name.length();
					}

					String key = f.getName().substring(0, le).toUpperCase();

					if (datamap.containsKey(key)) {
						datamap.get(key).addChild(f);
					} else {
						CsvJobGroup c = new CsvJobGroup();
						c.setName(key);
						 model.addCalendarToJobOrGroup(c, new CsvCalendar("Daily"));
						c.addChild(f);
						datamap.put(key, c);
					}
				});

			} else {
				// Is a forced single parent object
				String containername = concount;
				CsvJobGroup c = new CsvJobGroup();
				c.setName(containername);
				model.addCalendarToJobOrGroup(c, new CsvCalendar("Daily"));

				model.getJobOrGroups().forEach(f -> {
					c.addChild(f);
				});

				datamap.put(containername, c);
			}

		}

		if (!datamap.isEmpty()) {
			List<BaseCsvJobObject> mapped = new LinkedList<>();

			datamap.entrySet().stream().forEach(f -> {
				mapped.add(f.getValue());
			});

			model.setJobOrGroups(mapped);

		}
	}

	public void printWorkToDo(TidalDataModel model) {

		log.info("TimeZone Objects To Process [" + model.getTimeZones().size() + "]");
		log.info("Resource Objects To Process [" + model.getResource().size() + "]");
		log.info("Runtime User Objects To Process [" + model.getRuntimeusers().size() + "]");
		log.info("Calendar Objects To Process [" + model.getCalendars().size() + "]");
		log.info("Owner Objects To Process [" + model.getOwners().size() + "]");
		log.info("Variable Objects To Process [" + model.getVariables().size() + "]");
		log.info("Email Action Objects To Process [" + model.getEmailActions().size() + "]");
		log.info("Job/Group Objects To Process [" + model.getTotalJobGroupCount() + "]");
		log.info("Dependency Objects To Process [" + model.getDependencies().size() + "]");
		log.info("Compound Dependency Job Objects To Process [" + model.getCompoundDependnecyJobs().size() + "]");
		log.info("Job Resource Objects To Process [" + model.getJobResourceJoinsCounter() + "]");
		log.info("Job Class Objects To Process [" + model.getJobClasses().size() + "]");
		
		TidalModelReporterData.getReporters().forEach(f -> {
			

			log.trace("#####################################" + f.getClass().getSimpleName() +"#####################################");
			
			f.doReport(model);
			
			log.trace("#####################################" + f.getClass().getSimpleName() +"#####################################");
			
			
		});
	}


	private boolean isStringInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
}
