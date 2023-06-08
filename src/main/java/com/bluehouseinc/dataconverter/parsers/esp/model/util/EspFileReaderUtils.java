package com.bluehouseinc.dataconverter.parsers.esp.model.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.bluehouseinc.dataconverter.common.utils.RegexHelper;
import com.bluehouseinc.tidal.utils.StringUtils;

import lombok.extern.log4j.Log4j2;


public abstract class EspFileReaderUtils {
	private final static String EMPTY_LINE_PATTERN = "^\\s*$";
	private final static String COMMENT_PATTERN = "^\\/*(.*) *\\/$";

	private EspFileReaderUtils() {

	}

	public static String readLineTrimmed(final BufferedReader reader) throws IOException {
		String line = reader.readLine();
		return line != null ? line.trim() : null;
	}

	/**
	 * merges the line if the line ends with the newline character.
	 *
	 * @param reader
	 * @param line    current line
	 * @param newline
	 * @return
	 * @throws IOException
	 */
	public static String readLineMerged(BufferedReader reader, String line, Character newline) throws IOException {

		if (newline != null) { // Ignore if null
			while (line.endsWith(newline.toString())) {
				String nextLine = readLineTrimmed(reader);
				String substringline = line.substring(0, line.length() - 1);
				String nextlinedata = nextLine.trim();
				line = String.join(" ", substringline, nextlinedata);
			}

		}
		return line;
	}

	public static List<String> parseJobLines(BufferedReader reader, String endofdata, Character newline) throws IOException {
		return parseJobLines(reader, endofdata, newline, true);
	}

	/**
	 * Reads all lines until the end of our data and merges the line if the line ends with the newline character. Skips empty lines and comment lines.
	 *
	 * @param reader
	 * @param endofdata - Read all lines until you reach this line matching.
	 * @param newline   - In ESP the data len is static so you will find a - or + char to indicate the next line belongs to this line.
	 * @return List of lines
	 * @throws IOException
	 */
	public static List<String> parseJobLines(BufferedReader reader, String endofdata, Character newline, boolean checkskip) throws IOException {
		List<String> lines = new ArrayList<>();
		String line;
		while (!Objects.equals(line = readLineTrimmed(reader), endofdata)) {

			if (checkskip) {
				if (skippedLine(line)) {
					continue;
				}
			}
			
			line = readLineMerged(reader, line, newline);
			lines.add(line.trim());
		}
		return lines;
	}

	public static boolean skippedLine(String line) {
		if (StringUtils.isBlank(line) || isEmptyLine(line) || isCommentLine(line)) {
			return true;
		}

		return false;
	}

	public static boolean isEmptyLine(String line) {
		return line.matches(EMPTY_LINE_PATTERN);
	}

	private static boolean isCommentLine(String line) {
		return RegexHelper.matchesRegexPattern(line, COMMENT_PATTERN) || line.startsWith("/*") || line.startsWith("#") || line.startsWith("//");
	}

}
