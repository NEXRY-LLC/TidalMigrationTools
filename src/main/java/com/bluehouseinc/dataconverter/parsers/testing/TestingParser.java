package com.bluehouseinc.dataconverter.parsers.testing;

import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.model.TidalDataModel;
import com.bluehouseinc.dataconverter.model.impl.CsvCalendar;
import com.bluehouseinc.dataconverter.model.impl.CsvOSJob;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyJob;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.expressions.ExpressionType;
import com.bluehouseinc.expressions.ExpressionUtil;
import com.bluehouseinc.tidal.api.model.dependency.job.ExitCodeOperator;

public class TestingParser extends AbstractParser<TestingDataModel> {
	TidalDataModel model;

	public TestingParser(ConfigurationProvider cfgProvider) {
		super(new TestingDataModel(cfgProvider));
	}

	@Override
	public void parseFile() throws Exception {

		model = getParserDataModel().getTidal();

		CsvOSJob mainjob = addJobToDataModel("999 - COMPOUND TESTING");
		CsvOSJob depjobone = addJobToDataModel("999 - COMPOUND TESTING - Dep Job One");
		CsvOSJob depjobtwo = addJobToDataModel("999 - COMPOUND TESTING - Dep Job Two");
		CsvOSJob depjobthree = addJobToDataModel("999 - COMPOUND TESTING - Dep Job Three");

		model.addJobToModel(mainjob);
		model.addJobToModel(depjobone);
		model.addJobToModel(depjobtwo);
		model.addJobToModel(depjobthree);

		CvsDependencyJob depone = model.addJobDependencyForJobCompletedNormal(mainjob, depjobone, null);
		CvsDependencyJob deptwo = model.addJobDependencyForJobCompletedNormal(mainjob, depjobtwo, null);
		CvsDependencyJob depthree = model.addJobDependencyForJobCompletedNormal(mainjob, depjobthree, null);

		deptwo.setExitCodeOperator(ExitCodeOperator.NE);
		deptwo.setExitcodeStart(100);
		deptwo.setExitcodeEnd(101);

		//Autosys -> n dependencies of Types

		// TODO: Research this commented code for usage of parsing input for AUTOSYS Parser to properly parse dependencies!!!
		// Parse Autosys string to replace s(csv.fullpath) with jobid to match,
		// ExpressionParser.parse(data)
		//DependencyBuilder depbuilder = new DependencyBuilder();
		//mainjob.getCompoundDependencybuilder().build(data);

		//e(CTS_UTIL_PRD_2170_040.Verify_a02scheem0005) = 1722
		// Build a dependency using not equal completed normal for now.

		//mainjob.setCompoundDependency(null); // Use this method for Autosys after cleaned up.

		mainjob.getCompoundDependencyBuilder().or(depone.getIdString()).and(deptwo.getIdString()).or(depthree.getIdString());

		boolean isAllAnds = ExpressionUtil.isExpressionOfType(ExpressionType.and, mainjob.getCompoundDependencyBuilder().getExpression());
		boolean isAllOrs = ExpressionUtil.isExpressionOfType(ExpressionType.or, mainjob.getCompoundDependencyBuilder().getExpression());

		if (isAllAnds) {
			// TODO: Look at refactoring this code but for now it works.
			// if the deps are all ands, then ignore it and just set the Or logic to false
			mainjob.setDependencyOrlogic(false);
		} else if (isAllOrs) {
			// if all or's then just set the Or logic to true.
			mainjob.setDependencyOrlogic(true);
		} else {
			// BUT IF WE ARE Setting up a compound dep, then lets process correctly
			//mainjob.setCompoundDependency(depbuilder.toString());

			// If we set this we ignore the two above as we are a complex type.
			mainjob.setCompoundDependencyStringFromBuilder();
			// Register the job has having compound dependencies. OR addJob to model AFTER you setup your compound dependencies.
			model.registerCompoundDependencyJob(mainjob);
		}

	}

	private CsvOSJob addJobToDataModel(String name) {
		CsvOSJob mainjob = new CsvOSJob();
		mainjob.setName(name);
		mainjob.setCommandLine("cmd.exe");
		model.addCalendarToJobOrGroup(mainjob, new CsvCalendar("Daily"));
		model.addNodeToJobOrGroup(mainjob, "AgentOS");

		return mainjob;
	}

	@Override
	public IModelReport getModelReporter() {
		return null;
	}
}
