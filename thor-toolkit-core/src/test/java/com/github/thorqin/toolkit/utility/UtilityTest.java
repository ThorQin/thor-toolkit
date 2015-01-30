package com.github.thorqin.toolkit.utility;

import com.github.thorqin.toolkit.trace.TraceService;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by nuo.qin on 12/12/2014.
 */
public class UtilityTest {
    public static class MyTracer extends TraceService {
        @Override
        protected void onTraceInfo(Info info) {
        }
    }
    public static class MyType {
        public List<DateTime> abc = new ArrayList<>();
    }
    @Test
    public void test() throws IOException {
        ConfigManager config = new ConfigManager(new File("C:\\Users\\nuo.qin\\Workspace\\test.json"));
		List<DateTime> myObj = config.getList("root/abc", DateTime.class);
        for (DateTime dateTime : myObj) {
			System.out.println(dateTime);
		}
		System.out.println(myObj.size());
    }

    @Test
    public void testSplit() {
        String[] strings = "".split("/");
        System.out.println(strings.length);
    }

    @Test
    public void testSort() {
        List<Integer> list = new LinkedList<>();
        list.add(2);
        list.add(1);
        list.add(3);
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        System.out.println(list);
    }
}
