package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.utility.MimeUtils;
import com.github.thorqin.toolkit.web.utility.RuleMatcher;
import com.github.thorqin.toolkit.web.utility.ServletUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
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

    private String fileIdToPath(String fileId) {
        String uploadDir = "basedir";
        StringBuilder sb = new StringBuilder(uploadDir.length() + 50);
        sb.append(uploadDir).append("/");
        int len = fileId.length(), i = 0, scan = 0;
        while (i < 5 && scan + 2 < len) {
            sb.append(fileId.substring(scan, scan + 2));
            sb.append('/');
            i++;
            scan += 2;
        }
        sb.append(fileId);
        return sb.toString();
    }

    private String filePathToId(File file) {
        String uploadDir = "basedir";
        String path = file.getAbsoluteFile().getPath();
        String basePath = new File(uploadDir).getAbsoluteFile().getPath();
        if (!path.startsWith(basePath)) {
            return null;
        }
        if (!path.endsWith(".data")) {
            return null;
        }
        String name = file.getName();
        return name.substring(0, name.length() - 5);
    }

    @Test
    public void testDownloadManager() throws IOException {
//        Pattern pattern = Pattern.compile("(?i).+\\.(ktr|kjb)");
//        System.out.println(pattern.matcher("abc.ktr").matches());
//        File f = new File("/home/thor/test.data");
//        String name = f.getName();
//        name = f.getParent() + "/" + name.substring(0, name.length() - 4) + "json";
//        System.out.println(name);
//        String fileId = UUID.randomUUID().toString().replaceAll("-", "");
//        System.out.println(fileId);
//        String path = fileIdToPath(fileId);
//        System.out.println(path);
//
//        System.out.println(filePathToId(new File(path + ".data")));

    }

    @Test
    public void testGetFileName() {
        System.out.println(ServletUtils.getFilenameFromContentDisposition(
                "attachment; filename*=\"utf-8''中文.pdf\""));
        System.out.println(ServletUtils.getFilenameFromContentDisposition(
                "attachment; filename*=\"中文1.pdf\""));
        System.out.println(ServletUtils.getFilenameFromContentDisposition(
                "inline; filename= \"中文2 def.pdf\""));

        System.out.println(ServletUtils.getFilenameFromUrl(
                "http://10.152.71.33/MOA_Attachments/app_flowsupport/LC_2115520CD0ECB6D748257F260031D984_1451034269749/CB9FF2CCD167191E482580040040AE6A_1470224631276/%E6%96%B0%E5%BB%BA%20Microsoft%20Office%20PowerPoint%20%E6%BC%94%E7%A4%BA%E6%96%87%E7%A8%BF.pptx?abc"));

    }

    @Test
    public void printMime() {
        List<String> list = new LinkedList<>();
        for ( HashMap.Entry<String, String> entry: MimeUtils.MIME.entrySet()) {
            list.add("MIME_SUFFIX.put(\"" + entry.getValue() + "\", \"" + entry.getKey() + "\");");
        }

        Collections.sort(list);
        for (String s: list) {
            System.out.println(s);
        }
    }
}
