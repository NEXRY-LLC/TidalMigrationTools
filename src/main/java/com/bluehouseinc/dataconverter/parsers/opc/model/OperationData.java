package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/*
 * VALID APPL NAME TYPE INPUT RC VALID FREE DAY JCL VARIABLE TABLE
 * FROM STATUS RULE DEFINITION ARRIVE DEADLINE FROM TO RULE RUN CYCLE DESCRIPTION
 * -------- -------- -------------------------------- ------ -------- ----------------- -------- ----------------------
 * 008/14/96 ACTIVE SUNTHURS REGULAR 12.00 01 06.30 08/14/96 12/30/71 CANCEL
 * process every work day of the week
 * <ADRULE EVERY DAY(WORKDAY) WEEK >
 * 1LISTINGS FROM SAMPLE PAGE 2854
 * TWSZ/OPC 22 JUL 21
 * SUBSYSTEM OPCE 12:32
 * -
 * APPL ID: AOEDAILY VALID FROM: 08/14/96 STATUS: ACTIVE
 * ========================= ==================== ================
 * -
 * FIXME: Input arrive is for the whole group but jobs in the group can have different start times,
 * see expected arrival below for AFDA03.
 * FIXME: DUMY jobs are to not be Imported, they are just an OPC requirement that all jobs must have a dependency and
 * most DUMY
 * jobs are an operation to tie into.
 * OPERATION DATA
 * ==============
 * -
 * OPERATION NUMBER SPC SMOOTH CLASS & MVS OPTIONS EXPECTED
 * NAME NO TEXT DUR PS R1 R2 RES HRC FAC LIM JOBNAME FORM A H T S E D R R C ARRIVAL DEADLINE
 * -------- ------------------------ ----- -------- --- --- ------- -------- ---------- ----------------- --------
 * --------
 * 0DUMY_001 00.01 1 0 0 0 Y Y N N Y N N
 * CPU_002 00.01 1 0 0 0 AFDA03 Y Y Y N Y N N 14.45
 * CPU_004 00.05 1 0 0 0 AOEA37 Y Y Y N Y N N 14.40
 * CPU_005 00.30 1 0 0 0 ARSA37 Y Y Y N Y N N 14.40
 */
@Data
public class OperationData implements Serializable {
	private String operationName;
	private String text;
	private String jobName;
	private String expectedArrival;
	private MVSOptions options;
	private String deadLine;
	private List<JobResource> jobResources = new ArrayList<>();
}
