package com.github.thorqin.toolkit.utility;

import com.github.thorqin.toolkit.log.LogService;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuo.qin on 12/12/2014.
 */
public class UtilityTest {
    public static class MyLogger extends LogService {
        @Override
        protected void onLog(LogInfo info) {
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
}
