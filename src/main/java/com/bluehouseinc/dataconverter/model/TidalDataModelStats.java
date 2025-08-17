package com.bluehouseinc.dataconverter.model;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.bluehouseinc.dataconverter.model.dependency.DependencyModelStatistics;
import com.bluehouseinc.dataconverter.model.impl.BaseCsvJobObject;
import com.bluehouseinc.dataconverter.model.impl.CsvJobGroup;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyFile;
import com.bluehouseinc.dataconverter.model.impl.CvsDependencyJob;
import com.bluehouseinc.tidal.api.model.job.JobType;

import lombok.extern.log4j.Log4j2;

import com.bluehouseinc.dataconverter.util.ObjectUtils;

/**
 * Professional command-line statistics display for TidalDataModel
 * Provides comprehensive insights for demos and operational monitoring
 */
@Log4j2
public class TidalDataModelStats {

	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_BOLD = "\u001B[1m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_MAGENTA = "\u001B[35m";
	private static final String ANSI_WHITE = "\u001B[37m";

	private final TidalDataModel model;
	private final NumberFormat numberFormat;

	public TidalDataModelStats(TidalDataModel model) {
		this.model = model;
		this.numberFormat = NumberFormat.getNumberInstance(Locale.US);
	}

	/**
	 * Dual output method - prints to console (if enabled) and logs to Log4j2
	 */
	private void output(String message) {
		// Remove ANSI codes for log output
		String cleanMessage = message.replaceAll("\u001B\\[[;\\d]*m", "");
		log.info(message);
		//System.out.println(message);
	}

	private void outputf(String format, Object... args) {
		String message = String.format(format, args);
		output(message);
	}

	/**
	 * Display comprehensive statistics with professional formatting
	 */
	public void displayFullStatistics() {
		printHeader();
		printJobAnalysisSection();
		printDependencySection();
		printInfrastructureSection();
		printResourceSection();
		printFooter();
	}

	private void printHeader() {
		String title = "TIDAL WORKLOAD AUTOMATION - DATA MODEL STATISTICS";
		String separator = "=".repeat(80);

		output(ANSI_BOLD + ANSI_BLUE + separator + ANSI_RESET);
		output(ANSI_BOLD + ANSI_WHITE + centerText(title, 80) + ANSI_RESET);
		output(ANSI_BOLD + ANSI_BLUE + separator + ANSI_RESET);

	}


	private void printJobAnalysisSection() {
		output("\n"+ANSI_BOLD + ANSI_CYAN + "JOB TYPE ANALYSIS" + ANSI_RESET);
		output("─".repeat(50));

		Map<JobType, Long> jobTypeCounts = getJobTypeDistribution();

		outputf("%-20s %8s %8s", "Job Type", "Count", "Percent");
		output("─".repeat(40));

		long totalJobs = getTotalJobCount();
		jobTypeCounts.entrySet().stream().sorted(Map.Entry.<JobType, Long>comparingByValue().reversed()).forEach(entry -> {
			double percentage = totalJobs > 0 ? (entry.getValue() * 100.0 / totalJobs) : 0;
			outputf("%-20s %s%8s%s %7.1f%%", entry.getKey().toString(), ANSI_YELLOW, numberFormat.format(entry.getValue()), ANSI_RESET, percentage);
		});
	}

	private void printDependencySection() {
		output("\n"+ANSI_BOLD + ANSI_CYAN + "DEPENDENCY ANALYSIS" + ANSI_RESET);
		output("─".repeat(50));

		DependencyModelStatistics depstats = this.model.getDependencyProcessor().getComputeModelStatistics(true);
		
		//long jobsWithUpstreamDeps = this.model.getDependencyProcessor().getUpstreamJobCounts();
		//long jobsWithDownstreamDeps = this.model.getDependencyProcessor().getDownstreamJobCounts();
		//long rootJobs = this.model.getDependencyProcessor().getNoDependencyCounts();

		outputf("%-25s %s%,d%s", "Total Dependencies:", ANSI_GREEN + ANSI_BOLD, model.getDependencies().size(), ANSI_RESET);



		outputf("%-25s %s%,d%s", "Job Dependencies:", ANSI_GREEN + ANSI_BOLD, depstats.getTotalJobDependencies(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "File Dependencies:", ANSI_GREEN + ANSI_BOLD, depstats.getTotalFileDependencies(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Compound Dependencies:", ANSI_GREEN + ANSI_BOLD, model.getCompoundDependnecyJobs().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Job With no Dependencies (Root):", ANSI_GREEN + ANSI_BOLD, depstats.getStandAloneJobs(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Circulare Dependencies Detected:", ANSI_MAGENTA + ANSI_BOLD, depstats.getCircularDependencyCount(), ANSI_RESET);
		
		if(depstats.getCircularDependencyCount()>0) {
			log.error("======================= Circulare Dependencies Detected =======================");
			depstats.getCircularDependencyJobs().forEach(s -> log.error(s.getFullPath()));
			log.error("======================= Circulare Dependencies Detected =======================");
		}
		
		
		//outputf("%-25s %s%,d%s", "Jobs with Downstream Dependencies:", ANSI_GREEN + ANSI_BOLD, jobsWithDownstreamDeps, ANSI_RESET);

	}

	private void printInfrastructureSection() {
		output("\n"+ANSI_BOLD + ANSI_CYAN + "INFRASTRUCTURE COMPONENTS" + ANSI_RESET);
		output("─".repeat(50));

		outputf("%-25s %s%,d%s", "Agents/Nodes:", ANSI_BLUE + ANSI_BOLD, model.getNodes().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Agent Lists:", ANSI_BLUE + ANSI_BOLD, model.getAgentList().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Runtime Users:", ANSI_BLUE + ANSI_BOLD, model.getRuntimeusers().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Time Zones:", ANSI_BLUE + ANSI_BOLD, model.getTimeZones().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Owners:", ANSI_BLUE + ANSI_BOLD, model.getOwners().size(), ANSI_RESET);

	}

	private void printResourceSection() {
		output("\n"+ANSI_BOLD + ANSI_CYAN + "RESOURCE MANAGEMENT" + ANSI_RESET);
		output("─".repeat(50));

		outputf("%-25s %s%,d%s", "Resources:", ANSI_GREEN + ANSI_BOLD, model.getResource().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Variables:", ANSI_GREEN + ANSI_BOLD, model.getVariables().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Calendars:", ANSI_GREEN + ANSI_BOLD, model.getCalendars().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Job Classes:", ANSI_GREEN + ANSI_BOLD, model.getJobClasses().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Email Actions:", ANSI_GREEN + ANSI_BOLD, model.getEmailActions().size(), ANSI_RESET);
		outputf("%-25s %s%,d%s", "Job Tags:", ANSI_GREEN + ANSI_BOLD, model.getJobTags().size(), ANSI_RESET);
	}

	private void printFooter() {
		String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String separator = "=".repeat(80);

		output(ANSI_BOLD + ANSI_BLUE + separator + ANSI_RESET);
		output(ANSI_BLUE + "Generated: " + timestamp + " | Bluehouse - Human-led Software Enabled" + ANSI_RESET);
		output(ANSI_BOLD + ANSI_BLUE + separator + ANSI_RESET);
	}

	// Helper methods
	private String centerText(String text, int width) {
		int padding = (width - text.length()) / 2;
		return " ".repeat(Math.max(0, padding)) + text;
	}

	private long getTotalJobCount() {
		return ObjectUtils.toFlatStream(model.getJobOrGroups()).filter(job -> !(job instanceof CsvJobGroup)).count();
	}

	private long getTotalGroupCount() {
		return ObjectUtils.toFlatStream(model.getJobOrGroups()).filter(job -> job instanceof CsvJobGroup).count();
	}

	private Map<JobType, Long> getJobTypeDistribution() {
		return ObjectUtils.toFlatStream(model.getJobOrGroups()).collect(Collectors.groupingBy(BaseCsvJobObject::getType, Collectors.counting()));
	}

	private long getInfrastructureCount() {
		return model.getNodes().size() + model.getAgentList().size() + model.getRuntimeusers().size() + model.getTimeZones().size() + model.getOwners().size();
	}

	private long getConfigurationCount() {
		return model.getResource().size() + model.getVariables().size() + model.getCalendars().size() + model.getJobClasses().size() + model.getEmailActions().size() + model.getJobTags().size();
	}

	/**
	 * Export statistics to a formatted report file
	 */
	public void exportToFile(String filename) {
		// Implementation for file export if needed
		System.out.println("Export functionality - implement based on requirements");
	}

	/**
	 * Get statistics as structured data for integration
	 */
	public StatsSummary getStatsSummary() {
		return new StatsSummary(getTotalJobCount(), getTotalGroupCount(), model.getDependencies().size(), getInfrastructureCount(), getConfigurationCount(), getJobTypeDistribution());
	}

	/**
	 * Data class for structured statistics
	 */
	public static class StatsSummary {
		public final long totalJobs;
		public final long totalGroups;
		public final long totalDependencies;
		public final long infrastructureCount;
		public final long configurationCount;
		public final Map<JobType, Long> jobTypeDistribution;

		public StatsSummary(long totalJobs, long totalGroups, long totalDependencies, long infrastructureCount, long configurationCount, Map<JobType, Long> jobTypeDistribution) {
			this.totalJobs = totalJobs;
			this.totalGroups = totalGroups;
			this.totalDependencies = totalDependencies;
			this.infrastructureCount = infrastructureCount;
			this.configurationCount = configurationCount;
			this.jobTypeDistribution = jobTypeDistribution;
		}
	}


	/**
	 * Main method for testing and standalone execution
	 */
	public static void main(String[] args) {
		// Example usage - replace with actual model instance
		System.out.println("TidalDataModelStats - Professional Statistics Display");
		System.out.println("Usage: new TidalDataModelStats(model).displayFullStatistics()");
	}
}