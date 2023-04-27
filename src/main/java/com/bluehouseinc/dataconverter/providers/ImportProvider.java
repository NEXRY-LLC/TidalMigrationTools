package com.bluehouseinc.dataconverter.providers;

import org.springframework.stereotype.Component;

import com.bluehouseinc.dataconverter.common.exceptions.ImporterNotSupportedException;
import com.bluehouseinc.dataconverter.common.utils.FileHelper;
import com.bluehouseinc.dataconverter.importers.IImporter;
import com.bluehouseinc.dataconverter.importers.csv.CsvImporter;

@Component
public class ImportProvider {
	public IImporter getImporter(String filePath) throws ImporterNotSupportedException {
		String ext = FileHelper.getFileExtension(filePath);

		return this.getImporterByExtension(ext);
	}

	private IImporter getImporterByExtension(String fileExtension) throws ImporterNotSupportedException {
		switch (fileExtension) {
		case "csv":
			return new CsvImporter();

		default:
			throw new ImporterNotSupportedException();
		}
	}

}
