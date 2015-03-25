package com.github.thorqin.toolkit.trace;

import java.util.HashMap;

/**
 * Created by nuo.qin on 12/15/2014.
 */
public interface Tracer {
    public static class Info extends HashMap<String, Object> {
        public String name;
        public String catalog;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(catalog).append(":").append(name).append("] ");
            boolean first = true;
            for (String key : keySet()) {
                if (!first)
                    sb.append("; ");
                sb.append(key).append("=").append(get(key).toString());
                first = false;
            }
            return sb.toString();
        }
    }
    public void trace(Info info);
}
