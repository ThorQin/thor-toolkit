package com.github.thorqin.toolkit.utility;

import com.github.thorqin.toolkit.service.TaskService;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;


/**
 * Created by nuo.qin on 12/12/2014.
 */
public class UtilityTest {

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
        ConfigManager configManager = new ConfigManager();
        configManager.load("/home/thor/Workspace/AppData", "config.json");
        System.out.println(configManager.getJson("/", true));
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

    class A {
        public String abc = "s";
        public int def;
    }

    @Test
    public void someTest() {
        A a = new A();
        a.abc = null;
        A b = new A();
        System.out.println(Serializer.toJsonString(a));
        System.out.println(Serializer.toJsonString(b));
    }
}

