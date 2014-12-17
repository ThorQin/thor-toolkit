package com.github.thorqin.toolkit.log;

import java.util.HashMap;

/**
 * Created by nuo.qin on 12/15/2014.
 */
public interface Logger {
    public static class LogInfo extends HashMap<String, Object> {
        public String name;
        public String catalog;
    }
    public void log(LogInfo info);
}
