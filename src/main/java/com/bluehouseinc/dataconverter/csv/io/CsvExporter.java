package com.bluehouseinc.dataconverter.csv.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.opencsv.ICSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class CsvExporter {
	public static String PATH = "./export";

	public static <T> void WriteToFile(String fileName, List<T> data) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		
		File dir = new File(PATH);
		
	 Path reportfile = 	Paths.get(PATH + "/" + fileName);
		
		if (!dir.exists()) {
			dir.mkdirs();

		}
		

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(PATH + "/" + fileName))) {

			StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder(writer).withQuotechar(ICSVWriter.NO_QUOTE_CHARACTER).build();

			beanToCsv.write(data);
		}
	}
}
