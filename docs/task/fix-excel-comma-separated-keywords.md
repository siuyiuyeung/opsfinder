# Task: Fix Excel Comma-Separated Keywords Search

## Analysis

After reading the codebase, I've identified the root cause of why comma-separated keywords "seem not working" in the Excel-files page.

### Current Behavior

The current implementation searches for **all keywords within the SAME cell**. For example:

- User searches: `apple,fruit`
- Current logic: Find cells where cell_value contains BOTH "apple" AND "fruit"
- Example match: Cell with value "I like apple and fruit" ✓
- Example NO match: Row with cells ["apple", "fruit", "red"] ✗

**Code Flow:**
1. Frontend (ExcelFileView.vue:153): Sends `searchKeywords.value.trim()` as single string
2. Backend Controller (ExcelFileController.java:132): Receives as `@RequestParam String keywords`
3. Backend Service (ExcelSearchService.java:111): Splits by comma: `keywordsString.split(",")`
4. Backend Repository (ExcelCellRepository.java:60-64): Searches with AND logic **within each cell**:
   ```sql
   WHERE cell_value_lower LIKE '%apple%'
   AND cell_value_lower LIKE '%fruit%'
   ```

### Expected Behavior (User Intent)

Users likely expect to find **ROWS where different keywords appear in different cells**. For example:

- User searches: `apple,fruit`
- Expected: Find rows where ANY cell contains "apple" AND ANY cell contains "fruit"
- Example match: Row with cells ["apple", "banana", "fruit"] ✓

### Root Cause

The SQL query in `ExcelCellRepository.searchWithMultipleKeywords()` applies AND logic to **individual cells**, not **rows**. This severely limits the usefulness of multi-keyword search because:

1. Most spreadsheet cells contain single values, not sentences with multiple keywords
2. Users naturally expect row-level search when searching structured data
3. The UI hint "comma-separated for AND logic" implies row-level search, not cell-level

### Evidence

**ExcelCellRepository.java lines 60-64:**
```java
"AND (:keyword1 IS NULL OR LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword1 AS TEXT), '%'))) " +
"AND (:keyword2 IS NULL OR LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword2 AS TEXT), '%'))) " +
```

This checks if **the same** `ec.cell_value_lower` contains all keywords.

## Solution Options

### Option 1: Row-Level Search (Recommended)

Change the search logic to find rows where **all keywords appear across different cells** in the same row.

**Pros:**
- More intuitive for spreadsheet data
- Higher match rates for multi-keyword searches
- Aligns with user expectations

**Cons:**
- More complex SQL query
- Slightly slower performance (but still fast with proper indexing)

**SQL Approach:**
```sql
SELECT DISTINCT ec.*
FROM excel_cells ec
WHERE ec.row_number IN (
    SELECT sub.row_number
    FROM excel_cells sub
    WHERE sub.excel_sheet_id = ec.excel_sheet_id
    AND (
        LOWER(sub.cell_value_lower) LIKE '%keyword1%' OR
        LOWER(sub.cell_value_lower) LIKE '%keyword2%'
    )
    GROUP BY sub.row_number, sub.excel_sheet_id
    HAVING COUNT(DISTINCT CASE WHEN LOWER(sub.cell_value_lower) LIKE '%keyword1%' THEN 1 END) > 0
       AND COUNT(DISTINCT CASE WHEN LOWER(sub.cell_value_lower) LIKE '%keyword2%' THEN 1 END) > 0
)
```

### Option 2: Add Search Mode Toggle

Add a toggle in UI to switch between "Search within cell" vs "Search across row".

**Pros:**
- Gives users both options
- Maintains backward compatibility

**Cons:**
- More complex UI and implementation
- May confuse users

### Option 3: Keep Current Behavior, Improve Documentation

Just update the UI hints to clarify current behavior.

**Pros:**
- No code changes needed
- Simplest solution

**Cons:**
- Doesn't address user needs
- Limits search usefulness

## Recommendation

Implement **Option 1: Row-Level Search** because:
1. It matches user expectations for spreadsheet search
2. Single-cell multi-keyword matches are rare in structured data
3. Row-level search is more powerful and useful

## Todo List

- [ ] Update ExcelCellRepository queries to implement row-level multi-keyword search
- [ ] Test the new query logic with sample data
- [ ] Update ExcelSearchService to handle row-level results correctly
- [ ] Update UI hints to clarify "search across cells in the same row"
- [ ] Test with various keyword combinations
- [ ] Verify performance with large datasets

## Review

### Implementation Summary

Successfully implemented **row-level multi-keyword search with dynamic keyword support**.

### Changes Made

**1. Created Custom Repository System**
- `ExcelCellRepositoryCustom.java` - Interface for dynamic keyword search
- `ExcelCellRepositoryCustomImpl.java` - Implementation with dynamic SQL generation
- Updated `ExcelCellRepository` to extend the custom interface

**2. Dynamic SQL Generation**
- Builds SQL queries dynamically based on actual keyword count
- No fixed limit on parameters (increased from 5 to 10 keywords max)
- Optimized single-keyword path for performance
- Multi-keyword uses row-level AND logic

**3. Updated Service Layer**
- `ExcelSearchService.java` - Simplified to use single dynamic method
- Removed fixed 5-parameter method calls
- Increased MAX_KEYWORDS from 5 to 10

**4. Updated Documentation**
- Controller, Service, DTO, and Repository javadocs clarify row-level search
- Frontend UI hints updated to explain "row-level AND" behavior

**5. Updated Frontend**
- `ExcelFileView.vue` - Updated label and hint text to clarify row-level search

### Key Improvements

1. **Row-Level Search**: Keywords now match across different cells in the same row (not within single cells)
2. **Dynamic Keywords**: Supports any number of keywords (1-10) without code changes
3. **Better UX**: Clear hints explain search behavior
4. **Cleaner Code**: No more keyword1, keyword2, keyword3, etc. parameters
5. **Maintainable**: Easy to adjust keyword limit by changing MAX_KEYWORDS constant

### How It Works

**Single Keyword Example**: `apple`
- Finds all cells containing "apple"

**Multiple Keywords Example**: `apple,fruit,red`
- Finds rows where:
  - Cell A contains "apple" AND
  - Cell B contains "fruit" AND
  - Cell C contains "red"
- Returns all matching cells from those rows

### SQL Query Logic

1. First filter: Cell must contain at least one keyword (OR logic)
2. Second filter: Cell must be in a row where ALL keywords appear (AND logic at row level)
3. Uses subquery with GROUP BY and HAVING to ensure all keywords present in row
4. Results ordered by sheet, row, and column for coherent display

### Performance Considerations

- Indexed on `cell_value_lower` for fast keyword matching
- Subquery uses HAVING with SUM(CASE) for efficient row-level validation
- Pagination support to handle large result sets
- Dynamic query building has negligible overhead vs. fixed queries

### Testing Status

- Code compiles successfully
- Ready for runtime testing with actual Excel data
- Should test with: 1 keyword, 2 keywords, 5 keywords, 10 keywords
- Should verify performance with large datasets (100K+ cells)
