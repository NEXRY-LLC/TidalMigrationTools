package com.bluehouseinc.dataconverter.importers.csv;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.bluehouseinc.dataconverter.importers.IImporter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

public class CsvImporter<T> implements IImporter<T> {
	@Override

	public List<T> importModel(String filePath, Class<T> type) throws Exception {
		try (Reader reader = Files.newBufferedReader(Paths.get(filePath));) {
			CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader).withType(type).withIgnoreLeadingWhiteSpace(true).build();
			return csvToBean.parse();
		}
	}
}
