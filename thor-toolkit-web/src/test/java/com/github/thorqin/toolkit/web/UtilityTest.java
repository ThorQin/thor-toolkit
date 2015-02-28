package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.web.utility.DownloadManager;
import com.github.thorqin.toolkit.web.utility.RuleMatcher;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
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

    @Test
    public void testDownloadManager() throws IOException {
        File f = new File("/home/thor/test.data");
        String name = f.getName();
        name = f.getParent() + "/" + name.substring(0, name.length() - 4) + "json";
        System.out.println(name);
    }

}
