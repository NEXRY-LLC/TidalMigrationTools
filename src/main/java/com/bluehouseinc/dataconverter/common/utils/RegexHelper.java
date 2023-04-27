package com.bluehouseinc.dataconverter.common.utils;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexHelper {

	public static boolean matchesRegexPattern(String line, String regexPattern) {
		if(line == null) {
			return false;
		}
		return Pattern.compile(regexPattern).matcher(line).find();
	}

	public static String extractNthMatch(String line, String regexPattern, int nthMatch) {
		if(line == null) {
			return line;
		}

		Pattern p = Pattern.compile(regexPattern);
		Matcher m = p.matcher(line);
		if (m.find()) {
			return m.group(++nthMatch);
		} // it starts at 1

		return null;
	}

	public static String extractFirstMatch(String line, String regexPattern) {
		return extractNthMatch(line, regexPattern, 0);
	}

	public static String replaceAllSameLength(String input, String regex, String ch) {
		if(input == null) {
			return input;
		}

		// final String input = "pw:(abc) something!";

		Matcher matcher = Pattern.compile(regex).matcher(input);

		if (matcher.find()) {
			final String onlyPw = matcher.group();
			// final String stars = "".reperepeat("*", onlyPw.length());
			// String stars = new String(new char[onlyPw.length()]).replace("\0", ch);

			String stars = String.join("", Collections.nCopies(onlyPw.length(), ch));
			// System.out.println(stars);

			// System.out.println(input.replace(onlyPw, stars));
			return input.replace(onlyPw, stars);
		}

		return input;
	}
}
