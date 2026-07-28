# Task: Excel Search — Grouped Results with Full-Detail Toggle

## Analysis

`frontend/src/views/ExcelFileView.vue` renders Excel search results in a single
`v-data-table` with four columns (File, Sheet, Row, Matched Values) plus an
expand chevron. Expanding a row reveals the full row data as a *vertical* table
(`Column | Header | Value`, one line per cell).

Two problems with this:

1. Seeing full detail requires expanding each result one at a time.
2. The expanded detail reads top-to-bottom, which does not match how the data
   actually looks in Excel (a horizontal row under column headers).

File and Sheet are also repeated on every row even though results naturally
cluster by sheet.

The backend already returns everything needed — `ExcelRowSearchResult.rowData`
is a `RowCellData[]` carrying `columnHeader`, `columnIndex`, `cellValue`, and
`isMatchedCell` for the whole row. No backend or type changes required.

The view additionally carries leftover debug code: two `console.log` calls in
`handleSearch` and a debug `v-alert` dumping `JSON.stringify(item)` in the
expanded-row fallback branch.

## Design

**Toggle**: `showAllColumns` ref, surfaced as a `v-switch` labelled
"Show all columns" in the Search Results card title bar.

**Grouping** (applies to both toggle states): computed `groupedResults` groups
`searchResults` by `fileId_sheetId`:

```ts
{ key, fileName, sheetName, rows, columns: { columnIndex, columnHeader }[] }
```

`columns` is the union of `columnIndex` across the group's `rowData`, sorted
ascending. Grouping per sheet matters because different sheets have different
headers — each group therefore owns its column set.

**Render**: replace the single `v-data-table` with one section per group.

- Group header: `mdi-file-excel` icon + `fileName / sheetName` + `(N rows)`
- Table in an `overflow-x: auto` wrapper:
  - Toggle OFF → `Row | Matched Values` (chips, first 3 then "+N more")
  - Toggle ON → `Row | <one column per header>`, matched cells highlighted
    with `bg-yellow-lighten-4`
- Cell values truncated at 50 chars for display, full text in `title` attr

**Removed**, superseded by the toggle: expand chevron, `expandedRows`,
`getRowKey`, `searchResultHeaders`, the `expanded-row` template, plus the
leftover debug `console.log`s and debug `v-alert`.

**Trade-off**: plain grouped tables lose the data table's 10-per-page
pagination. Search caps at 100 results, so the worst case is 100 rows rendered
flat. Accepted — the alternative (collapsible groups) hides results by default.

## Todo List

- [x] Add `showAllColumns` ref and `groupedResults` computed
- [x] Add value-truncation helper
- [x] Replace search-results `v-data-table` with grouped sections + toggle
- [x] Remove `expandedRows`, `getRowKey`, `searchResultHeaders`, expanded-row template
- [x] Remove leftover debug `console.log`s and debug `v-alert`
- [x] Verify: `npm run type-check` and `npm run build`

## Review

Single file changed: `frontend/src/views/ExcelFileView.vue`. No backend, type,
or service changes — `ExcelRowSearchResult.rowData` already carried everything.

**Added**

- `GroupColumn` / `ResultGroup` local interfaces.
- `showAllColumns` ref, bound to a `v-switch` in the Search Results title bar.
  Defaults to `true` — the full-column view is the more useful landing state,
  and the switch is there to collapse back to matched values only.
- `groupedResults` computed: buckets results by `fileId_sheetId`, then builds
  each group's column set from the union of its rows' `columnIndex` values,
  sorted ascending. Sparse rows are handled by `cellAt()`, which returns
  `undefined` for a missing cell (rendered as an em dash).
- `truncate(value, max = 50)` helper, reused by both the chip view (max 30) and
  the full-column view.
- `.result-table-wrapper` style: `overflow-x: auto` with `white-space: nowrap`
  on cells, so a wide sheet scrolls inside its own box rather than stretching
  the page.

**Removed**

- Expand chevron and its `expanded-row` template — the toggle replaces it.
- `expandedRows` ref, `getRowKey()`, `searchResultHeaders`.
- Leftover debug code: two `console.log` calls in `handleSearch` and the
  `v-alert` that dumped `JSON.stringify(item)` on the no-rowData branch.

**Behaviour**

Both toggle states render grouped sections headed by
`fileName / sheetName (N rows)`. Toggle OFF shows `Row | Matched Values`
(chips, first 3 then "+N more"). Toggle ON shows `Row` plus one column per
sheet header, with matched cells highlighted `bg-yellow-lighten-4` and full
cell text available via the `title` attribute.

**Verification**

- `npm run build` — passes (`✓ built in 9.18s`).
- `npm run type-check` — the pre-existing error set is unchanged. Reported
  errors live in `vuetify.ts`, `DeviceListView.vue`, `TechMessageListView.vue`,
  `vite.config.ts`, and `ExcelFileView.vue:130` (`handleFileSelect`, untouched
  by this task). No error originates from the new code.
- Not exercised in a running browser — no local runtime environment available.

## Follow-up: two defects found in production testing

**1. Matched cells never highlighted (pre-existing backend bug)**

`RowCellData.isMatchedCell` is a primitive `boolean`, so Lombok generates the
getter `isMatchedCell()`. Jackson strips the `is` prefix when deriving a bean
property name, so the field serialized as `matchedCell`:

```json
{"columnHeader":"SERVICE","columnIndex":4,"cellValue":"Email Agent","matchedCell":true}
```

The frontend reads `isMatchedCell`, which was therefore always `undefined` —
no cell ever highlighted. This predates the grouped view; the old expand-row
table had the same bug, it was just less visible.

Fixed at the source with `@JsonProperty("isMatchedCell")` on the field, which
matches what both `frontend/src/types/excel.ts` and `docs/openapi.yaml`
already specify. Guarded by
`src/test/java/com/igsl/opsfinder/dto/excel/RowCellDataSerializationTest.java`,
which fails without the annotation.

A grep of `src/main/java/com/igsl/opsfinder/dto` confirms `RowCellData` is the
only DTO with a `private boolean isXxx` field, so no other endpoint is
affected.

**2. Duplicate info icon**

`v-alert type="info"` renders its own leading icon, and the template added a
second `<v-icon icon="mdi-information">` inside it. Removed the explicit one.

**Verification of follow-up**

- `./gradlew test --tests "*RowCellDataSerializationTest"` — fails before the
  annotation, passes after.
- `npm run build` — passes (`✓ built in 6.60s`).
- Compile warnings are pre-existing (`AuthResponse` `@Builder.Default`,
  `ExcelFileMapper` unmapped `rowData`), unrelated to this change.
