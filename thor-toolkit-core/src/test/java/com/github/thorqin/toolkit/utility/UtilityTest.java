package com.github.thorqin.toolkit.utility;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


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
}

