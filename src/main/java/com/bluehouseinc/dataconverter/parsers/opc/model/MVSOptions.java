package com.bluehouseinc.dataconverter.parsers.opc.model;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Splitter;

/**
 * For now we care about the time element of the array and we do not currently know what all the others represent.
 *
 * A H -> T <- S E D R R C
 *
 *
 * @author Brian Hayes
 *
 */
/*
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
public class MVSOptions implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private String raw;
	private List<String> rawelements;

	public MVSOptions(String data) {
		this.raw = data;// .trim().replace(" ", ""); // Strip this down to nothing
		this.rawelements = Splitter.on(' ').trimResults().omitEmptyStrings().splitToList(data);
	}

	public boolean isTimeRestricted() {
		if (this.rawelements.size() > 2) {
			String yn = this.rawelements.get(2);

			return yn.equals("Y");
		}

		return false;
	}
}
