# Task: Make Search Input Box Single Line

## Analysis

After reading the codebase, I found the search input in `frontend/src/views/TechMessageSearchView.vue` (lines 28-43).

Currently, the search input uses a `v-textarea` component with `rows="4"`, which makes it display as a 4-line text area by default. The user wants this to be a single line instead.

**Current implementation:**
```vue
<v-textarea
  v-model="searchText"
  label="Error message or keywords"
  placeholder=""
  variant="outlined"
  rows="4"           ← This makes it 4 lines tall
  clearable
  autofocus
  :loading="searching"
  @input="onSearchInput"
  @keydown.enter.prevent="performSearch"
>
```

**Two possible approaches:**
1. **Simple approach**: Change `rows="4"` to `rows="1"`
2. **Better approach**: Convert `v-textarea` to `v-text-field` (more appropriate for single-line input)

I recommend **Approach 1 (Simple)** because:
- Minimal code change (one attribute value change)
- Preserves all existing functionality
- Follows the "simplest solution" principle from CLAUDE.md
- If multi-line input is ever needed in the future, users can still manually expand the textarea

## Todo List
- [x] Change `rows="4"` to `rows="1"` in TechMessageSearchView.vue line 33
- [ ] Test the UI to ensure the search box displays as single-line (User testing)
- [ ] Verify all functionality still works (search, debouncing, enter key) (User testing)

## Review

**Changes Made:**
- Updated `frontend/src/views/TechMessageSearchView.vue` line 33
- Changed `rows="4"` to `rows="1"` in the search textarea component

**Impact:**
- Search input box now displays as single line by default
- Users can still manually expand the textarea if needed by dragging the resize handle
- All existing functionality preserved (autofocus, clearable, debouncing, enter key search)
- No changes to backend or logic required

**Files Modified:**
- `frontend/src/views/TechMessageSearchView.vue`

**Testing Recommendations:**
1. Verify search box displays as single line on page load
2. Confirm search functionality works (typing, auto-search, enter key)
3. Test textarea resize handle still works if users want multi-line input
4. Check mobile responsiveness
