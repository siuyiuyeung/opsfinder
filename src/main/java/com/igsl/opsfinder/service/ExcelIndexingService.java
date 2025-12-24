package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.excel.ExcelFileData;
import com.igsl.opsfinder.entity.ExcelCell;
import com.igsl.opsfinder.entity.ExcelFile;
import com.igsl.opsfinder.entity.ExcelSheet;
import com.igsl.opsfinder.repository.ExcelCellRepository;
import com.igsl.opsfinder.repository.ExcelFileRepository;
import com.igsl.opsfinder.repository.ExcelSheetRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for indexing parsed Excel data into the database.
 * Handles entity creation and bulk insertion with batch processing for performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelIndexingService {

    private static final int BATCH_SIZE = 500;

    private final ExcelFileRepository excelFileRepository;
    private final ExcelSheetRepository excelSheetRepository;
    private final ExcelCellRepository excelCellRepository;
    private final EntityManager entityManager;

    /**
     * Index parsed Excel file data into the database.
     * Creates ExcelFile, ExcelSheet, and ExcelCell entities with their relationships.
     *
     * @param fileData the parsed Excel file data
     * @param filePath the stored file path on disk
     * @param uploadedBy the username who uploaded the file
     * @return persisted ExcelFile entity with all relationships
     */
    @Transactional
    public ExcelFile indexExcelFile(ExcelFileData fileData, String filePath, String uploadedBy) {
        log.info("Starting to index Excel file: {} (uploaded by: {})",
                fileData.getOriginalFilename(), uploadedBy);

        long startTime = System.currentTimeMillis();

        // Create ExcelFile entity
        ExcelFile excelFile = createExcelFileEntity(fileData, filePath, uploadedBy);
        excelFile = excelFileRepository.save(excelFile);
        log.debug("Saved ExcelFile entity with ID: {}", excelFile.getId());

        // Create ExcelSheet entities
        List<ExcelSheet> sheets = new ArrayList<>();
        for (ExcelFileData.SheetData sheetData : fileData.getSheets()) {
            ExcelSheet sheet = createExcelSheetEntity(sheetData, excelFile);
            sheet = excelSheetRepository.save(sheet);
            sheets.add(sheet);
            log.debug("Saved ExcelSheet entity: {} (ID: {})", sheet.getSheetName(), sheet.getId());
        }

        // Create ExcelCell entities in bulk
        int totalCellsIndexed = 0;
        for (int i = 0; i < sheets.size(); i++) {
            ExcelSheet sheet = sheets.get(i);
            ExcelFileData.SheetData sheetData = fileData.getSheets().get(i);

            List<ExcelCell> cells = createExcelCellEntities(sheetData, sheet);
            int cellsIndexed = bulkInsertCells(cells);
            totalCellsIndexed += cellsIndexed;

            log.debug("Indexed {} cells for sheet: {}", cellsIndexed, sheet.getSheetName());
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Successfully indexed Excel file: {} sheets, {} rows, {} cells in {}ms",
                excelFile.getSheetCount(),
                excelFile.getRowCount(),
                totalCellsIndexed,
                duration);

        return excelFile;
    }

    /**
     * Create ExcelFile entity from parsed data.
     *
     * @param fileData the parsed file data
     * @param filePath the stored file path
     * @param uploadedBy the uploader username
     * @return ExcelFile entity (not yet persisted)
     */
    private ExcelFile createExcelFileEntity(ExcelFileData fileData, String filePath, String uploadedBy) {
        String originalFilename = fileData.getOriginalFilename();
        String storedFilename = filePath.substring(filePath.lastIndexOf(java.io.File.separator) + 1);

        return ExcelFile.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .filePath(filePath)
                .fileSize(fileData.getFileSize())
                .uploadedBy(uploadedBy)
                .uploadedAt(LocalDateTime.now())
                .sheetCount(fileData.getSheets().size())
                .rowCount(fileData.getTotalRowCount())
                .cellCount(fileData.getTotalCellCount())
                .status(ExcelFile.Status.ACTIVE)
                .build();
    }

    /**
     * Create ExcelSheet entity from sheet data.
     *
     * @param sheetData the parsed sheet data
     * @param excelFile the parent Excel file
     * @return ExcelSheet entity (not yet persisted)
     */
    private ExcelSheet createExcelSheetEntity(ExcelFileData.SheetData sheetData, ExcelFile excelFile) {
        ExcelSheet sheet = ExcelSheet.builder()
                .excelFile(excelFile)
                .sheetName(sheetData.getSheetName())
                .sheetIndex(sheetData.getSheetIndex())
                .rowCount(sheetData.getRows().size())
                .columnCount(sheetData.getColumnCount())
                .build();

        // Set headers as comma-separated string
        sheet.setHeadersList(sheetData.getHeaders());

        return sheet;
    }

    /**
     * Create ExcelCell entities from sheet data.
     *
     * @param sheetData the parsed sheet data
     * @param excelSheet the parent Excel sheet
     * @return list of ExcelCell entities (not yet persisted)
     */
    private List<ExcelCell> createExcelCellEntities(ExcelFileData.SheetData sheetData, ExcelSheet excelSheet) {
        List<ExcelCell> cells = new ArrayList<>();
        List<String> headers = sheetData.getHeaders();

        for (ExcelFileData.RowData rowData : sheetData.getRows()) {
            int rowNumber = rowData.getRowNumber();
            List<String> cellValues = rowData.getCellValues();

            for (int columnIndex = 0; columnIndex < cellValues.size(); columnIndex++) {
                String cellValue = cellValues.get(columnIndex);

                // Only create cell if it has a value
                if (cellValue == null || cellValue.trim().isEmpty()) {
                    continue;
                }

                String columnHeader = columnIndex < headers.size()
                        ? headers.get(columnIndex)
                        : "Column_" + (columnIndex + 1);

                ExcelCell cell = ExcelCell.builder()
                        .excelSheet(excelSheet)
                        .rowNumber(rowNumber)
                        .columnIndex(columnIndex)
                        .columnHeader(columnHeader)
                        .cellValue(cellValue)
                        .build();

                cells.add(cell);
            }
        }

        return cells;
    }

    /**
     * Bulk insert cells using batch processing for performance.
     * Inserts cells in batches of BATCH_SIZE (500) to optimize database performance.
     *
     * @param cells the list of cells to insert
     * @return number of cells inserted
     */
    private int bulkInsertCells(List<ExcelCell> cells) {
        if (cells.isEmpty()) {
            return 0;
        }

        int totalCells = cells.size();
        int batchCount = 0;

        for (int i = 0; i < totalCells; i++) {
            entityManager.persist(cells.get(i));

            if ((i + 1) % BATCH_SIZE == 0 || (i + 1) == totalCells) {
                // Flush and clear every BATCH_SIZE or at the end
                entityManager.flush();
                entityManager.clear();
                batchCount++;
            }
        }

        log.debug("Inserted {} cells in {} batches", totalCells, batchCount);
        return totalCells;
    }
}
