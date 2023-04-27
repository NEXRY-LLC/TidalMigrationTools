package com.bluehouseinc.dataconverter.parsers.opc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.util.SerializationUtils;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.model.IModelReport;
import com.bluehouseinc.dataconverter.parsers.AbstractParser;
import com.bluehouseinc.dataconverter.parsers.opc.model.AdRule;
import com.bluehouseinc.dataconverter.parsers.opc.model.Adrep1DataModel;
import com.bluehouseinc.dataconverter.parsers.opc.model.Application;
import com.bluehouseinc.dataconverter.parsers.opc.model.InternalOperationLogic;
import com.bluehouseinc.dataconverter.parsers.opc.model.JobResource;
import com.bluehouseinc.dataconverter.parsers.opc.model.MVSOptions;
import com.bluehouseinc.dataconverter.parsers.opc.model.OperationData;
import com.bluehouseinc.dataconverter.parsers.opc.model.RuleBasedRunCycleInformation;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;



public class OPCParser extends AbstractParser<Adrep1DataModel> {


	public OPCParser(ConfigurationProvider cfgProvider) {
		super(new Adrep1DataModel(cfgProvider));
	}

	private String REPORT_PAGE_PATTERN = "\\s*(LISTINGS FROM SAMPLE)\\s*(PAGE \\d{4})";
	private String REPORT_END_PATTERN = "\\>*\\s*(END OF APPLICATION DESCRIPTION PRINTOUT)\\s*\\<*";

	private String APP_ID_PATTERN = "APPL ID:\\s*([#a-zA-Z0-9]*)";

	private String COMMON_DATA_SECTION_PATTERN = "[-]{0,1}\\s*(COMMON DATA)\\s*";
	private String RULE_BASED_RUN_CYCLE_SECTION_PATTERN = "[-]{0,1}\\s*(RULE-BASED RUN CYCLE INFORMATION)\\s*";
	private String OFFSET_BASED_RUN_CYCLE_SECTION_PATTERN = "[-]{0,1}\\s*(OFFSET-BASED RUN CYCLE INFORMATION)\\s*";
	private String OPERATION_DATA_SECTION_PATTERN = "[-]{0,1}\\s*(OPERATION DATA)\\s*";
	private String INTERNAL_OPERATION_LOGIC_PATTERN = "[-]{0,1}\\s*(INTERNAL OPERATION LOGIC)\\s*";

	private String HEADER_SEPARATOR_PATTERN = "(?=.*[-])(?=.*[ ])[- ]*\\-$";
	private String EMPTY_LINE_PATTERN = "^\\s*$";
	private String ROW_PATTERN = "(?=.*[a-zA-Z0-9])(?=.*[ ])(?=.*)(?=.*[a-zA-Z0-9]).*$";

	private enum Fields {
		APPLICATION_ID,

		// common data
		APPLICATION_TEXT, OWNER_ID, VALID_FROM_TO, CALENDAR_ID, STATUS,

		// rule based run cycle information
		VALID_FROM, APP_STATUS, RULE_DEFINITION, RUN_CYCLE_DESCRIPTION, INPUT_ARRIVE, DEADLINE, FREE_DAY_RULE,

		// operation data
		OPERATION_NAME, TEXT, JOB_NAME, MVS_OPTIONS, EXPECTED_ARRIVAL, EXPECTED_DEADLINE,

		// internal operation logic
		INT_EXT_PREDECESSOR, OPERATION, INTERNAL_SUCCESSOR,
	}

	private enum Sections {
		COMMON_DATA, RULE_BASED_RUN_CYCLE_INFORMATION, OFFSET_BASED_RUN_CYCLE_INFORMATION, OPERATION_DATA, INTERNAL_OPERATION_LOGIC,
	}

	Sections currentSection = null;
	private List<List<String>> commonDataHeaderColumns = new ArrayList<>();
	private List<List<String>> ruleBasedRunCycleHeaderColumns = new ArrayList<>();
	private List<List<String>> offsetBasedRunCycleHeaderColumns = new ArrayList<>();
	private List<List<String>> operationDataHeaderColumns = new ArrayList<>();
	private List<List<String>> internalOperationLogicHeaderColumns = new ArrayList<>();


	@Override
	public void parseFile() throws Exception {
		String file = this.getParserDataModel().getConfigeProvider().getOPCPath();
		BufferedReader reader = new BufferedReader(new FileReader(file));

		boolean startOfReportReached = false;
		boolean endOfReportReached = false;

		// TODO: move reset data into reset function
		currentSection = null;
		commonDataHeaderColumns = new ArrayList<>();
		ruleBasedRunCycleHeaderColumns = new ArrayList<>();
		offsetBasedRunCycleHeaderColumns = new ArrayList<>();
		operationDataHeaderColumns = new ArrayList<>();
		internalOperationLogicHeaderColumns = new ArrayList<>();

		int page = 0;
		int rowLinesCount = 0;
		boolean expectDataRow = false;

		Application current = null;

		String line;
		while (!endOfReportReached && (line = reader.readLine()) != null) {
			// found report page
			if (this.isPageBreakLine(line)) {
				if (!startOfReportReached) {
					startOfReportReached = true;
				}

				page++;
				this.skipNextLines(reader, 3);
				continue;
			}

			// found end of report
			if (line.matches(REPORT_END_PATTERN)) {
				endOfReportReached = true;
				continue;
			}

			if (this.isAppIdLine(line)) {
				String appId = RegexHelper.extractFirstMatch(line, APP_ID_PATTERN);

				System.out.println();
				System.out.println("APPL ID: " + appId);

				if (appId.equalsIgnoreCase("AOEDAILY")) {
					System.out.println("APPL ID: " + appId);
				}

				if (current == null) {
					current = new Application(appId);
				}

				if (!current.getApplicationId().equals(appId)) {
					this.getParserDataModel().getApplications().add((Application) SerializationUtils.deserialize(SerializationUtils.serialize(current)));

					current = new Application(appId);
				}

				expectDataRow = false;
				continue;
			}

			if (this.isSectionLine(line)) {
				currentSection = this.detectSection(line);
				expectDataRow = false;
				continue;
			}

			// found header line
			if (line.matches(HEADER_SEPARATOR_PATTERN)) {
				this.generateColumnsForSection(line);

				expectDataRow = true;
				rowLinesCount = 0; // reset
				continue;
			}

			if (!this.isSectionHeaderLineReached()) {
				continue;
			}

			if (this.shouldSkipLine(line)) {
				rowLinesCount++;
				continue;
			}

			if (expectDataRow) {
				switch (currentSection) {
				case COMMON_DATA:
					if (rowLinesCount == 0) {
						System.out.println("=== COMMON DATA ===");
						current.getCommonData().setAppText(this.extractFieldValue(currentSection, Fields.APPLICATION_TEXT, line, commonDataHeaderColumns));
						current.getCommonData().setCalendarId(this.extractFieldValue(currentSection, Fields.CALENDAR_ID, line, commonDataHeaderColumns));
						System.out.println("APPL TEXT: " + this.extractFieldValue(currentSection, Fields.APPLICATION_TEXT, line, commonDataHeaderColumns));
						System.out.println("CALENDAR ID: " + this.extractFieldValue(currentSection, Fields.CALENDAR_ID, line, commonDataHeaderColumns));
					} else {
						current.getCommonData().setOwnerId(this.extractFieldValue(currentSection, Fields.OWNER_ID, line, commonDataHeaderColumns));
						current.getCommonData().setStatus(this.extractFieldValue(currentSection, Fields.STATUS, line, commonDataHeaderColumns));
						String validFromTo = this.extractFieldValue(currentSection, Fields.VALID_FROM_TO, line, commonDataHeaderColumns);
						current.getCommonData().setValidFrom(validFromTo.split(" ")[0]);
						current.getCommonData().setValidTo(validFromTo.split(" ")[1]);
						System.out.println("OWNER ID: " + this.extractFieldValue(currentSection, Fields.OWNER_ID, line, commonDataHeaderColumns));
						System.out.println("VALID FROM - TO: " + validFromTo);
						System.out.println("STATUS: " + this.extractFieldValue(currentSection, Fields.STATUS, line, commonDataHeaderColumns));
					}
					break;

				case RULE_BASED_RUN_CYCLE_INFORMATION:
					if (rowLinesCount == 0) {
						System.out.println("=== RULE-BASED RUN CYCLE INFORMATION ===");
					}

					if (!this.isAddRuleLine(line)) {
						String ruleDef = this.extractFieldValue(currentSection, Fields.RULE_DEFINITION, line, ruleBasedRunCycleHeaderColumns);
						if (ruleDef.length() > 0) {
							// new
							RuleBasedRunCycleInformation ruleInfo = new RuleBasedRunCycleInformation();
							ruleInfo.setValidFrom(this.extractFieldValue(currentSection, Fields.VALID_FROM, line, ruleBasedRunCycleHeaderColumns));
							ruleInfo.setAppStatus(this.extractFieldValue(currentSection, Fields.VALID_FROM, line, ruleBasedRunCycleHeaderColumns));
							ruleInfo.setRuleDefinition(ruleDef);
							ruleInfo.setInputArrive(this.extractFieldValue(currentSection, Fields.INPUT_ARRIVE, line, ruleBasedRunCycleHeaderColumns));
							ruleInfo.setDeadline(this.extractFieldValue(currentSection, Fields.DEADLINE, line, ruleBasedRunCycleHeaderColumns));
							ruleInfo.setFreeDayRule(this.extractFieldValue(currentSection, Fields.FREE_DAY_RULE, line, ruleBasedRunCycleHeaderColumns));

							current.getRuleBasedRunCycleInformation().add(ruleInfo);
							System.out.println("VALID FROM: " + this.extractFieldValue(currentSection, Fields.VALID_FROM, line, ruleBasedRunCycleHeaderColumns));
							System.out.println("APPL STATUS: " + this.extractFieldValue(currentSection, Fields.APP_STATUS, line, ruleBasedRunCycleHeaderColumns));
							System.out.println("RULE DEFINITION: " + ruleDef);
							System.out.println("INPUT ARRIVE: " + this.extractFieldValue(currentSection, Fields.INPUT_ARRIVE, line, ruleBasedRunCycleHeaderColumns));
							System.out.println("DEADLINE: " + this.extractFieldValue(currentSection, Fields.DEADLINE, line, ruleBasedRunCycleHeaderColumns));
							System.out.println("FREE DAY RULE: " + this.extractFieldValue(currentSection, Fields.FREE_DAY_RULE, line, ruleBasedRunCycleHeaderColumns));

						} else {
							// get last added rule def and set description
							System.out.println("RUN CYCLE DESCRIPTION: " + line);
							current.getRuleBasedRunCycleInformation().get(current.getRuleBasedRunCycleInformation().size() - 1).setRuleDefinition(line.trim());
						}
					} else {
						String addRuleStr = AdRule.sanitizeAdRule(line);
						String nextLine = reader.readLine();
						// if page break - temp fix
						if (this.isPageBreakLine(nextLine)) {
							this.skipNextLines(reader, 12);
							nextLine = reader.readLine();
						}

						if (this.isAddRuleLine(nextLine)) {
							addRuleStr += AdRule.sanitizeAdRule(nextLine);
						}

						AdRule adRule = new AdRule(addRuleStr);
						current.getRuleBasedRunCycleInformation().get(current.getRuleBasedRunCycleInformation().size() - 1).setAdRule(adRule);
						System.out.println("ADRULE: " + adRule.toString());
					}
					break;

				case OFFSET_BASED_RUN_CYCLE_INFORMATION:
					// do nothing for now
					break;

				case OPERATION_DATA:
					if (rowLinesCount == 0) {
						System.out.println("=== OPERATION DATA ===");
					}

					String operationName = this.extractFieldValue(currentSection, Fields.OPERATION_NAME, line, operationDataHeaderColumns);

					if (operationName.isEmpty()) {
						// should be JobResource row
						String[] jobResourceData = line.trim().replaceAll("\\s\\s+", " ").split("\\s");
						if (jobResourceData.length < 3) {
							// not a valid row
							continue;
						}

						JobResource jobResource = new JobResource();
						jobResource.setName(jobResourceData[0]);
						jobResource.setAsterisk(jobResourceData[1]);
						jobResource.setS(jobResourceData[2]);
						if (jobResourceData.length > 3) {
							jobResource.setN(jobResourceData[3]);
						}

						current.getOperations().get(current.getOperations().size() - 1).getJobResources().add(jobResource);
						continue;
					}

					OperationData opData = new OperationData();
					opData.setOperationName(operationName);

					String text = this.extractFieldValue(currentSection, Fields.TEXT, line, operationDataHeaderColumns);
					opData.setText(text);

					String jobname = this.extractFieldValue(currentSection, Fields.JOB_NAME, line, operationDataHeaderColumns);
					opData.setJobName(jobname);

					String mvsOptions = this.extractFieldValue(currentSection, Fields.MVS_OPTIONS, line, operationDataHeaderColumns);
					opData.setOptions(new MVSOptions(mvsOptions));

					String expectedA = this.extractFieldValue(currentSection, Fields.EXPECTED_ARRIVAL, line, operationDataHeaderColumns);

					if (expectedA != null && !expectedA.isEmpty()) {
						opData.setExpectedArrival(expectedA);
					}

					String deadlineA = this.extractFieldValue(currentSection, Fields.EXPECTED_DEADLINE, line, operationDataHeaderColumns);

					if (deadlineA != null && !deadlineA.isEmpty()) {
						opData.setDeadLine(deadlineA);
					}

					current.getOperations().add(opData);

					System.out.println("OPERATION NAME: " + operationName);
					System.out.println("OPERATION NAME TEXT: " + text);
					System.out.println("JOB NAME: " + jobname);
					System.out.println("MVS OPTIONS: " + mvsOptions);
					System.out.println("EXPECTED ARRIVAL: " + expectedA);
					System.out.println("DEADLINE: " + deadlineA);
					break;

				case INTERNAL_OPERATION_LOGIC:
					if (rowLinesCount == 0) {
						System.out.println("=== INTERNAL OPERATION LOGIC ===");
					}

					if (current.getApplicationId().equals("ACCA10")) {
						System.out.println();
					}

					InternalOperationLogic iol = new InternalOperationLogic();
					iol.setInternalAndExternalPredecessor(this.extractFieldValue(currentSection, Fields.INT_EXT_PREDECESSOR, line, internalOperationLogicHeaderColumns));
					iol.setOperation(this.extractFieldValue(currentSection, Fields.OPERATION, line, internalOperationLogicHeaderColumns));
					iol.setInternalSuccessor(this.extractFieldValue(currentSection, Fields.INTERNAL_SUCCESSOR, line, internalOperationLogicHeaderColumns));
					current.getInternalOperationLogic().add(iol);

					System.out.println("INT &  EXT PREDECESSOR: " + this.extractFieldValue(currentSection, Fields.INT_EXT_PREDECESSOR, line, internalOperationLogicHeaderColumns));
					System.out.println("OPERATION: " + this.extractFieldValue(currentSection, Fields.OPERATION, line, internalOperationLogicHeaderColumns));
					System.out.println("INTERNAL SUCCESSOR: " + this.extractFieldValue(currentSection, Fields.INTERNAL_SUCCESSOR, line, internalOperationLogicHeaderColumns));
					break;
				}
				rowLinesCount++;
			}
		}

		System.out.println();
		System.out.println("Num of pages: " + page);
		System.out.println();

	}

	private void generateColumnsForSection(String line) {
		Function<String, String> sanitizeColumns = s -> s.replaceAll("\\s{1}(?!-)", "-");

		switch (currentSection) {
		case COMMON_DATA:
			commonDataHeaderColumns = this.generateColumns(line, sanitizeColumns);
			break;

		case RULE_BASED_RUN_CYCLE_INFORMATION:
			ruleBasedRunCycleHeaderColumns = this.generateColumns(line, sanitizeColumns);
			break;

		case OFFSET_BASED_RUN_CYCLE_INFORMATION:
			offsetBasedRunCycleHeaderColumns = this.generateColumns(line, sanitizeColumns);
			break;

		case OPERATION_DATA:
			operationDataHeaderColumns = this.generateColumns(line, sanitizeColumns);
			break;

		case INTERNAL_OPERATION_LOGIC:
			internalOperationLogicHeaderColumns = this.generateColumns(line, sanitizeColumns);
			break;
		}
	}

	private boolean isPageBreakLine(String line) {
		return RegexHelper.matchesRegexPattern(line, REPORT_PAGE_PATTERN);
	}

	private boolean isAppIdLine(String line) {
		return RegexHelper.matchesRegexPattern(line, APP_ID_PATTERN);
	}

	private boolean isSectionLine(String line) {
		return line.matches(COMMON_DATA_SECTION_PATTERN) || line.matches(RULE_BASED_RUN_CYCLE_SECTION_PATTERN) || line.matches(OFFSET_BASED_RUN_CYCLE_SECTION_PATTERN) || line.matches(OPERATION_DATA_SECTION_PATTERN)
				|| line.matches(INTERNAL_OPERATION_LOGIC_PATTERN);
	}

	private boolean isAddRuleLine(String line) {
		return line.matches("[0,1]{0,1}\\s*<.*>$");
	}

	private boolean shouldSkipLine(String line) {
		return line.matches(EMPTY_LINE_PATTERN) || line.equals("-") || !line.matches(ROW_PATTERN);
	}

	private Sections detectSection(String line) {
		if (!isSectionLine(line))
			return null;

		if (line.matches(COMMON_DATA_SECTION_PATTERN)) {
			return Sections.COMMON_DATA;
		}

		if (line.matches(RULE_BASED_RUN_CYCLE_SECTION_PATTERN)) {
			return Sections.RULE_BASED_RUN_CYCLE_INFORMATION;
		}

		if (line.matches(OFFSET_BASED_RUN_CYCLE_SECTION_PATTERN)) {
			return Sections.OFFSET_BASED_RUN_CYCLE_INFORMATION;
		}

		if (line.matches(OPERATION_DATA_SECTION_PATTERN)) {
			return Sections.OPERATION_DATA;
		}

		if (line.matches(INTERNAL_OPERATION_LOGIC_PATTERN)) {
			return Sections.INTERNAL_OPERATION_LOGIC;
		}

		return null;
	}

	private boolean isSectionHeaderLineReached() {
		if ((currentSection == Sections.COMMON_DATA && commonDataHeaderColumns.size() > 0) || (currentSection == Sections.RULE_BASED_RUN_CYCLE_INFORMATION && ruleBasedRunCycleHeaderColumns.size() > 0)) {
			return true;
		}

		if ((currentSection == Sections.OPERATION_DATA && operationDataHeaderColumns.size() > 0) || (currentSection == Sections.INTERNAL_OPERATION_LOGIC && internalOperationLogicHeaderColumns.size() > 0)) {
			return true;
		}

		return false;
	}

	// configure per section
	private String extractFieldValue(Sections section, Fields field, String ln, List<List<String>> columns) throws Exception {
		Integer columnGroupPos = 0, columnPos = null;
		switch (section) {
		case COMMON_DATA:
			switch (field) {
			case APPLICATION_TEXT:
				// special, going across two columns
				Map.Entry<Integer, Integer> cords = this.getValuePosition(columnGroupPos, 0, columns);
				Map.Entry<Integer, Integer> cords2 = this.getValuePosition(columnGroupPos, 1, columns);
				return ln.substring(cords.getKey(), cords2.getValue());

			case OWNER_ID:
				columnPos = 0;
				break;

			case CALENDAR_ID:
				columnPos = 2;
				break;

			case VALID_FROM_TO:
				columnPos = 2;
				break;

			case STATUS:
				columnPos = 3;
				break;
			}

		case RULE_BASED_RUN_CYCLE_INFORMATION:
			switch (field) {
			case VALID_FROM:
				columnPos = 0;
				break;

			case APP_STATUS:
				columnPos = 1;
				break;

			case RULE_DEFINITION:
				columnPos = 2;
				break;

			case RUN_CYCLE_DESCRIPTION:
				columnPos = 7;
				break;

			case INPUT_ARRIVE:
				columnPos = 3;
				break;

			case DEADLINE:
				columnPos = 4;
				break;

			case FREE_DAY_RULE:
				columnPos = 6;
				break;
			}

		case OPERATION_DATA:
			switch (field) {
			case OPERATION_NAME:
				columnPos = 0;
				break;

			case TEXT:
				columnPos = 1;
				break;

			case JOB_NAME:
				columnPos = 7;
				break;
			case MVS_OPTIONS:
				columnPos = 9;
				break;
			case EXPECTED_ARRIVAL:
				columnPos = 10;
				break;
			case EXPECTED_DEADLINE:
				columnPos = 11;
				break;
			}
			break;

		case INTERNAL_OPERATION_LOGIC:
			switch (field) {
			case INT_EXT_PREDECESSOR:
				columnPos = 3;
				break;

			case OPERATION:
				columnPos = 4;
				break;

			case INTERNAL_SUCCESSOR:
				columnPos = 5;
				break;

			}
			break;
		}

		if (columnPos == null) {
			throw new Exception("Field not found");
		}

		Map.Entry<Integer, Integer> cords = this.getValuePosition(columnGroupPos, columnPos, columns);

		// for page breaks, prevent index out of range exceptions

		if (cords.getKey() >= ln.length()) {
			return "";
		}

		if (cords.getValue() >= ln.length()) {
			cords.setValue(ln.length());
		}

		return ln.substring(cords.getKey(), cords.getValue()).trim();
	}


	@Override
	public IModelReport getModelReporter() {
		return new OPCReporter();
	}
}
