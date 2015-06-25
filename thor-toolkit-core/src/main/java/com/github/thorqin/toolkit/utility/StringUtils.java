/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.thorqin.toolkit.utility;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author nuo.qin
 */
public final class StringUtils {
	private static Pattern intPattern =
			Pattern.compile("^[+-]?\\d+$");
	private static Pattern doublePattern =
			Pattern.compile("^[+-]?\\d*\\.\\d+$");

	public static boolean isInteger(String text) {
		return intPattern.matcher(text).find();
	}

	public static boolean isDouble(String text) {
		return doublePattern.matcher(text).find();
	}

	public static boolean isNumber(String text) {
		return isInteger(text) || isDouble(text);
	}

	public static Set<String> toStringSet(String value) {
		return toStringSet(value, ",");
	}

	public static Set<String> toStringSet(String value, String delimiter) {
		Set<String> result = new HashSet<>();
		if (value == null)
			return result;
		String[] array = value.split(delimiter);
		for (String v: array)
			if (v != null && !v.isEmpty())
				result.add(v);
		return result;
	}

	public static String join(Iterable<String> array) {
		return join(array, ",");
	}

	public static String join(Iterable<String> array, String delimiter) {
		if (array == null)
			return "";
		StringBuilder result = new StringBuilder();
		Iterator<String> it = array.iterator();
		boolean first = true;
		while (it.hasNext()) {
			if (first)
				first = false;
			else
				result.append(delimiter);
			result.append(it.next());
		}
		return result.toString();
	}

	public static String join(Object[] array) {
		return join(array, ",");
	}

	public static String join(Object[] array, String delimiter) {
		if (array == null)
			return "";
		// Cache the length of the delimiter
		// has the side effect of throwing a NullPointerException if
		// the delimiter is null.
		int delimiterLength = delimiter.length();
		// Nothing in the array return empty string
		// has the side effect of throwing a NullPointerException if
		// the array is null.
		if (array.length == 0) {
			return "";
		}
		// Only one thing in the array, return it.
		if (array.length == 1) {
			if (array[0] == null) {
				return "";
			}
			return array[0].toString();
		}
		// Make a pass through and determine the size
		// of the resulting string.
		int length = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				length += array[i].toString().length();
			}
			if (i < array.length - 1) {
				length += delimiterLength;
			}
		}
		// Make a second pass through and concatenate everything
		// into a string buffer.
		StringBuilder result = new StringBuilder(length);
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				result.append(array[i].toString());
			}
			if (i < array.length - 1) {
				result.append(delimiter);
			}
		}
		return result.toString();
	}
	
	/**
	 * Convert name from 'xxxYyyyZzzz' value to 'xxxx_yyyy_zzzz' value
	 * @param name String that separated in camel form
	 * @return String that separated by underline
	 */
	public static String camelToUnderline(String name) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (ch >= 'A' && ch <= 'Z') {
				if (sb.length() > 0) {
					sb.append('_');
					sb.append((char)(ch + 32));
				} else
					sb.append((char)(ch + 32));
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	public static DateTime parseISO8601(String dateTime) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();
		return formatter.parseDateTime(dateTime);
	}

	public static String toISO8601(DateTime dateTime) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return dateTime.toDateTimeISO().toString(formatter);
	}
	
	/**
	 * Convert name from 'xxxx_yyyy_zzzz' value to 'xxxYyyyZzzz' value
	 * @param name String that separated by underline
	 * @return String that separated in camel form
	 */
	public static String underlineToCamel(String name) {
		StringBuilder sb = new StringBuilder();
		boolean upperCaseLetter = false;
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (ch == '_') {
				upperCaseLetter = true;
			} else {
				if (upperCaseLetter && ch >= 'a' && ch <= 'z') {
					sb.append((char)(ch - 32));
				} else
					sb.append(ch);
				upperCaseLetter = false;
			}
		}
		return sb.toString();
	}
}
