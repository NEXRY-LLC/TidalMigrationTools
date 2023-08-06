package com.bluehouseinc.dataconverter.parsers.tivoli.data.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.dataconverter.parsers.esp.model.util.EspFileReaderUtils;
import com.bluehouseinc.dataconverter.parsers.tivoli.data.schedule.TivoliScheduleProcessor;
import com.bluehouseinc.tidal.api.exceptions.TidalException;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Component
public class TivoliResourceProcessor {
	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	private  static String RES_PATTERN = "^(\\w+)#(\\w+)(.*)";

	@Setter(value = AccessLevel.PRIVATE)
	@Getter(value = AccessLevel.PRIVATE)
	Map<String, List<ResourceData>> data = new HashMap<>();

	public void doProcessFile(File datafile) {

		BufferedReader reader = null;

		try {
			if (datafile == null) {
				throw new TidalException("Missing cpu file");
			}

			reader = new BufferedReader(new FileReader(datafile));

			String line;

			while ((line = EspFileReaderUtils.readLineTrimmed(reader)) != null) {

				line.trim();

				if (EspFileReaderUtils.skippedLine(line)) {
					continue;
				}

				if (isResourcePattern(line)) {

					String group = RegexHelper.extractNthMatch(line, RES_PATTERN,0);
					String name = RegexHelper.extractNthMatch(line, RES_PATTERN,1);
					String info = RegexHelper.extractNthMatch(line, RES_PATTERN,2);
					
					
					String resdata[] = info.trim().split(" ", 2); // Should always be two

					ResourceData resource = new ResourceData();
					resource.setName(name);
					resource.setValue(resdata[0].trim());
					resource.setGroupName(group);
					
					if (resdata.length > 1) {
						resource.setDescription(resdata[1]);
					}

					if(this.data.containsKey(group)) {
						this.data.get(group).add(resource);
					}else {
						List<ResourceData> reslist = new ArrayList<>();
						reslist.add(resource);
						this.data.put(group, reslist);
					}
					
				}
			}
		} catch (Exception e) {
			throw new TidalException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {

			}
		}
	}

	private boolean isResourcePattern(String line) {
		return RegexHelper.matchesRegexPattern(line, RES_PATTERN);
	}

	public ResourceData getResourceInGroupByName(String group, String name) {
		
		List<ResourceData> resdata = this.data.get(group);
		
		if(resdata == null) {
			return null;
		}
		return resdata.stream().filter(f -> f.getName().trim().equalsIgnoreCase(name.trim())).findFirst().orElse(null);
	}
}
