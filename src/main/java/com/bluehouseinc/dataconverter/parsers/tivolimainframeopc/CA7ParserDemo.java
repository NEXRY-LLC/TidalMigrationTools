package com.bluehouseinc.dataconverter.parsers.tivolimainframeopc;

import java.io.IOException;
import java.util.List;

//CA7ParserDemo.java - Example usage and testing
class CA7ParserDemo {
	public static void main(String[] args) {

		// try {
		// // List<CA7Application> applications = parser.parseFile("./cfg/bkbsks/dev/app.txt");
		//
		// System.out.println("=== CA7 Parser Results ===");
		// // System.out.println(parser.getStatistics());
		// System.out.println();
		//
		// // Print detailed information for each application
		// // for (CA7Application app : applications) {
		// // printApplicationDetails(app);
		// // System.out.println();
		// // }
		//
		// } catch (IOException e) {
		// System.err.println("Error reading file: " + e.getMessage());
		// System.exit(1);
		// }
	}

	// private static void printApplicationDetails(CA7Application app) {
	// System.out.println("Application: " + app.getApplicationId());
	// System.out.println(" Description: " + app.getDescription());
	// System.out.println(" Owner: " + app.getOwner());
	// System.out.println(" Priority: " + app.getPriority());
	// System.out.println(" Calendar: " + app.getCalendar());
	// System.out.println(" Jobs: " + app.getJobs().size());
	//
	// // Print job details
	// for (CA7Job job : app.getJobs()) {
	// System.out.printf(" Job %d: %s (%s) - Duration: %d mins%n",
	// job.getOperationNumber(), job.getJobName(),
	// job.getWorkstationId(), job.getDuration());
	//
	// // Print dependencies
	// if (!job.getDependencies().isEmpty()) {
	// System.out.println(" Dependencies:");
	// for (CA7Dependency dep : job.getDependencies()) {
	// System.out.printf(" -> %s.%d%n",
	// dep.getPredecessorWorkstationId(),
	// dep.getPredecessorOperationNumber());
	// }
	// }
	// }
	//
	// // Print schedules
	// if (!app.getSchedules().isEmpty()) {
	// System.out.println(" Schedules:");
	// for (CA7Schedule schedule : app.getSchedules()) {
	// System.out.printf(" %s: Rule %s (%s)%n",
	// schedule.getName(), schedule.getRule(), schedule.getRuleDescription());
	// }
	// }
	// }

}
