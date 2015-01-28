package com.github.thorqin.toolkit.trace;

import java.util.HashMap;

/**
 * Created by nuo.qin on 12/15/2014.
 */
public interface Tracer {
    public static class Info extends HashMap<String, Object> {
        public String name;
        public String catalog;
    }
    public void trace(Info info);
}
