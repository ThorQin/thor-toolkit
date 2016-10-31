package com.github.thorqin.toolkit;

import com.github.thorqin.toolkit.excel.ExcelParser;
import com.github.thorqin.toolkit.excel.ExcelWriter;
import com.github.thorqin.toolkit.excel.annotation.Column;
import com.github.thorqin.toolkit.excel.annotation.Sheet;
import com.github.thorqin.toolkit.utility.Serializer;
import org.junit.Test;

import java.util.*;

/**
 * Created by thor on 8/4/16.
 */
public class TestExcel {

    @Sheet(value = "用户", required = true)
    private static class User {
        @Column(value = "姓名", requiredBy = {"年龄"}, width = 4000)
        public String name;
        @Column(value = "年龄", format = "\\d+", formatDescription = "请提供数字")
        public int age;
    }

    private static class SheetHandler implements ExcelParser.ProcessHandler {
        private ExcelWriter writer;
        public SheetHandler(ExcelWriter writer) {
            this.writer = writer;
        }

        @Override
        public void onSheetBegin(Class<?> sheetType, String sheetName) {
            writer.addSheet(sheetType);
            System.out.println("Sheet: " + sheetName);
            System.out.println("-----------------------");
        }

        @Override
        public void onSheetEnd(Class<?> sheetType, String sheetName, ExcelParser.Stat stat) {

        }

        @Override
        public void onRow(Object item, Map<String, String> errors, Map<String, String> rawData) {
            if (errors == null || errors.isEmpty()) {

            }
            writer.addRow(rawData, errors);
            System.out.print(Serializer.toJsonString(item));
            System.out.print(Serializer.toJsonString(errors));
            System.out.println(Serializer.toJsonString(rawData));
        }

        @Override
        public void onSheetError(Class<?> sheetType, String sheetName, String errorMessage, ExcelParser.Stat stat) {
            writer.addRow(new HashMap<String, String>(), Collections.singletonMap((String)null, errorMessage));
            System.out.println("Process sheet (" + sheetName + ") error: " + errorMessage);
        }
    }
    @Test
    public void testParser() throws Exception {
        try (ExcelWriter writer = new ExcelWriter("/home/thor/Downloads/输出.xlsx")) {
            SheetHandler handler = new SheetHandler(writer);
            List<ExcelParser.Stat> statList = ExcelParser.parse("/home/thor/Downloads/测试.xlsx", new Class<?>[]{User.class}, handler);
            if (statList.isEmpty())
                System.out.println("No sheet can import");
            boolean errorRaised = false;
            for (ExcelParser.Stat stat : statList) {
                if (stat.fatalError != null) {
                    System.out.println(stat.sheetName + ": fatal error: " + stat.fatalError);
                } else
                    System.out.println(stat.sheetName + ": success: " + stat.successCount + ", failed: " + stat.failedCount);
            }
            writer.save();
        }
    }

}
