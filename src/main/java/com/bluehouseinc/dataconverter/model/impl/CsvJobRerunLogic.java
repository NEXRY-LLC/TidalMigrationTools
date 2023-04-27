package com.bluehouseinc.dataconverter.model.impl;

import com.bluehouseinc.tidal.api.model.job.RepeatType;
import com.bluehouseinc.tidal.utils.StringUtils;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class CsvJobRerunLogic {


	/**
	 * This is matching and using API object for conversion data logic.
	 *
	 * This is going to be the driver for the other fields values.
	 * Default is do not repeat.
	 *
	 */
	@CsvBindByName
	RepeatType repeatType = RepeatType.NONE;

	/**
	 * Set your data if not a standar repeat option.
	 *
	 * E.G minutes after hours, or start times (Hours)
	 */
	@CsvBindByName
	String rerunData;

	@CsvBindByName
	Integer repeatEvery;

	@CsvBindByName
	Integer repeatMaxTimes;


	/**
	 * Helper Method - returns true if the rerunType is not set to NONE.
	 * @return
	 */
	public boolean isReRunningJob() {
		return repeatType != RepeatType.NONE;
	}

	/**
	 * Helper method to let you know you got the data right based on todays logic :)
	 *
	 * if new or same , then you need to set rerunEveryMinutes and optionally rerunThisManyTimes
	 *
	 * All others need rerunData to be set.
	 *
	 *
	 * @return true always if RepeatType.NONE is true
	 */
	public boolean isValid() {

		if(!isReRunningJob()) { return true ; } // Ignore all other data.

		if(repeatType == RepeatType.NEW || repeatType == RepeatType.SAME) {
			if(repeatEvery != null) {
				return true;
			}
		}

		// Any of the others, we only need our string data
		if(repeatType != RepeatType.NEW && repeatType != RepeatType.SAME) {
			if(!StringUtils.isBlank(rerunData)) {
				return true;
			}
		}

		return false;
	}
}
