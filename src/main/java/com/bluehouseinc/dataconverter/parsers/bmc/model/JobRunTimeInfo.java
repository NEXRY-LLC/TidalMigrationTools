package com.bluehouseinc.dataconverter.parsers.bmc.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

import com.bluehouseinc.dataconverter.parsers.bmc.xml.model.JobData;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.Data;

/**
 * Job time infor for a BMC job.
 *
 * @author Brian Hayes
 *
 */
@Data
public class JobRunTimeInfo {

	JobData jobData;

	public JobRunTimeInfo(JobData data) {
		this.jobData = data;
	}

	/**
	 * CYCLIC="1" => Is the BMC check box to make the job a rerunning job multiple
	 * times a day based on INTERVAL="00001M"
	 *
	 * @return
	 */
	public boolean isRerunningJob() {
		String cycl = this.jobData.getCYCLIC();
		if (cycl == null) {
			return false;
		} else if (Integer.parseInt(cycl) != 1) {
			return false;
		}

		return true;
	}

	/**
	 * The interval that rerunning jobs rerun.
	 *
	 * @return
	 */
	public Integer getRerunIntervalSequence() {

		// if (this.jobData.getCYCLICTYPE().equals("Interval")) {
		String i = this.jobData.getCYCLICINTERVALSEQUENCE();
		if (i.endsWith("M")) {
			return Integer.parseInt(i.replace("M", ""));
		} else if (i.endsWith("H")) {
			return Integer.parseInt(i.replace("H", "")) * 60;
		} else {
			throw new RuntimeException("Unknown getCYCLICINTERVALSEQUENCE[" + i + "]");
		}
	}

	/**
	 * The interval that rerunning jobs rerun.
	 *
	 * @return
	 */
	public Integer getRerunInterval() {

		// if (this.jobData.getCYCLICTYPE().equals("Interval")) {
		String i = this.jobData.getINTERVAL();
		if (i.endsWith("M")) {
			return Integer.parseInt(i.replace("M", ""));
		} else if (i.endsWith("H")) {
			return Integer.parseInt(i.replace("H", "")) * 60;
		} else {
			throw new RuntimeException("Unknown getINTERVAL[" + i + "]");
		}
	}

	public String getStartTime() {
		return cleanTimeChars(this.jobData.getTIMEFROM());
	}

	public String getEndTime() {
		return cleanTimeChars(this.jobData.getTIMETO());
	}

	/**
	 * Jobs that are setup to only run until a certain date. BMC ACTIVE_TILL
	 * element, aka ACTIVE_TILL="20181029"
	 *
	 * @return the {@link Date} or null if not present or in bad format.
	 */
	public Date getExpiredDate() {
		// ACTIVE_TILL="20181029"
		SimpleDateFormat fm = new SimpleDateFormat("YYYYMMDD");
		String std = this.jobData.getACTIVETILL();

		if (std != null) {
			try {
				return fm.parse(std);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public boolean isOperatorRelease() {

		String con = this.jobData.getCONFIRM();

		if (StringUtils.isBlank(con)) {
			return false;
		} else {

			return Integer.valueOf(con) == 1;
		}
	}

	private String cleanTimeChars(String time) {
		if (time == null) {
			return time;
		}
		if (time.equals(">")) {
			return null;
		}
		return time;
	}

	public RerunType getRerunType() {
		return RerunType.valueOfSring(this.jobData.getCYCLICTYPE());
	}

	/**
	 * If this is a RerunType.SpecificTimes then this should have data.
	 *
	 * @return
	 */
	public String getStartTimeSeqence() {
		return this.jobData.getCYCLICTIMESSEQUENCE();
	}

	public enum RerunType {

		IntervalSequence("V"), SpecificTimes("S"), Interval("C"), NULL(null);

		public final String type;

		private RerunType(String type) {
			this.type = type;
		}

		public static RerunType valueOfSring(String type) {
			if (type == null) {
				return NULL;
			}
			return EnumSet.allOf(RerunType.class).stream().filter(e -> e.type.equals(type)).findFirst().orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", type)));
		}
	}

}
