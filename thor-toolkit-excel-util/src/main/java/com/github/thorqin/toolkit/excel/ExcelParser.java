package com.github.thorqin.toolkit.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

import com.github.thorqin.toolkit.excel.annotation.Column;
import com.github.thorqin.toolkit.excel.annotation.Sheet;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.utility.Serializer;
import com.google.common.base.Strings;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by thor on 8/3/16.
 */
public class ExcelParser {

    public static class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }

    public static class Stat {
        public String sheetName;
        public int successCount = 0;
        public int failedCount = 0;
        public int ignoreCount = 0;
        public String fatalError = null;
    }

    public interface ProcessHandler {
        void onSheetBegin(Class<?> sheetType, String sheetName);
        void onSheetEnd(Class<?> sheetType, String sheetName);
        void onSheetError(Class<?> sheetType, String sheetName, String errorMessage);
        void onRow(Object item, Map<String, String> errors, Map<String, String> rawData);
    }

    private static class SheetContentsHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private int currentRow = -1;
        private int currentCol = -1;
        private Sheet sheetAnnotation;
        private ProcessHandler handler;
        private Class<?> type;
        private Map<Integer, String> rowData = new HashMap<>();
        private Map<String, Integer> fieldMapping;
        private Map<String, Field> allFields;
        private Localization loc;
        private int success = 0;
        private int failed = 0;
        private int ignore = 0;

        public int getSuccessCount() {
            return success;
        }
        public int getFailedCount() {
            return failed;
        }
        public int getIgnoreCount() {
            return ignore;
        }

        private SheetContentsHandler(Sheet sheetAnnotation, Class<?> type, ProcessHandler handler, Localization loc) {
            this.sheetAnnotation = sheetAnnotation;
            this.handler = handler;
            this.type = type;
            this.fieldMapping = new HashMap<>();
            this.loc = loc;
            this.allFields = getFieldMapping(type);
        }

        public static <T> Map<String, Field> getFieldMapping(Class<T> type) {
            Map<String, Field> result = new HashMap<>();
            Set<Field> fields = Serializer.getVisibleFields(type);
            for (Field field: fields) {
                Column anno = field.getAnnotation(Column.class);
                if (anno != null) {
                    result.put(anno.value(), field);
                }
            }
            return result;
        }

        public void startRow(int rowNum) {
            currentRow = rowNum;
            currentCol = -1;
            rowData.clear();
        }

        public void endRow(int rowNum) {
            if (sheetAnnotation.headerLine() == currentRow) {
                for (Map.Entry<Integer, String> col: rowData.entrySet()) {
                    String value = col.getValue();
                    Field field = allFields.get(value);
                    if (field != null) {
                        fieldMapping.put(value, col.getKey());
                    }
                }
                ignore++;
            } else if (currentRow > sheetAnnotation.headerLine()) {
                Map<String, String> errors = new HashMap<>();
                Map<String, String> rawData = new HashMap<>();
                Map<String, Object> validData = new HashMap<>();
                // In data row
                for (Map.Entry<String, Field> entry: allFields.entrySet()) {
                    Column columnAnno = entry.getValue().getAnnotation(Column.class);
                    Integer col = fieldMapping.get(entry.getKey());
                    String value;
                    if (col == null) {
                        value = null;
                    } else
                        value = rowData.get(col);
                    rawData.put(entry.getKey(), value);
                    if (Strings.isNullOrEmpty(value)) {
                        if (columnAnno.required()) {
                            errors.put(entry.getKey(), MessageConstant.COLUMN_IS_REQUIRED.getMessage(loc, columnAnno.value()));
                        } else {
                            StringBuilder sb = new StringBuilder();
                            boolean errorRaised = false;
                            for (String other: columnAnno.requiredBy()) {
                                Integer otherCol = fieldMapping.get(other);
                                String otherValue;
                                if (otherCol != null)
                                    otherValue = rowData.get(otherCol);
                                else
                                    otherValue = null;
                                if (!Strings.isNullOrEmpty(otherValue)) {
                                    if (errorRaised)
                                        sb.append(", ");
                                    sb.append(other);
                                    errorRaised = true;
                                }
                            }
                            if (errorRaised)
                                errors.put(entry.getKey(), MessageConstant.COLUMN_IS_REQUIRED_BY.getMessage(loc, columnAnno.value(), sb.toString()));
                        }
                    } else {
                        boolean invalid = false;
                        if (columnAnno.format().length > 0) {
                            invalid = true;
                            for (String format : columnAnno.format()) {
                                if (value.matches(format)) {
                                    invalid = false;
                                    break;
                                }
                            }
                        }
                        if (invalid) {
                            errors.put(entry.getKey(), MessageConstant.INVALID_VALUE_FORMAT.getMessage(loc, columnAnno.formatDescription()));
                        } else {
                            validData.put(entry.getValue().getName(), value);
                        }
                    }
                }

                Object item = Serializer.fromJson(Serializer.toJsonString(validData), type);
                if (handler != null) {
                    try {
                        handler.onRow(item, errors, rawData);
                        if (errors.isEmpty())
                            success++;
                        else
                            failed++;
                    } catch (Exception e) {
                        failed++;
                    }
                } else {
                    if (errors.isEmpty())
                        success++;
                    else
                        failed++;
                }
            } else
                ignore++;
        }

        public void cell(String cellReference, String formattedValue,
                         XSSFComment comment) {
            // gracefully handle missing CellRef here in a similar way as XSSFCell does
            if(cellReference == null) {
                cellReference = new CellAddress(currentRow, currentCol).formatAsString();
            }

            currentCol = (new CellReference(cellReference)).getCol();
            rowData.put(currentCol, formattedValue);
        }

        public void headerFooter(String text, boolean isHeader, String tagName) {
            // Skip
        }
    }

    public static List<Stat> parse(String file, Class<?>[] parseTypes) throws Exception {
        return parse(new File(file), parseTypes, null, null);
    }

    public static List<Stat> parse(String file, Class<?>[] parseTypes, ProcessHandler handler) throws Exception {
        return parse(new File(file), parseTypes, handler, null);
    }

    public static List<Stat> parse(String file, Class<?>[] parseTypes, Localization loc) throws Exception {
        return parse(new File(file), parseTypes, null, loc);
    }

    public static List<Stat> parse(String file, Class<?>[] parseTypes, ProcessHandler handler, Localization loc) throws Exception {
        return parse(new File(file), parseTypes, handler, loc);
    }

    public static List<Stat> parse(File file, Class<?>[] parseTypes) throws Exception {
        return parse(file, parseTypes, null, null);
    }

    public static List<Stat> parse(File file, Class<?>[] parseTypes, ProcessHandler handler) throws Exception {
        return parse(file, parseTypes, handler, null);
    }

    public static List<Stat> parse(File file, Class<?>[] parseTypes, Localization loc) throws Exception {
        return parse(file, parseTypes, null, loc);
    }

    public static List<Stat> parse(File file, Class<?>[] parseTypes, ProcessHandler handler, Localization loc) throws Exception {
        if (loc == null)
            loc = Localization.getInstance();
        List<Stat> stats = new LinkedList<>();
        if (parseTypes == null || parseTypes.length == 0)
            throw new ParseException(MessageConstant.EMPTY_DATA_MODEL.getMessage(loc));
        try (OPCPackage pkg = OPCPackage.open(file)) {
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader xssfReader = new XSSFReader(pkg);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator iterator = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

            Map<String, InputStream> sheets = new HashMap<>();
            while (iterator.hasNext()) {
                InputStream stream = iterator.next();
                sheets.put(iterator.getSheetName(), stream);
            }

            boolean errorRaised = false;
            StringBuilder sb = new StringBuilder();
            List<SheetInfo> sheetList = new ArrayList<>(sheets.size());
            for (Class<?> type: parseTypes) {
                Sheet sheetAnno = type.getAnnotation(Sheet.class);
                if (sheets.containsKey(sheetAnno.value())) {
                    InputStream sheetStream = sheets.get(sheetAnno.value());
                    SheetInfo info = new SheetInfo();
                    info.annotation = sheetAnno;
                    info.type = type;
                    info.stream = sheetStream;
                    sheetList.add(info);
                } else {
                    if (sheetAnno.required()) {
                        if (errorRaised)
                            sb.append(", ");
                        sb.append(MessageConstant.SHEET_IS_REQUIRED.getMessage(loc, sheetAnno.value()));
                        errorRaised = true;
                    } else {
                        for (String other: sheetAnno.requiredBy()) {
                            if (sheets.containsKey(other)) {
                                if (errorRaised)
                                    sb.append(", ");
                                sb.append(MessageConstant.SHEET_IS_REQUIRED_BY.getMessage(loc, sheetAnno.value(), other));
                                errorRaised = true;
                            }
                        }
                    }
                }
            }
            if (errorRaised)
                throw new ParseException(sb.toString());

            Collections.sort(sheetList, new SheetComparator());
            for (SheetInfo sheetInfo: sheetList) {
                SheetContentsHandler contentsHandler = new SheetContentsHandler(sheetInfo.annotation, sheetInfo.type, handler, loc);
                Stat stat = new Stat();
                stat.sheetName = sheetInfo.annotation.value();
                try {
                    if (handler != null) {
                        handler.onSheetBegin(sheetInfo.type, sheetInfo.annotation.value());
                    }
                    processSheet(styles, strings, contentsHandler, sheetInfo.stream);
                    if (handler != null) {
                        handler.onSheetEnd(sheetInfo.type, sheetInfo.annotation.value());
                    }
                } catch (Exception e) {
                    stat.fatalError = e.getMessage();
                    if (handler != null) {
                        handler.onSheetError(sheetInfo.type, sheetInfo.annotation.value(), e.getMessage());
                    }
                } finally {
                    stat.successCount = contentsHandler.getSuccessCount();
                    stat.failedCount = contentsHandler.getFailedCount();
                    stat.ignoreCount = contentsHandler.getIgnoreCount();
                    stats.add(stat);
                    sheetInfo.stream.close();
                }
            }
        }
        return stats;
    }

    private static class SheetInfo {
        public Sheet annotation;
        public Class<?> type;
        public InputStream stream;
    }

    private static class SheetComparator implements Comparator<SheetInfo> {
        @Override
        public int compare(SheetInfo o1, SheetInfo o2) {
            return Integer.compare(o1.annotation.order(), o2.annotation.order());
        }
    }

    private static void processSheet(
            StylesTable styles,
            ReadOnlySharedStringsTable strings,
            SheetContentsHandler sheetHandler,
            InputStream sheetInputStream)
            throws IOException, ParserConfigurationException, SAXException {
        DataFormatter formatter = new DataFormatter();
        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            XMLReader sheetParser = SAXHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(
                    styles, null, strings, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch(ParserConfigurationException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
        }
    }
}
