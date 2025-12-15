# Task: Fix Duplicate ORDER BY Clause in Full-Text Search Query

## Analysis

**Problem**: SQL syntax error due to duplicate ORDER BY clauses in the searchDevices query.

**Root Cause**:
1. `DeviceRepository.searchDevices()` has a native SQL query with explicit `ORDER BY ts_rank(...) DESC` (line 30)
2. `DeviceController.searchDevices()` creates a Pageable with `Sort.by(direction, sortParams[0])` defaulting to `"id,desc"` (line 55)
3. Spring Data JPA attempts to append the Pageable's sort to the native query
4. Result: Invalid SQL with two ORDER BY clauses

**Error SQL**:
```sql
SELECT d.* FROM devices d
WHERE d.search_vector @@ plainto_tsquery('english', ?)
ORDER BY ts_rank(d.search_vector, plainto_tsquery('english', ?)) DESC
order by d.id desc  -- ❌ Second ORDER BY from Pageable
fetch first ? rows only
```

**Files Affected**:
- `src/main/java/com/igsl/opsfinder/controller/DeviceController.java:42-59`
- `src/main/java/com/igsl/opsfinder/repository/DeviceRepository.java:27-32`

**Solution Strategy**:
For full-text search, relevance ranking (ts_rank) should take priority over arbitrary sorting. The fix is to:
1. Remove the sort parameter from the search endpoint (it doesn't make sense for relevance-based search)
2. Create a Pageable without Sort (using `PageRequest.of(page, size)`) in the controller
3. Optionally add a countQuery to the repository for better performance

## Todo List
- [ ] Update `DeviceController.searchDevices()` to remove sort parameter and create unsorted Pageable
- [ ] Add countQuery to `DeviceRepository.searchDevices()` for optimized pagination
- [ ] Test the search endpoint to verify fix
- [ ] Update API documentation (if any) to reflect removed sort parameter

## Implementation Details

### Change 1: DeviceController.java (lines 42-59)
**Before**:
```java
@GetMapping("/search")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<Page<DeviceResponse>> searchDevices(
        @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id,desc") String sort) {  // ❌ Remove this

    log.info("Search devices request - term: {}, page: {}, size: {}", q, page, size);

    String[] sortParams = sort.split(",");  // ❌ Remove this
    Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC : Sort.Direction.DESC;  // ❌ Remove this
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));  // ❌ Has sort

    Page<DeviceResponse> devices = deviceService.searchDevices(q, pageable);
    return ResponseEntity.ok(devices);
}
```

**After**:
```java
@GetMapping("/search")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<Page<DeviceResponse>> searchDevices(
        @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {  // ✅ No sort parameter

    log.info("Search devices request - term: {}, page: {}, size: {}", q, page, size);

    // For full-text search, results are ordered by relevance (ts_rank) - no custom sort needed
    Pageable pageable = PageRequest.of(page, size);  // ✅ No sort

    Page<DeviceResponse> devices = deviceService.searchDevices(q, pageable);
    return ResponseEntity.ok(devices);
}
```

### Change 2: DeviceRepository.java (lines 27-32)
**Before**:
```java
@Query(value = """
        SELECT d.* FROM devices d
        WHERE d.search_vector @@ plainto_tsquery('english', :searchTerm)
        ORDER BY ts_rank(d.search_vector, plainto_tsquery('english', :searchTerm)) DESC
        """, nativeQuery = true)
Page<Device> searchDevices(@Param("searchTerm") String searchTerm, Pageable pageable);
```

**After**:
```java
@Query(value = """
        SELECT d.* FROM devices d
        WHERE d.search_vector @@ plainto_tsquery('english', :searchTerm)
        ORDER BY ts_rank(d.search_vector, plainto_tsquery('english', :searchTerm)) DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM devices d
        WHERE d.search_vector @@ plainto_tsquery('english', :searchTerm)
        """,
        nativeQuery = true)
Page<Device> searchDevices(@Param("searchTerm") String searchTerm, Pageable pageable);
```

## Review

**Implementation Completed**: ✅

### Changes Made

**1. DeviceController.java (lines 32-55)**
- Removed `@RequestParam(defaultValue = "id,desc") String sort` parameter
- Removed sort parsing logic (lines 52-54)
- Changed to `PageRequest.of(page, size)` without Sort object
- Updated JavaDoc to reflect that results are ordered by relevance (ts_rank)

**Benefit**: Eliminates the duplicate ORDER BY clause since no Sort is applied to the Pageable

**2. DeviceRepository.java (lines 27-37)**
- Added `countQuery` parameter to @Query annotation
- Count query mirrors the WHERE clause without ORDER BY
- Improves performance by optimizing the count operation for pagination

**Benefit**: Spring Data JPA now uses explicit count query instead of SELECT COUNT(*) over entire result set

### Why This Fixes the Issue

**Before**:
- Controller creates Pageable with `Sort.by(direction, "id")`
- Repository's @Query has `ORDER BY ts_rank(...) DESC`
- Spring Data JPA appends the sort to native query → Two ORDER BY clauses → SQL ERROR

**After**:
- Controller creates Pageable without any Sort
- Repository's @Query has `ORDER BY ts_rank(...) DESC`
- Spring Data JPA has no sort to append → Clean single ORDER BY clause → ✅ Works

### Testing Plan
1. Run `./gradlew clean build`
2. Start the application: `./gradlew bootRun`
3. Make a search request: `GET /api/devices/search?q=test&page=0&size=20`
4. Verify no SQL syntax error appears
5. Verify results are ordered by relevance (ts_rank DESC)
6. Verify pagination works correctly (try page=1, size=10, etc.)

### API Changes
- **Removed**: `sort` query parameter from `/api/devices/search` endpoint
- **Impact**: Clients can no longer customize sort order for search
- **Rationale**: Full-text search results should always be ordered by relevance (ts_rank) for best UX
