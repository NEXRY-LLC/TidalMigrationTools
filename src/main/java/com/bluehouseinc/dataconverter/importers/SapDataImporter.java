package com.bluehouseinc.dataconverter.importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.importers.csv.CsvSAPData;
import com.bluehouseinc.dataconverter.providers.ConfigurationProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class SapDataImporter {
	private static final String SAPDATAFILE = "SAP.DataFile";
	private List<CsvSAPData> sapdata;

	private ConfigurationProvider cfgProvider;

	private List<String> jobNames = new ArrayList<>();;
	
	public SapDataImporter(ConfigurationProvider cfgProvider) {
		this.sapdata = new ArrayList<>();
		this.cfgProvider = cfgProvider;
	}

	public String getSapDataFile() {
		return cfgProvider.getConfigurations().getOrDefault(SAPDATAFILE, null);
	}

	public List<CsvSAPData> getSAPData() {
		String datafile = getSapDataFile();

		if (datafile != null) {
			if (this.sapdata.isEmpty()) {

				File file = new File(datafile);
				log.debug("doHandleSAP Reading Data [" + datafile + "]");
				this.sapdata = AbstractCsvImporter.fromFile(file, CsvSAPData.class);
			}
		}

		return this.sapdata;
	}

	public CsvSAPData getDataByJobName(String name) {
		return getSAPData().stream().filter(f -> f.getJobName().equalsIgnoreCase(name)).findAny().orElse(null);
	}
	
	public void addJobName(String name) {
		if(jobNames.contains(name)) {
			
		}else {
			jobNames.add(name);
		}
	}
	
	
	public <E> CsvToBean<E> fromFile(File file, Class<E> cls) {

		CsvToBean<E> data = null;//new ArrayList<>();

		if (file == null) {
			return data;
		} else if (!file.exists()) {
			throw new TidalException("fromFile ERROR[" + file.getAbsolutePath() + "] does not exist");
		} else {

			try {
				log.info("AbstractCsvImporter.fromFile[" + file.getAbsolutePath() + "] processing data.");
				CsvToBeanBuilder<E> beanBuilder = new CsvToBeanBuilder<>(new InputStreamReader(new FileInputStream(file)));
				beanBuilder.withType(cls)
				.withIgnoreLeadingWhiteSpace(true)
				.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
				.withExceptionHandler(null)
				.withIgnoreEmptyLine(true)
				.withQuoteChar(new Character('"'));
				// build methods returns a list of Beans
				data = beanBuilder.build();// .forEach(e -> log.error(e.toString()));

			} catch (IOException e) {
				throw new TidalException("AbstractCsvImporter Error in fromFile ", e);
			}

			return data;
		}
	}
	
}
