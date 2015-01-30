package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.web.utility.RuleMatcher;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuo.qin on 1/30/2015.
 */
public class UtilityTest {

    @Test
    public void testMatcher() {
        RuleMatcher<String> ruleMatcher = new RuleMatcher<>();
        ruleMatcher.addURLRule("/abc/thor/test", null);
        ruleMatcher.addURLRule("/abc/{user}/{name}", "ok", 10002, false);
        long begin = System.currentTimeMillis();
        long count = 0;
        for (int i = 0; i < 10; i++) {
            RuleMatcher.Result info = ruleMatcher.match("/abc/thor/test");
            if (info != null)
                count++;
        }
        System.out.println("Use time: " + (System.currentTimeMillis() - begin));
        System.out.println("Matched: " + count);
    }
}
