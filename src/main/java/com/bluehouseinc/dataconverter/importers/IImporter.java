package com.bluehouseinc.dataconverter.importers;

import java.util.List;

public interface IImporter<T> {
	List<T> importModel(String filePath, Class<T> type) throws Exception;
}
