package com.github.thorqin.toolkit.utility;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thor on 3/19/15.
 */
public class AppClassLoader extends ClassLoader {
    private List<String> searchDirs;

    public AppClassLoader(ClassLoader parent) {
        this(parent, (List<String>) null);
    }

    public AppClassLoader(ClassLoader parent, List<String> searchDirs) {
        super(parent);
        if (searchDirs == null) {
            this.searchDirs = new ArrayList<>();
        } else
            this.searchDirs = searchDirs;
    }

    public AppClassLoader(ClassLoader parent, final String searchDir) {
        super(parent);
        if (searchDir != null)
            this.searchDirs = new ArrayList<String>(){{add(searchDir);}};
        else
            this.searchDirs = new ArrayList<>();
    }

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        byte[] bytes = loadClassBytes(name);
        Class theClass = defineClass(name, bytes, 0, bytes.length);
        if (theClass == null) throw new ClassFormatError();
        return theClass;
    }

    @Override
    protected URL findResource(String name) {
        try {
            URL url = super.findResource(name);
            if (url != null) return url;
            url = new URL("file:///" + getResourceFile(name));
            return url;
        } catch (MalformedURLException | FileNotFoundException ex) {
            return null;
        }
    }

    private byte[] loadClassBytes(String className) throws ClassNotFoundException {
        try {
            String classFile = getClassFile(className);
            FileInputStream inputStream = new FileInputStream(classFile);
            FileChannel fileC = inputStream.getChannel();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            WritableByteChannel outC = Channels.newChannel(outputStream);
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
            while (true) {
                int i = fileC.read(buffer);
                if (i == 0 || i == -1) {
                    break;
                }
                buffer.flip();
                outC.write(buffer);
                buffer.clear();
            }
            inputStream.close();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new ClassNotFoundException(className, ex);
        }
    }

    private String getResourceFile(String name) throws FileNotFoundException {
        for (String dir: searchDirs) {
            StringBuilder sb = new StringBuilder(dir);
            sb.append(File.separator).append(name);
            File file = new File(sb.toString());
            if (file.isFile())
                return file.toString();
        }
        throw new FileNotFoundException(name);
    }

    private String getClassFile(String name) throws FileNotFoundException {
        name = name.replace('.', File.separatorChar) + ".class";
        for (String dir: searchDirs) {
            StringBuilder sb = new StringBuilder(dir);
            sb.append(File.separator).append(name);
            File file = new File(sb.toString());
            if (file.isFile())
                return file.toString();
        }
        throw new FileNotFoundException(name);
    }
}
