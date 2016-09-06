package com.github.thorqin.toolkit.utility;

import com.github.thorqin.toolkit.Application;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by thor on 9/6/16.
 */
public class ClassScanner {

    public interface ClassHandler {
        void handleClass(Class<?> clazz) throws Exception;
    }

    private static List<String> loadDefaultScanPackages() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = loader.getResourceAsStream("packages.json");
        if (inputStream == null)
            return null;
        try (InputStreamReader reader = new InputStreamReader(inputStream, "utf-8")) {
            return Serializer.fromJson(reader, Serializer.STRING_LIST);
        } catch (IOException e) {
            System.err.println("Load class scanner setting failed: " + e.getMessage());
            return null;
        }
    }

    public static void scanByAnnotation(String annotationClassName, ClassHandler handler) throws Exception {
        List<String> scanPackages = loadDefaultScanPackages();
        scanByAnnotation(scanPackages, annotationClassName, handler);
    }

    public static void scanByAnnotation(List<String> packageNames, String annotationClassName, ClassHandler handler) throws Exception {
        URL root = Application.class.getResource("/");
        if (root != null) {
            scanFile(new File(root.toURI()), annotationClassName, handler);
        } else {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL[] urls = ((URLClassLoader)loader).getURLs();
            for (URL url: urls) {
                String path = url.getPath();
                if (path != null && path.matches("(?i).+(\\.jar|\\.war)")) {
                    scanJarByAnnotation(url, packageNames, annotationClassName, handler);
                }
            }
        }
    }

    private static boolean isValidClass(List<String> packageNames, String classPath) {
        if (!classPath.endsWith(".class"))
            return false;
        if (packageNames == null)
            return true;
        else {
            for (String name: packageNames) {
                if (classPath.startsWith(name))
                    return true;
            }
            return false;
        }
    }

    public static void scanJarByAnnotation(URL jarURL, String annotationClassName, ClassHandler handler) throws Exception {
        List<String> scanPackages = loadDefaultScanPackages();
        scanJarByAnnotation(jarURL, scanPackages, annotationClassName, handler);
    }

    public static void scanJarByAnnotation(URL jarURL, List<String> packageNames, String annotationClassName, ClassHandler handler) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String path = jarURL.getPath();
        JarFile jarFile = new JarFile(path);
        Enumeration<JarEntry> entrys = jarFile.entries();
        while (entrys.hasMoreElements()) {
            JarEntry jarEntry = entrys.nextElement();
            String entryName = jarEntry.getName();
            if (isValidClass(packageNames, entryName)) {
                InputStream inputStream = loader.getResourceAsStream(entryName);
                parseClass(inputStream, annotationClassName, handler);
            }
        }
    }

    private static void scanFile(File path, String annotationClassName, ClassHandler handler) throws Exception {
        if (path == null) {
            return;
        }
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File item : files) {
                    scanFile(item, annotationClassName, handler);
                }
            }
            return;
        }
        else if (!path.isFile() || !path.getName().endsWith(".class")) {
            return;
        }
        String filePath = path.getPath();

        parseClass(new FileInputStream(filePath), annotationClassName, handler);
    }

    private static void parseClass(InputStream inputStream, String annotationClassName, ClassHandler handler) throws Exception {
        try (DataInputStream fstream = new DataInputStream(inputStream)){
            ClassFile cf = new ClassFile(fstream);
            String className = cf.getName();
            AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(
                    AnnotationsAttribute.visibleTag);
            if (visible == null) {
                return;
            }
            for (javassist.bytecode.annotation.Annotation ann : visible.getAnnotations()) {
                if (!ann.getTypeName().equals(annotationClassName)) {
                    continue;
                }
                Class<?> clazz = Class.forName(className);
                if (clazz == null) {
                    continue;
                }
                if (handler != null)
                    handler.handleClass(clazz);
            }
        }
    }
}
