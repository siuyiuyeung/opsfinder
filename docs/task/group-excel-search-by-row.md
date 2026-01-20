# Task: Group Excel Search Results by Row

## Analysis

**Current Behavior**: When searching Excel files, each matched cell returns as a separate `ExcelSearchResultResponse`. If 3 cells in row 5 match the search keywords, the user sees 3 separate entries in the results table.

**Desired Behavior**: Results should be grouped by row. If multiple cells in the same row match, they should appear as a single result with all matched cells highlighted.

### Current Data Flow
1. `ExcelCellRepositoryCustomImpl` returns individual `ExcelCell` entities
2. `ExcelFileService.mapToSearchResult()` maps each cell to an `ExcelSearchResultResponse`
3. Frontend displays each response as a separate table row

### Key Files
- **Backend DTO**: `src/main/java/com/igsl/opsfinder/dto/ExcelSearchResultResponse.java`
- **Backend Service**: `src/main/java/com/igsl/opsfinder/service/ExcelFileService.java` (lines 184-222)
- **Frontend Types**: `src/views/types/excel.ts`
- **Frontend View**: `src/views/ExcelFileView.vue`

## Proposed Solution

Create a new row-based response structure and group matched cells at the service layer.

### Changes Required

**Backend**:
1. Create new DTO `ExcelRowSearchResult` that represents a row with multiple matched cells
2. Modify `ExcelFileService.searchExcelData()` to group results by (fileId, sheetId, rowNumber)
3. Track which cells matched (multiple `isMatchedCell` flags in `rowData`)

**Frontend**:
1. Update TypeScript types to match new response structure
2. Adjust table display to show row-based results
3. Highlight all matched cells in the expanded row view

## Todo List

- [ ] Create new DTO `ExcelRowSearchResult.java` with row-level grouping structure
- [ ] Modify `ExcelFileService.searchExcelData()` to group cells by row
- [ ] Update `RowCellData` to properly mark multiple matched cells per row
- [ ] Update frontend TypeScript types in `excel.ts`
- [ ] Update `ExcelFileView.vue` to display row-grouped results
- [ ] Test with multi-keyword searches that match multiple cells in same row

## New Data Structures

### Backend: ExcelRowSearchResult.java
```java
public class ExcelRowSearchResult {
    private Long fileId;
    private String fileName;
    private Long sheetId;
    private String sheetName;
    private Integer rowNumber;
    private List<String> matchedValues;      // Values that matched the search
    private List<Integer> matchedColumnIndices;  // Columns where matches occurred
    private List<RowCellData> rowData;       // Full row with isMatchedCell flags
}
```

### Grouping Logic (in ExcelFileService)
```java
// Group cells by composite key: fileId + sheetId + rowNumber
Map<String, List<ExcelCell>> groupedByRow = cells.stream()
    .collect(Collectors.groupingBy(cell ->
        cell.getExcelSheet().getExcelFile().getId() + "_" +
        cell.getExcelSheet().getId() + "_" +
        cell.getRowNumber()
    ));
```

## Review
[To be filled after completion]
