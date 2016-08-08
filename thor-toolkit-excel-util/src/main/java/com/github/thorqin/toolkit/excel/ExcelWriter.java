package com.github.thorqin.toolkit.excel;

import com.github.thorqin.toolkit.excel.annotation.Column;
import com.github.thorqin.toolkit.excel.annotation.Sheet;
import com.github.thorqin.toolkit.utility.Serializer;
import com.google.common.base.Strings;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by thor on 8/5/16.
 */
public class ExcelWriter implements AutoCloseable {

    public static class Option {
        public String textColor = "#000000";
        public String fillColor = "#FFFFFF";
        public String font = "Microsoft Yahei";;
        public short fontSize = 11;

        public String headerTextColor = "#000000";
        public String headerFillColor = "#CCCCFF";
        public String headerFont = "Microsoft Yahei";
        public short headerFontSize = 11;

        public Color getHeaderFillColor() {
            if (headerFillColor == null)
                return new Color(255, 255, 255);
            if (!headerFillColor.matches("#[0-9a-fA-F]{6}")) {
                return new Color(255, 255, 255);
            }
            return new Color(Integer.valueOf(headerFillColor.substring(1), 16));
        }

        public Color getHeaderTextColor() {
            if (headerTextColor == null)
                return new Color(0, 0, 0);
            if (!headerTextColor.matches("#[0-9a-fA-F]{6}")) {
                return new Color(0, 0, 0);
            }
            return new Color(Integer.valueOf(headerTextColor.substring(1), 16));
        }

        public Color getFillColor() {
            if (fillColor == null)
                return new Color(255, 255, 255);
            if (!fillColor.matches("#[0-9a-fA-F]{6}")) {
                return new Color(255, 255, 255);
            }
            return new Color(Integer.valueOf(fillColor.substring(1), 16));
        }

        public Color getTextColor() {
            if (textColor == null)
                return new Color(0, 0, 0);
            if (!textColor.matches("#[0-9a-fA-F]{6}")) {
                return new Color(0, 0, 0);
            }
            return new Color(Integer.valueOf(textColor.substring(1), 16));
        }
    }

    private SXSSFWorkbook wb = null;
    private File targetFile = null;
    private SXSSFSheet currentSheet = null;
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle cellStyle;
    private XSSFCellStyle errorStyle;
    private List<Column> currentColumns;
    private Set<String> addedSheets = new HashSet<>();
    private int currentRow = 0;


    public ExcelWriter(String filePath) {
        this(new File(filePath), null);
    }

    public ExcelWriter(String filePath, Option option) {
        this(new File(filePath), option);
    }

    public ExcelWriter(File file) {
        this(file, null);
    }

    public ExcelWriter(File file, Option option) {
        targetFile = file;
        wb = new SXSSFWorkbook();
        if (option == null)
            option = new Option();

        StylesTable stylesTable = wb.getXSSFWorkbook().getStylesSource();
        headerStyle = stylesTable.createCellStyle();
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(option.getHeaderFillColor()));
        XSSFFont headerFont = wb.getXSSFWorkbook().createFont();
        headerFont.setFontHeightInPoints(option.headerFontSize);
        headerFont.setColor(new XSSFColor(option.getHeaderTextColor()));
        headerFont.setFontName(option.headerFont);
        headerStyle.setFont(headerFont);

        cellStyle = stylesTable.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(new XSSFColor(option.getFillColor()));
        XSSFFont cellFont = wb.getXSSFWorkbook().createFont();
        cellFont.setFontHeightInPoints(option.fontSize);
        cellFont.setColor(new XSSFColor(option.getTextColor()));
        cellFont.setFontName(option.font);
        cellStyle.setFont(cellFont);

        errorStyle = stylesTable.createCellStyle();

        errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        errorStyle.setFillForegroundColor(new XSSFColor(Color.YELLOW));
        XSSFFont errorFont = wb.getXSSFWorkbook().createFont();
        errorFont.setFontHeightInPoints(option.fontSize);
        errorFont.setColor(new XSSFColor(Color.RED));
        errorFont.setFontName(option.font);
        errorStyle.setFont(errorFont);
    }

    private static class ColumnComparator implements Comparator<Column> {
        @Override
        public int compare(Column o1, Column o2) {
            return Integer.compare(o1.order(), o2.order());
        }
    }

    public void addSheet(Class<?> type) {
        if (type == null)
            throw new IllegalArgumentException("Must provide a class type for a sheet!");
        Sheet sheetAnno = type.getAnnotation(Sheet.class);
        if (sheetAnno == null) {
            throw new IllegalArgumentException("Given class type has no annotation whit type 'Sheet'!");
        }
        if (addedSheets.contains(sheetAnno.value()))
            return;
        currentRow = 0;
        currentSheet = wb.createSheet(sheetAnno.value());
        addedSheets.add(sheetAnno.value());
        SXSSFRow row = currentSheet.createRow(currentRow++);
        Set<Field> fields = Serializer.getVisibleFields(type);
        currentColumns = new ArrayList<>();
        for (Field field: fields) {
            Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno != null) {
                currentColumns.add(columnAnno);
            }
        }

        Collections.sort(currentColumns, new ColumnComparator());
        int col = 0;
        for (Column column : currentColumns) {
            SXSSFCell cell = row.createCell(col);
            cell.setCellValue(column.value());
            cell.setCellStyle(headerStyle);
            if (column.width() >= 0) {
                int width = column.width() * 256;
                currentSheet.setColumnWidth(col, Math.min(width, 65280));
            }
            col++;
        }
    }

    public void addRow(Map<String, String> rowContent, Map<String, String> errors) {
        if (rowContent == null)
            return;
        SXSSFRow row = currentSheet.createRow(currentRow++);
        int col = 0;
        for (Column column : currentColumns) {
            SXSSFCell cell = row.createCell(col++);
            String value = rowContent.get(column.value());
            if (errors != null) {
                String error = errors.get(column.value());
                if (error != null) {
                    if (Strings.isNullOrEmpty(value))
                        value = "(ERR: " + error + ")";
                    else
                        value += " (ERR: " + error + ")";
                    cell.setCellStyle(errorStyle);
                } else
                    cell.setCellStyle(cellStyle);
            } else
                cell.setCellStyle(cellStyle);

            cell.setCellValue(value);
        }
    }

    public void addErrorRow(String message) {
        if (message == null)
            return;
        SXSSFRow row = currentSheet.createRow(currentRow++);
        SXSSFCell cell = row.createCell(0);
        cell.setCellStyle(errorStyle);
        cell.setCellValue(message);
    }

    public void save() throws IOException {
        try (FileOutputStream out = new FileOutputStream(targetFile)) {
            wb.write(out);
        }
    }

    @Override
    public void close() throws IOException {
        wb.dispose();
    }
}
