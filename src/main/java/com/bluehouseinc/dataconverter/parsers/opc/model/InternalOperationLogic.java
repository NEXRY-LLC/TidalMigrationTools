package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import lombok.Data;

/*
 * LTP PRINT REASON FOR DEPENDENCY TIME PREDECESSOR OPERATION SUCCESSOR
 * --------- -------------------------------------------------- ----- -------------------------- --------- ---------
 * 0 CPU_015 CPU_018
 * ALWAYS AFIC20 :CPU_005
 * ALWAYS AFIC21 :CPU_005
 * ALWAYS AFIDAILY :CPU_011
 * ALWAYS AFIDAILY :CPU_015
 * ALWAYS AFIMONTHLY :CPU_011
 * ALWAYS AFIMONTHLY :CPU_015
 * ALWAYS AFISEMIANNUAL :CPU_011
 * ALWAYS AFISEMIANNUAL :CPU_015
 * ALWAYS AFIWEEKLY :CPU_011
 * ALWAYS AFIWEEKLY :CPU_015
 * 0 CPU_015 CPU_019 CPU_020
 * ALWAYS AFIC20 :CPU_005
 * ALWAYS AFIC21 :CPU_005
 * 0 CPU_015 CPU_020
 * CPU_019
 * ALWAYS AFIDAILY :CPU_015
 * NOTES: Operation is the job we want to run , if Operation contains a prediscessor in the CPU_ form with nothing else,
 * then this is
 * an internal dependency. We are going to ignore Successors for the most part becuase we can about what a operation /
 * job is dependent upon.
 * Rules:
 * If PREDECESSOR contains CPU_ object then we are dependent on an internal job
 * if PREDECESSOR contains NAME:CPU_ then this is external and Name is the application id to OPC or job group in TIDAL.
 * Each PREDECESSOR containing Name:CPU_ belongs to the above PREDECESSOR listing only the CPU_
 * BAD DATA Examples
 * TRSP INT & EXT INTERNAL
 * LTP PRINT REASON FOR DEPENDENCY TIME PREDECESSOR OPERATION SUCCESSOR
 * --------- -------------------------------------------------- ----- -------------------------- --------- ---------
 * 0 CPU_005
 * 0 AAPA41
 * FIXME: YMSE01 in TIDAL is missing AMSE00 and CDBWEEKLY dependencies.
 * -
 * APPL ID: YMSE01 VALID FROM: 06/01/06 STATUS: PENDING
 * ========================= ==================== ================
 * -
 * OPERATION DATA
 * ==============
 * -
 * OPERATION NUMBER SPC SMOOTH CLASS & MVS OPTIONS EXPECTED
 * NAME NO TEXT DUR PS R1 R2 RES HRC FAC LIM JOBNAME FORM A H T S E D R R C ARRIVAL DEADLINE
 * -------- ------------------------ ----- -------- --- --- ------- -------- ---------- ----------------- --------
 * --------
 * 0CPU_010 name and address change 00.47 1 0 0 0 YMSE01 Y Y N N Y N N
 * CPU_015 00.01 1 0 0 0 YMSE02 Y Y N N Y N N
 * -
 * - INTERNAL OPERATION LOGIC
 * ========================
 * -
 * TRSP INT & EXT INTERNAL
 * LTP PRINT REASON FOR DEPENDENCY TIME PREDECESSOR OPERATION SUCCESSOR
 * --------- -------------------------------------------------- ----- -------------------------- --------- ---------
 * 0ALWAYS ABCWEEKLY :CPU_080 CPU_010 CPU_015
 * ALWAYS AMSE00 :CPU_010
 * ALWAYS CDBWEEKLY :CPU_010
 * 0 CPU_010 CPU_015
 * 0ALWAYS ABCWEEKLY :YBCC05 YMSE01 YMSE02
 * ALWAYS AMSE00 :AMSE00
 * ALWAYS CDBWEEKLY :CDBC06
 * ABOA60 TODO: REview this app, it has multiple start times and an internal loop dep to ensure the next run of the app
 * doesn't happen unless the prior run as completed.
 * ASFA10 TODO: To many rerun references.
 * Look for start times spanning midnight.
 * -
 * OPERATION DATA
 * ==============
 * -
 * OPERATION NUMBER SPC SMOOTH CLASS & MVS OPTIONS EXPECTED
 * NAME NO TEXT DUR PS R1 R2 RES HRC FAC LIM JOBNAME FORM A H T S E D R R C ARRIVAL DEADLINE
 * -------- ------------------------ ----- -------- --- --- ------- -------- ---------- ----------------- --------
 * --------
 * 0CPU_010 00.08 1 0 0 0 ABIA00 Y Y N N Y N N
 * -
 * - INTERNAL OPERATION LOGIC
 * ========================
 * -
 * TRSP INT & EXT INTERNAL
 * LTP PRINT REASON FOR DEPENDENCY TIME PREDECESSOR OPERATION SUCCESSOR
 * --------- -------------------------------------------------- ----- -------------------------- --------- ---------
 * 0ALWAYS MIDOWN :OPER_005 CPU_010
 * 0ALWAYS MIDOWN :MIDOWN ABIA00
 */
@Data
public class InternalOperationLogic implements Serializable {
	private String internalAndExternalPredecessor;
	private String operation;
	private String internalSuccessor;
	private ExternalJobRef externalJobRef;
	private InternalJobRef internalJobRef;

	public boolean isOperationLookupType() {

		if ((this.internalAndExternalPredecessor != null && this.internalAndExternalPredecessor.contains("_")) || (this.operation != null && this.operation.contains("_"))) {
			return true;
		}

		return false;
	}

	public void setInternalAndExternalPredecessor(String data) {
		this.internalAndExternalPredecessor = data;
		if (data.contains(":")) {
			List<String> tokenlist = Arrays.asList(data.split(":"));
			if (tokenlist.size() == 2) { // Have cases where the bad data showing just external app id but not the job
											// ref
				this.externalJobRef = new ExternalJobRef(tokenlist.get(0).trim(), tokenlist.get(1).trim());
			} else {
				this.externalJobRef = new ExternalJobRef(tokenlist.get(0).trim(), null);
			}
		} else if (data != null & !data.isEmpty()) {
			// Must be of some value for internal
			this.internalJobRef = new InternalJobRef(data.trim());
		} else {
			// DO we care? Think not that a single line with no PREDECESSOR simply is reporting a SUCCESSOR
		}
	}

	@Data
	final class ExternalJobRef implements Serializable {
		private String appId;
		private String jobId; // Can be either CPU_ ref to job name or

		public ExternalJobRef(String appid, String jobid) {
			this.appId = appid;
			this.jobId = jobid;
		}

	}

	@Data
	final class InternalJobRef implements Serializable {
		private String jobId; // Can be either CPU_ ref to job name or

		public InternalJobRef(String jobid) {
			this.jobId = jobid;
		}

	}
}
