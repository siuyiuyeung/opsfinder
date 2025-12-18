# Task: Improve Action Level Display Readability

## Analysis

After reading the codebase, I found the action levels display in `frontend/src/views/TechMessageSearchView.vue` (lines 193-214).

**Current implementation:**
```vue
<v-list-item>
  <template v-slot:prepend>
    <v-chip size="x-small" class="mr-2" variant="tonal" color="primary">
      {{ action.occurrenceMin }}{{ action.occurrenceMax ? `-${action.occurrenceMax}` : '+' }} times
    </v-chip>
    <v-chip size="x-small" class="mr-2" variant="outlined" color="grey-darken-2">
      Priority: {{ action.priority }}
    </v-chip>
  </template>
  <v-list-item-title>{{ action.actionText }}</v-list-item-title>
</v-list-item>
```

**Current issues:**
- Chips in prepend slot consume horizontal space
- Action text is pushed to the right and harder to read
- On mobile/narrow screens, chips may wrap or truncate action text

**Proposed solution:**
Display occurrence and priority chips ABOVE the action text in a vertical layout.

**Current layout (horizontal):**
```
[chip1] [chip2] Action text goes here...
```

**New layout (vertical):**
```
[chip1] [chip2]
Action text goes here...
```

This will make the action text much easier to read while keeping the metadata visible.

**Additional requirement:**
- Collapse the description and action levels by default
- Add expand/collapse functionality for better space efficiency
- Show only key information (pattern, match type) by default

## Todo List
- [x] Remove the prepend slot from action levels
- [x] Move occurrence and priority chips above the action text
- [x] Ensure action text displays full-width without horizontal constraints
- [x] Add collapsible sections for description and all action levels
- [x] Pattern always visible, details collapsed by default
- [ ] Test on mobile/narrow screens for readability (User testing)

## Review

**Changes Made:**
- Updated `frontend/src/views/TechMessageSearchView.vue`
- Removed prepend slot from action level list items (lines 203-223)
- Moved occurrence and priority chips above action text in vertical layout
- Added expand/collapse functionality with chevron button in card title
- Added `expandedMatches` ref (Set<number>) to track expansion state per match
- Added `toggleMatchDetails()` and `isMatchExpanded()` helper functions

**New Display Structure:**

**Always Visible (Collapsed State):**
- Severity chip
- Category name
- Match type chip (Exact/Fuzzy with score)
- Pattern (regex pattern)
- Expand/Collapse button (chevron icon)

**Collapsible Details (Hidden by Default):**
- Description
- Extracted Variables
- Recommended Action
- All Action Levels (with chips above text)

**Action Level Layout (Vertical):**
```
[1-5 times] [Priority: 1]
Action text goes here on full width...
```

**Impact:**
- Much better readability for action text (full-width display)
- Cleaner, more compact default view showing only essential info
- Better mobile experience with collapsible sections
- Chips no longer consume horizontal space next to action text
- Users can expand individual matches to see full details

**Files Modified:**
- `frontend/src/views/TechMessageSearchView.vue`

**Testing Recommendations:**
1. Verify pattern is always visible for all matches
2. Click expand/collapse button - details should toggle
3. Verify action text displays full-width below chips
4. Test on mobile/narrow screens for readability
5. Confirm multiple matches can be expanded independently
