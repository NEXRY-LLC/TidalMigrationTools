package com.bluehouseinc.dataconverter.parsers.esp.reporter;

import com.bluehouseinc.dataconverter.model.BaseParserDataModel;
import com.bluehouseinc.dataconverter.model.IReporter;
import com.bluehouseinc.dataconverter.parsers.esp.model.EspDataModel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EspComplexSchedEventData implements IReporter {
	EspDataModel espmodel;
	
	@Override
	public <B extends BaseParserDataModel<?, ?>> void doPrint(B model) {

		espmodel = (EspDataModel) model;

		int totalcount = espmodel.getScheduleEventDataProcessor().getAdvancedEventData().size();
		
		log.trace("Advance Schedule Data Total {}", totalcount);

		espmodel.getScheduleEventDataProcessor().getAdvancedEventData().forEach(f -> {

			
			log.trace("{}", f.getRawEventLine());
			f.getRawDataLines().forEach( d -> {
				log.trace(d);
				});
			log.trace("\n");
		});

		log.trace("Advance Schedule Data Total {}", totalcount);
	}



}
