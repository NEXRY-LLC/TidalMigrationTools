package com.bluehouseinc.dataconverter.parsers.esp.model.jobs.data;

import lombok.Data;

@Data
public class CcCheck {
		boolean isOkContinue = true; // FAIL CONTINUE = false or OK CONTINUE = true

		boolean isStepProcessCheck = false;
		String processStepName;
		String proccessName;
		Integer processReturnCode = 0;
		
		
		boolean isSingleCheck = false;
		Integer singleReturnCode = 0;
		
		boolean isRangeCheck = false;
		Integer rangeStartCode = 0;
		Integer rangeEndCode = 0;;
		
		
		
		
}
