package com.bluehouseinc.dataconverter.common.utils;

public final class FileHelper {

	public static String getFileExtension(String filePath) {
		int lastIndexOf = filePath.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // no extension
		}
		return filePath.substring(++lastIndexOf);
	}
}
