package com.bluehouseinc.dataconverter.parsers.bmc.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.BaseVariableProcessor;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvActionEmail;
import com.bluehouseinc.dataconverter.parsers.IParserModel;
import com.bluehouseinc.dataconverter.parsers.bmc.model.jobs.BaseBMCJobOrFolder;
import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.DoMailData;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.bluehouseinc.transform.ITransformer;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false)
public class BMCDataModel extends BaseParserDataModel<BaseBMCJobOrFolder,BMCConfigProvider> implements IParserModel {
	
	private Map<String, DoMailData> uniqueEmails = new HashMap<>();

	@Getter(value =  AccessLevel.PRIVATE)
	@Setter(value = AccessLevel.PRIVATE)
	private DependencyGraphMapper depgraph;
	
	public BMCDataModel(ConfigurationProvider cfgProvider) {
		super(new BMCConfigProvider(cfgProvider));
	}

	@Override
	public BaseVariableProcessor<BaseBMCJobOrFolder> getVariableProcessor(TidalDataModel model) {
		return new BMCVariableProcessor(model);
	}

	@Override
	public ITransformer<List<BaseBMCJobOrFolder>, TidalDataModel> getJobTransformer(TidalDataModel model) {
		return new BMCToTIDALTransformer(model, this);
	}

	private Map<BMCJobTypes, List<BaseBMCJobOrFolder>> jobtypes = new Hashtable<>();

	public DependencyGraphMapper getDependencyGraphMapper() {
		if(depgraph == null) {
			depgraph = new DependencyGraphMapper(this);
		}
		
		return depgraph;
	}

	@Override
	public void doPostTransformJobObjects(List<BaseBMCJobOrFolder> jobs) {
		if (this.getConfigeProvider().includeEmailActions()) {
			jobs.forEach(d -> doProcessBuildEmailAction(d));
		}

		String dosetreporting = this.getConfigeProvider().reportDoSetVarDataLocation();

		if (!StringUtils.isBlank(dosetreporting)) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(dosetreporting, true));
				Map<String, String> mydata = new HashMap<>();
				jobs.forEach(d -> doProcessReportDoSetData(d, mydata));

				for (String key : mydata.keySet()) {
					String value = mydata.get(key);
					writer.append(key + "," + value + "\n");
				}

			} catch (Exception e) {
				throw new TidalException(e);
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}

		}
	}
	
	@Override
	public void doProcessJobDependency(List<BaseBMCJobOrFolder> jobs) {
		
		if (getConfigeProvider().includeConditions()) {
			jobs.forEach(f -> this.getDependencyGraphMapper().doProcessJobDeps(f));
		}
	}


	public void addJobByType(BMCJobTypes type, BaseBMCJobOrFolder base) {

		List<BaseBMCJobOrFolder> jobs = jobtypes.get(type);

		if (jobs == null) {
			List<BaseBMCJobOrFolder> n = new ArrayList<>();
			n.add(base);
			jobtypes.put(type, n);
		} else {
			jobtypes.get(type).add(base);
		}

	}

	private void doProcessBuildEmailAction(BaseBMCJobOrFolder bmc) {

		bmc.getDoMailData().forEach(doo -> {
			String key = doo.getSUBJECT() + doo.getMESSAGE() + doo.getDEST() + doo.getCCDEST();

			if (!uniqueEmails.keySet().contains(key)) {
				uniqueEmails.put(key, doo);

				CsvActionEmail action = new CsvActionEmail();
				action.setMessage(doo.getMESSAGE());
				action.setSubject(doo.getSUBJECT());
				action.setToEmailAddresses(doo.getDEST() + "," + doo.getCCDEST());

				getTidal().addOwnerToAction(action, getTidal().getDefaultOwner());

				action.setName(key);
				this.getTidal().getEmailActions().add(action);
			}

		});

		if (!bmc.getChildren().isEmpty()) {
			bmc.getChildren().forEach(d -> doProcessBuildEmailAction((BaseBMCJobOrFolder) d));
		}
	}

	private void doProcessReportDoSetData(BaseBMCJobOrFolder bmc, Map<String, String> data) {

		if (!bmc.getDoSetVarData().isEmpty()) {
			bmc.getDoSetVarData().forEach(f -> {
				String name = VariableFixUtil.replaceBMCVariableInstructionKey(f.getNAME());
				String value = f.getVALUE();

				if (!StringUtils.isBlank(value)) {
					if (!data.containsKey(name)) {
						data.put(name, value);
					}
				}
			});
		}

		bmc.getChildren().forEach(f -> doProcessReportDoSetData((BaseBMCJobOrFolder) f, data));
	}

	@Override
	public void doPostJobDependencyJobObject(List<BaseBMCJobOrFolder> jobs) {
		// TODO Auto-generated method stub
		
	}
}
