package com.bluehouseinc.dataconverter.importers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.bluehouseinc.dataconverter.providers.ImportProvider;
import com.bluehouseinc.tidal.api.exceptions.TidalException;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import lombok.extern.log4j.Log4j2;

@Log4j2

public abstract class AbstractCsvImporter {

	public static <T> List<T> importFromFile(ImportProvider importProvider, String filePath, Class<T> type) throws Exception {
		@SuppressWarnings("unchecked")
		IImporter<T> importer = importProvider.getImporter(filePath);
		return importer.importModel(filePath, type);
	}


	public static <E> List<E> fromFile(File file, Class<E> cls) {

		List<E> data = new ArrayList<>();

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
				data = beanBuilder.build().parse();// .forEach(e -> log.error(e.toString()));

			} catch (IOException e) {
				throw new TidalException("AbstractCsvImporter Error in fromFile ", e);
			}

			return data;
		}
	}

}
