package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.web.utility.RuleMatcher;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

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

    private class OrderComparetor implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    }

    @Test
    public void testDownloadManager() throws IOException {
//        Pattern pattern = Pattern.compile("(?i).+\\.(ktr|kjb)");
//        System.out.println(pattern.matcher("abc.ktr").matches());
//        File f = new File("/home/thor/test.data");
//        String name = f.getName();
//        name = f.getParent() + "/" + name.substring(0, name.length() - 4) + "json";
//        System.out.println(name);
    }

}
