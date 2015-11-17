package com.github.thorqin.toolkit.utility;

import com.github.thorqin.toolkit.Application;
import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.service.TaskService;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by nuo.qin on 12/12/2014.
 */
public class UtilityTest implements FileMonitor.FileChangeListener, ConfigManager.ChangeListener {

    @Override
    public void onFileChange(File file, FileMonitor.ChangeType changeType, Object param) {
        System.out.println(file + " -> " + changeType.toString());
    }

    @Override
    public void onConfigChanged(ConfigManager configManager) {
        System.out.println(configManager.getJson("/", true));
    }

    @Test
    public void testFileMonitor() throws IOException, InterruptedException {
        FileMonitor.Monitor monitor1 = FileMonitor.watch("/home/thor/Documents/Test/1.txt", this);
        FileMonitor.Monitor monitor2 = FileMonitor.watch("/home/thor/Documents/Test/abc/2.txt", this);
        FileMonitor.Monitor monitor3 = FileMonitor.watch("/home/thor/Documents/Test/abc/def/3.txt", this);
        Thread.sleep(40000);
        monitor1.close();
        monitor2.close();
        System.out.println("step 1 finished.");
        System.in.read();
        monitor3.close();
    }


    @Test
    public void test() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AppClassLoader classLoader = new AppClassLoader(
                UtilityTest.class.getClassLoader(), "/home/thor/Workspace/AppData");
        Class<?> systemClass = classLoader.loadClass("java.lang.System");
        Method setOutMethod = systemClass.getDeclaredMethod("setOut", PrintStream.class);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream, true, "utf-8");
        setOutMethod.invoke(null, printStream);

        System.out.println("test");
        String str = byteArrayOutputStream.toString("utf-8");
    }

    @Test
    public void testConfigManager() throws IOException {
        Application application = new Application("junit-test#service");
        ConfigManager configManager = application.getConfigManager();
        System.out.println(configManager.getJson("/", true));
        configManager.addChangeListener(this);
        System.out.println(application.getDataDir("\\abc"));
        System.out.println(application.getDataDir("\\abc/def"));
        System.out.println(application.getDataDir("abc/def\\"));
        System.out.println(application.getDataDir("/abc"));
        System.out.println(new File("c:/test\\abc/def\\").toString());
                System.in.read();
    }

    @Test
    public void testTaskService() throws InterruptedException {
        TaskService<String> taskService = new TaskService<String>(new TaskService.TaskHandler<String>() {
            @Override
            public void process(String task) {
                System.out.println("in task: " + task);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        for (int i = 0; i < 5; i++) {
            taskService.offer("test" + i);
        }
        System.out.println("All task submitted!");
        //taskService.shutdown(100);
        Thread.sleep(1500);
        System.out.println(taskService.getOfferCount());
        taskService.shutdown();
    }

    @Test
    public void testPrepareSql() throws Exception {
        List<String> l = new LinkedList<String>() {
            {
                add("ab'c");
                add("def");
            }
        };
        String sql = "select * from tb where age in (?)";
        Object[] args = {l};
        DBService.PreparedInfo info = DBService.prepareSql(sql, args);
        System.out.println(info.sql);
        System.out.println(StringUtils.join(info.args));
    }

    @Test
    public void testStringFormatAndLog() {

        System.out.println(DateTime.parse("1970-01-02T23:23:"));
    }
}

