package com.bluehouseinc.dataconverter.parsers.tivoli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.parsers.tivoli.model.TivoliDataModel;
import com.bluehouseinc.dataconverter.parsers.tivoli.model.TivoliObject;
import com.bluehouseinc.dataconverter.parsers.tivoli.model.TivoliObject.TaskType;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;

public class TivoliParser extends AbstractParser<TivoliDataModel> {

	public TivoliParser(ConfigurationProvider cfgProvider) {
		super(new TivoliDataModel(cfgProvider));
	}

	private String PARAMS_PATTERN = "^(MAIN_TABLE)\\.(.*\\s\\\".*\\\")";
	private String JOB_LINE_PATTERN = "^(\\w+)#(\\w+)";

	private String jobsPathKey = "tivoli.jobs.path";
	private String paramsPathKey = "tivoli.params.path";

	@Override
	public void parseFile() throws Exception {
		// I guess we can move away from passing a single file path,
		// and instead define path key(s) inside each parser
		Map<String, String> params = this.parseParams();
		this.parseJobs();

	}

	private void parseJobs() throws IOException {
		String filePath = this.getParserDataModel().getConfigeProvider().getProvider().getConfigurations().get(jobsPathKey);
		File file = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;

		// List<TivoliObject> data = new ArrayList<>();

		TivoliObject currentGroup = null;
		while ((line = reader.readLine()) != null) {
			if (line.matches(JOB_LINE_PATTERN)) {
				String groupName = RegexHelper.extractNthMatch(line, JOB_LINE_PATTERN, 0);
				if (currentGroup == null || !Objects.equals(groupName, currentGroup.getName())) {
					currentGroup = new TivoliObject();
					currentGroup.setName(groupName);
					currentGroup.setGroupFlag(true);
					this.getParserDataModel().addDataDuplicateLevelCheck(currentGroup);
					// System.out.println(currentGroup);
					// data.add(currentGroup);
				}

				TivoliObject job = new TivoliObject();
				String name = RegexHelper.extractNthMatch(line, JOB_LINE_PATTERN, 1);
				job.setName(name);
				job.setDoCommand(reader.readLine().trim().split("\\s", 2)[1]);
				job.setStreamLogon(reader.readLine().trim().split("\\s", 2)[1]);
				job.setDescription(reader.readLine().trim().split("\\s", 2)[1]);
				String tasktype = reader.readLine().trim().split("\\s", 2)[1];
				job.setTaskType(TaskType.valueOf(tasktype)); // I do this so I can have hard failure on any new type
																// data.
				job.setRecovery(reader.readLine().trim().split("\\s", 2)[1]);

				currentGroup.addChild(job);

				System.out.println(job.getFullPath());

			}
		}

		reader.close();
	}

	private Map<String, String> parseParams() throws IOException {
		String filePath = this.getParserDataModel().getConfigeProvider().getProvider().getConfigurations().get(paramsPathKey);
		File file = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		Map<String, String> params = new HashMap<>();

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.matches(PARAMS_PATTERN)) {
				String data = RegexHelper.extractNthMatch(line, PARAMS_PATTERN, 1);
				// System.out.println(data);
				String splitData[] = data.split("\\s", 2);
				params.put(splitData[0], splitData[1]);
			}
		}

		reader.close();

		return params;
	}

	@Override
	public IModelReport getModelReporter() {
		// TODO Auto-generated method stub
		return null;
	}

}
