/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web.utility;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nuo.qin
 * @param <T> Stored info object type
 */
public final class RuleMatcher<T> {

    private class Rule {
        public Pattern pattern;
        public T info;
        public int order;
        public boolean useCache;
        private final Set<String> parameters;
        public Rule(String exp, Set<String> parameters, T info, int order, boolean useCache) {
            this.pattern = Pattern.compile(exp);
            this.info = info;
            this.order = order;
            this.useCache = useCache;
            this.parameters = parameters;
        }
    }

    private class OrderComparetor implements Comparator<Rule> {
        @Override
        public int compare(Rule o1, Rule o2) {
            return o1.order - o2.order;
        }
    }

    private final List<Rule> rules = new ArrayList<>();
	private final Map<String, Rule> cache = new HashMap<>();

	public void addRule(String ruleExp, Set<String> parameters, T info, int order, boolean useCache) {
        rules.add(new Rule(ruleExp, parameters, info, order, useCache));
        Collections.sort(rules, new OrderComparetor());
	}

    public void addURLRule(String urlPattern, T info, int order, boolean useCache) {
        Set<String> parameters = new HashSet<>();
        String exp = ruleToExp(urlPattern, parameters);
        addRule(exp, parameters, info, order, useCache);
    }

    public void addURLRule(String urlPattern, T info, int order) {
        addURLRule(urlPattern, info, order, true);
    }

    public void addURLRule(String urlPattern, T info) {
        addURLRule(urlPattern, info, 10000, true);
    }

    public void addURLRule(String urlPattern, T info, boolean useCache) {
        addURLRule(urlPattern, info, 10000, useCache);
    }

    public void addRule(String ruleExp, Set<String> parameters, T info) {
        addRule(ruleExp, parameters, info, 10000, true);
    }

    public void addRule(String ruleExp, T info) {
        addRule(ruleExp, null, info, 10000, true);
    }

    public void addRule(String ruleExp, Set<String> parameters, T info, boolean useCache) {
        addRule(ruleExp, parameters, info, 10000, useCache);
    }

	public void clear() {
        rules.clear();
        cache.clear();
	}
	
	public int size() {
		return rules.size();
	}

    public class Result {
        public Map<String, String> parameters = new HashMap<>();
        public T info;
    }

    private Result searchCache(String input) {
        Rule rule = cache.get(input);
        if (rule != null) {
            Result result = new Result();
            Matcher matcher = rule.pattern.matcher(input);
            if (matcher.matches()) {
                result.info = rule.info;
                if (rule.parameters != null && !rule.parameters.isEmpty()) {
                    for (String key : rule.parameters) {
                        result.parameters.put(key, matcher.group(key));
                    }
                }
                return result;
            }
        }
        return null;
    }

	public Result match(String input) {
        if (input == null)
            return null;
        Result result = searchCache(input);
        if (result != null)
            return result;
        for (Rule rule: rules) {
            Matcher matcher = rule.pattern.matcher(input);
            if (matcher.matches()) {
                result = new Result();
                result.info = rule.info;
                if (rule.useCache)
                    cache.put(input, rule);
                if (rule.parameters != null && !rule.parameters.isEmpty()) {
                    for (String key : rule.parameters) {
                        result.parameters.put(key, matcher.group(key));
                    }
                }
                return result;
            }
        }
        return null;
	}

    private static String removeKeyword(String exp) {
        return exp.replaceAll("[\\^\\$\\(\\)\\[\\]\\{\\}\\|\\+\\*\\?\\.\\\\]", "\\\\$0");
    }

    public static Pattern paramPattern = Pattern.compile(
            "\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    public static String ruleToExp(String rule, Set<String> parameters) {
        parameters.clear();
        Matcher matcher = paramPattern.matcher(rule);
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        while (matcher.find()) {
            sb.append(removeKeyword(rule.substring(pos, matcher.start())));
            pos = matcher.end();
            String key = matcher.group(1);
            if (parameters.contains(key))
                throw new InvalidParameterException("URL parameter value duplicated: " + key);
            parameters.add(key);
            sb.append("(?<").append(key).append(">[^/?]+)");
        }
        sb.append(removeKeyword(rule.substring(pos)));
        return sb.toString();
    }

}
