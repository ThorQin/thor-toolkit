package com.github.thorqin.toolkit;

import com.github.thorqin.toolkit.excel.ExcelParser;
import com.github.thorqin.toolkit.excel.annotation.Column;
import com.github.thorqin.toolkit.excel.annotation.Sheet;
import com.github.thorqin.toolkit.utility.Serializer;
import org.junit.Test;
import java.util.List;
import java.util.Map;

/**
 * Created by thor on 8/4/16.
 */
public class TestExcel {

    @Sheet(value = "用户", required = true)
    private static class User {
        @Column(value = "姓名", requiredBy = {"年龄"})
        public String name;
        @Column(value = "年龄", format = "\\d+", formatDescription = "请提供数字")
        public int age;
    }

    private static class SheetHandler implements ExcelParser.ProcessHandler {

        @Override
        public void onSheet(String sheetName) {
            System.out.println("Sheet: " + sheetName);
            System.out.println("-----------------------");
        }

        @Override
        public void onRow(Object item, Map<String, String> errors, Map<String, String> rawData) {
            System.out.print(Serializer.toJsonString(item));
            System.out.print(Serializer.toJsonString(errors));
            System.out.println(Serializer.toJsonString(rawData));
        }

        @Override
        public void onSheetError(String sheetName, String errorMessage) {
            System.out.println("Process sheet (" + sheetName + ") error: " + errorMessage);
        }
    }
    @Test
    public void testParser() throws Exception {
        SheetHandler handler = new SheetHandler();
        List<ExcelParser.Stat> statList = ExcelParser.parse("/home/thor/Downloads/测试.xlsx", new Class<?>[]{User.class}, handler);
        if (statList.isEmpty())
            System.out.println("No sheet can import");
        for (ExcelParser.Stat stat: statList) {
            if (stat.fatalError != null) {
                System.out.println(stat.sheetName + ": fatal error: " + stat.fatalError);
            } else
                System.out.println(stat.sheetName + ": success: " + stat.successCount + ", failed: " + stat.failedCount);
        }
    }
}
