/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web.utility;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nuo.qin
 * @param <T> Stored info object type
 */
public class RuleMatcher<T> {
	private final List<SimpleEntry<String, T>> resultList = new ArrayList<>();
	private final Map<String, Integer> ruleMap = new HashMap<>();
	private final static String groupPrefix = "_RG_";
	private Pattern pattern = null;
	
	public void addRule(String ruleRegExp, T info) {
		if (ruleMap.containsKey(ruleRegExp)) {
			resultList.set(ruleMap.get(ruleRegExp), new SimpleEntry<>(ruleRegExp,info));
		} else {
			resultList.add(new SimpleEntry<>(ruleRegExp,info));
			ruleMap.put(ruleRegExp, resultList.size() - 1);
		}
		pattern = null;
	}
	public void removeRule(String ruleRegExp) {
		if (ruleMap.containsKey(ruleRegExp)) {
			resultList.remove((int)ruleMap.get(ruleRegExp));
			ruleMap.clear();
			for (int i = 0; i < resultList.size(); i++)
				ruleMap.put(resultList.get(i).getKey(), i);
		}
		pattern = null;
	}
	public void clear() {
		ruleMap.clear();
		resultList.clear();
		pattern = null;
	}
	public void build() {
		String regText = "";
		for (int i = 0; i < resultList.size(); i++) {
			SimpleEntry<String, T> entry = resultList.get(i);
			String groupName = groupPrefix + i;
			if (!regText.isEmpty())
				regText += "|";
			regText += "(?<" + groupName + ">" + entry.getKey() + ")";
		}
		pattern = Pattern.compile(regText);
	}
	
	public int size() {
		return resultList.size();
	}
	
	public T match(String input) {
		if (pattern == null)
			build();
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			for (int i = 0; i < resultList.size(); i++) {
				SimpleEntry<String, T> entry = resultList.get(i);
				String groupName = groupPrefix + i;
				if (matcher.group(groupName) != null) {
					return entry.getValue();
				}
			}
			return null;
		} else
			return null;
	}
	
	public static boolean matchUrlRule(String rule, String url, Map<String, String> parameters) {
		Map<String, Integer> paramSet = new HashMap<>();
		String regexp = formatUrlRule(rule, paramSet);
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(url);
		if (!matcher.find())
			return false;
		if (parameters != null)
			parameters.clear();
		else
			return true;
		int gCount = matcher.groupCount();
		for (String k : paramSet.keySet()) {
			int idx = paramSet.get(k);
			if (idx <= gCount) {
				parameters.put(k, matcher.group(idx));
			}
		}
		return true;
	}
	
	public static String formatUrlRule(String input, Map<String, Integer> paramSet) {
		if (paramSet != null)
			paramSet.clear();
		input = input.replaceAll("[\\^\\$\\(\\)\\[\\]\\|\\?\\.\\\\]", "\\\\$0");
		input = input.replaceAll("\\*", "[^/]*");
		input = input.replaceAll("\\+", ".*");
		input = input.replaceAll("\\\\\\?\\.\\*", "(\\\\?.*)?");
		Pattern pt = Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9]*)\\}");
		Matcher matcher = pt.matcher(input);
		StringBuilder result = new StringBuilder();
		result.append("^");
		int pos = 0;
		int findPos = 1;
		while (matcher.find()) {
			result.append(input.subSequence(pos, matcher.start()));
			result.append("(");
			if (paramSet != null)
				paramSet.put(matcher.group(1), findPos++);
			result.append("[^&=/\\?\\\\]+)");
			pos = matcher.end();
		}
		if (pos < input.length())
			result.append(input.subSequence(pos, input.length()));
		result.append("$");
		return result.toString();
	}
}
