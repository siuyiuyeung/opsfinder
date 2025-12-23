# Task: CSV Import/Export Frontend Implementation

## Analysis

After analyzing the frontend codebase, I understand the following:

**Frontend Stack**:
- Vue 3 with TypeScript and Composition API
- Vuetify for UI components
- Axios for HTTP requests with JWT token interceptors
- Pinia for state management

**Existing Patterns**:
- Service layer pattern: `deviceService` exports API methods
- Views use Vuetify components (v-card, v-dialog, v-btn, etc.)
- Role-based UI rendering using `authStore.isAdmin` and `authStore.isOperator`
- Error handling with try-catch and console.error
- Loading states tracked in Pinia store

**Backend API Endpoints** (already implemented):
- `POST /api/devices/import` - Upload CSV file (ADMIN/OPERATOR only)
- `GET /api/devices/export?zone=&type=` - Download CSV file (authenticated users)

**Requirements**:
1. Add export functionality with optional zone/type filters (all authenticated users)
2. Add import functionality with file upload (ADMIN/OPERATOR only)
3. Display detailed import results (success/failure rows)
4. Proper error handling and loading states
5. Follow existing UI/UX patterns from DeviceListView

## Todo List

### 1. TypeScript Types
- [ ] Add `DeviceImportResult` interface to `types/device.ts`
- [ ] Add `RowResult` interface to `types/device.ts`

### 2. Service Layer
- [ ] Add `importDevices(file)` method to `device.service.ts`
- [ ] Add `exportDevices(zone?, type?)` method to `device.service.ts`

### 3. UI Components (DeviceListView.vue)
- [ ] Add "Export CSV" button next to "Add Device" button
- [ ] Add "Import CSV" button next to "Export CSV" (ADMIN/OPERATOR only)
- [ ] Create import dialog with file upload component
- [ ] Create import results dialog with success/failure details
- [ ] Add loading states for import/export operations
- [ ] Add error handling with user-friendly messages

### 4. Import Dialog Features
- [ ] File input field with accept=".csv"
- [ ] Upload button with loading state
- [ ] Cancel button
- [ ] File validation (CSV only, max 10MB client-side check)

### 5. Export Functionality
- [ ] Export button triggers download with current filters
- [ ] Proper filename generation (devices_zone_type_date.csv)
- [ ] Loading indicator during export
- [ ] Error handling for export failures

### 6. Import Results Display
- [ ] Show total rows, success count, failure count
- [ ] Display table with row-by-row results
- [ ] Show error messages for failed rows
- [ ] Color coding (success: green, failure: red)
- [ ] Close button to dismiss dialog

### 7. Testing
- [ ] Test export with no filters
- [ ] Test export with zone filter
- [ ] Test export with type filter
- [ ] Test export with both filters
- [ ] Test import with valid CSV
- [ ] Test import with invalid CSV
- [ ] Test import with partially valid CSV
- [ ] Test authorization (import only for ADMIN/OPERATOR)
- [ ] Test file size validation
- [ ] Test CSV file type validation

## Implementation Details

### TypeScript Interfaces
```typescript
export interface RowResult {
  rowNumber: number
  success: boolean
  errorMessage?: string
  deviceData?: any
}

export interface DeviceImportResult {
  totalRows: number
  successCount: number
  failureCount: number
  results: RowResult[]
}
```

### Service Methods
```typescript
// Import devices from CSV file
async importDevices(file: File): Promise<DeviceImportResult>

// Export devices to CSV file
async exportDevices(zone?: string, type?: string): Promise<Blob>
```

### UI Layout Changes
- Add buttons in card title section next to "Add Device"
- Use v-menu or separate buttons for import/export actions
- Import dialog: v-dialog with v-file-input component
- Results dialog: v-dialog with v-data-table showing results

### Error Handling
- Network errors: Display toast/snackbar with error message
- File validation errors: Show inline validation messages
- Import errors: Display in results dialog with detailed row information
- Export errors: Show toast/snackbar with error message

## Review

### Changes Summary

**1. frontend/src/types/device.ts**
- Added `RowResult` interface for import result row details
- Added `DeviceImportResult` interface for overall import results

**2. frontend/src/services/device.service.ts**
- Added import for `DeviceImportResult` type
- Added `importDevices(file)` method - uploads CSV with multipart/form-data
- Added `exportDevices(zone?, type?)` method - downloads CSV as Blob with optional filters

**3. frontend/src/views/DeviceListView.vue**
- **Template Changes**:
  - Added "Export CSV" button (all authenticated users)
  - Added "Import CSV" button (ADMIN/OPERATOR only)
  - Created import dialog with file upload component (v-file-input)
  - Created import results dialog with summary cards and detailed table
  - Used Vuetify components for consistent UI/UX

- **Script Changes**:
  - Added import for `deviceService` and `DeviceImportResult` type
  - Added reactive variables: `showImportDialog`, `showImportResultsDialog`, `importFile`, `importLoading`, `exportLoading`, `importError`, `importResults`
  - Added `fileValidationRule` function for client-side validation
  - Added `handleImport()` function for file upload and result display
  - Added `handleExport()` function for CSV download with dynamic filename
  - Added `closeImportDialog()` and `closeImportResultsDialog()` helper functions

### Implementation Highlights

**Export Functionality**:
- Uses current zone/type filters from the UI
- Generates filename with filters and date: `devices_zone_type_YYYY-MM-DD.csv`
- Downloads file directly to browser using Blob and download link
- Shows loading indicator during export

**Import Functionality**:
- File validation: CSV only, max 10MB (client-side check)
- Clear instructions in dialog showing CSV format and required fields
- Uploads file using multipart/form-data
- Displays detailed results with summary cards (Total/Success/Failed)
- Shows row-by-row results in table with color-coded status chips
- Refreshes device list after successful import

**Security**:
- Export: Available to all authenticated users
- Import: Restricted to ADMIN and OPERATOR roles only
- Uses existing JWT token authentication from api.ts interceptors

**Error Handling**:
- File validation errors shown inline in import dialog
- Network errors shown in alert/error message
- Import results show detailed error messages per row
- Console logging for debugging

### Testing Results

**Build Verification**:
- ✅ Frontend builds successfully with no TypeScript errors
- ✅ All components compile correctly
- ✅ No linting errors

**Manual Testing Required**:
The following scenarios should be tested manually:
- [ ] Export with no filters (all devices)
- [ ] Export with zone filter only
- [ ] Export with type filter only
- [ ] Export with both zone and type filters
- [ ] Import with valid CSV file
- [ ] Import with invalid CSV (validation errors)
- [ ] Import with partially valid CSV (some rows fail)
- [ ] Import authorization (only ADMIN/OPERATOR can access)
- [ ] File size validation (>10MB rejection)
- [ ] CSV file type validation (non-CSV rejection)
- [ ] Verify device list refreshes after import
- [ ] Verify import results dialog shows correct summary
- [ ] Verify row-by-row error messages are displayed

### Known Issues

None discovered during implementation. All functionality follows existing patterns and integrates cleanly with the existing codebase.

### Architecture Notes

- Follows Vue 3 Composition API pattern used throughout the project
- Uses Vuetify components for consistent UI/UX
- Integrates with existing authentication and authorization system
- Service layer properly separates API calls from UI logic
- TypeScript types ensure type safety across the stack
- Minimal code changes - only added CSV functionality without modifying existing device management features
