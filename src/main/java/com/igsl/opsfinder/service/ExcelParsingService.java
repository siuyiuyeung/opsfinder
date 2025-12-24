package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.excel.ExcelFileData;
import com.igsl.opsfinder.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing Excel files using Apache POI.
 * Extracts sheets, headers, and cell values from .xlsx files.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelParsingService {

    private static final int MAX_CELLS_PER_FILE = 100_000;
    private static final int MAX_SHEETS_PER_FILE = 50;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##########");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Parse an Excel file and extract all data.
     *
     * @param file the uploaded Excel file
     * @return parsed Excel file data
     * @throws BadRequestException if file is invalid or exceeds limits
     */
    public ExcelFileData parseExcelFile(MultipartFile file) {
        log.info("Starting to parse Excel file: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            int numberOfSheets = workbook.getNumberOfSheets();
            if (numberOfSheets > MAX_SHEETS_PER_FILE) {
                throw new BadRequestException(
                        String.format("Excel file contains too many sheets: %d (max: %d)",
                                numberOfSheets, MAX_SHEETS_PER_FILE));
            }

            ExcelFileData fileData = ExcelFileData.builder()
                    .originalFilename(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .sheets(new ArrayList<>())
                    .build();

            // Parse each sheet
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                ExcelFileData.SheetData sheetData = parseSheet(sheet, i);
                fileData.getSheets().add(sheetData);
            }

            // Validate total cell count
            long totalCells = fileData.getTotalCellCount();
            if (totalCells > MAX_CELLS_PER_FILE) {
                throw new BadRequestException(
                        String.format("Excel file contains too many cells: %d (max: %d)",
                                totalCells, MAX_CELLS_PER_FILE));
            }

            log.info("Successfully parsed Excel file: {} sheets, {} total rows, {} total cells",
                    fileData.getSheets().size(),
                    fileData.getTotalRowCount(),
                    totalCells);

            return fileData;

        } catch (IOException e) {
            log.error("Failed to parse Excel file: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    /**
     * Parse a single sheet from the workbook.
     *
     * @param sheet the sheet to parse
     * @param sheetIndex the 0-based index of the sheet
     * @return parsed sheet data
     */
    private ExcelFileData.SheetData parseSheet(Sheet sheet, int sheetIndex) {
        String sheetName = sheet.getSheetName();
        log.debug("Parsing sheet: {} (index: {})", sheetName, sheetIndex);

        ExcelFileData.SheetData sheetData = ExcelFileData.SheetData.builder()
                .sheetName(sheetName)
                .sheetIndex(sheetIndex)
                .headers(new ArrayList<>())
                .rows(new ArrayList<>())
                .build();

        int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum < 0) {
            log.debug("Sheet {} is empty", sheetName);
            return sheetData;
        }

        // First row is the header
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            sheetData.setHeaders(extractHeaders(headerRow));
        }

        // Parse data rows (starting from row 1)
        for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                continue; // Skip empty rows
            }

            ExcelFileData.RowData rowData = parseRow(row, rowNum, sheetData.getHeaders().size());
            if (!rowData.getCellValues().isEmpty()) {
                sheetData.getRows().add(rowData);
            }
        }

        log.debug("Parsed sheet {}: {} headers, {} data rows",
                sheetName, sheetData.getHeaders().size(), sheetData.getRows().size());

        return sheetData;
    }

    /**
     * Extract header values from the first row.
     *
     * @param headerRow the header row
     * @return list of header names
     */
    private List<String> extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        int lastCellNum = headerRow.getLastCellNum();

        for (int cellNum = 0; cellNum < lastCellNum; cellNum++) {
            Cell cell = headerRow.getCell(cellNum);
            String headerValue = getCellValueAsString(cell);

            // Use column index as header if cell is blank
            if (headerValue.isEmpty()) {
                headerValue = "Column_" + (cellNum + 1);
            }

            headers.add(headerValue);
        }

        return headers;
    }

    /**
     * Parse a single data row.
     *
     * @param row the row to parse
     * @param rowNumber the 1-based row number (excluding header)
     * @param headerCount number of headers (expected column count)
     * @return parsed row data
     */
    private ExcelFileData.RowData parseRow(Row row, int rowNumber, int headerCount) {
        List<String> cellValues = new ArrayList<>();
        int lastCellNum = Math.max(row.getLastCellNum(), headerCount);

        boolean hasNonEmptyCell = false;

        for (int cellNum = 0; cellNum < lastCellNum; cellNum++) {
            Cell cell = row.getCell(cellNum);
            String cellValue = getCellValueAsString(cell);
            cellValues.add(cellValue);

            if (!cellValue.isEmpty()) {
                hasNonEmptyCell = true;
            }
        }

        // Only return row if it has at least one non-empty cell
        if (!hasNonEmptyCell) {
            cellValues.clear();
        }

        return ExcelFileData.RowData.builder()
                .rowNumber(rowNumber)
                .cellValues(cellValues)
                .build();
    }

    /**
     * Convert cell value to string based on cell type.
     * Handles NUMERIC, STRING, BOOLEAN, FORMULA, BLANK, and ERROR types.
     *
     * @param cell the cell to convert
     * @return string representation of cell value
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();

                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return formatDateCell(cell);
                    } else {
                        return formatNumericCell(cell);
                    }

                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());

                case FORMULA:
                    return evaluateFormulaCell(cell);

                case BLANK:
                    return "";

                case ERROR:
                    return "ERROR";

                default:
                    return "";
            }
        } catch (Exception e) {
            log.warn("Error reading cell value at row {}, column {}: {}",
                    cell.getRowIndex(), cell.getColumnIndex(), e.getMessage());
            return "";
        }
    }

    /**
     * Format a date cell as string.
     *
     * @param cell the date cell
     * @return formatted date string
     */
    private String formatDateCell(Cell cell) {
        try {
            LocalDateTime dateTime = cell.getLocalDateTimeCellValue();
            if (dateTime == null) {
                return "";
            }

            // If time component is midnight, format as date only
            if (dateTime.getHour() == 0 && dateTime.getMinute() == 0 && dateTime.getSecond() == 0) {
                return dateTime.format(DATE_FORMATTER);
            } else {
                return dateTime.format(DATE_TIME_FORMATTER);
            }
        } catch (Exception e) {
            // Fallback to numeric value
            return formatNumericCell(cell);
        }
    }

    /**
     * Format a numeric cell as string.
     *
     * @param cell the numeric cell
     * @return formatted numeric string
     */
    private String formatNumericCell(Cell cell) {
        double numericValue = cell.getNumericCellValue();

        // If it's a whole number, format without decimals
        if (numericValue == Math.floor(numericValue)) {
            return String.valueOf((long) numericValue);
        } else {
            return DECIMAL_FORMAT.format(numericValue);
        }
    }

    /**
     * Evaluate a formula cell and return its calculated value.
     *
     * @param cell the formula cell
     * @return evaluated formula result as string
     */
    private String evaluateFormulaCell(Cell cell) {
        try {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            CellValue cellValue = evaluator.evaluate(cell);

            switch (cellValue.getCellType()) {
                case STRING:
                    return cellValue.getStringValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return formatDateCell(cell);
                    } else {
                        double numValue = cellValue.getNumberValue();
                        if (numValue == Math.floor(numValue)) {
                            return String.valueOf((long) numValue);
                        } else {
                            return DECIMAL_FORMAT.format(numValue);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cellValue.getBooleanValue());
                default:
                    return "";
            }
        } catch (Exception e) {
            log.warn("Failed to evaluate formula in cell at row {}, column {}: {}",
                    cell.getRowIndex(), cell.getColumnIndex(), e.getMessage());
            return "";
        }
    }
}
