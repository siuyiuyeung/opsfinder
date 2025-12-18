# Task: Group Tech Messages by Category then Severity

## Analysis

After reading the codebase, I understand the current tech message structure:

**Current State**:
- TechMessage entity has `category` (String) and `severity` (Enum: LOW, MEDIUM, HIGH, CRITICAL)
- Frontend displays tech messages in a flat expansion panel list
- Users can filter by category or severity using dropdowns
- Messages are shown in default order without grouping

**Desired State**:
- Tech messages should be grouped by category first
- Within each category, messages should be sub-grouped by severity
- Display hierarchy: Category → Severity → Messages

**Implementation Approach**:
- **Frontend-only solution**: Group messages in the Vue component after fetching
- Keep backend API unchanged (simpler, minimal code impact)
- Use nested v-expansion-panels or custom grouping UI
- Maintain existing filter functionality

## Todo List

- [ ] Analyze severity ordering (should be CRITICAL → HIGH → MEDIUM → LOW)
- [ ] Create grouping logic in Vue component to organize messages by category then severity
- [ ] Update the UI to display nested groups (category → severity → messages)
- [ ] Ensure severity chips and action levels display correctly within groups
- [ ] Test filter functionality with new grouped display
- [ ] Verify pagination works correctly with grouped view
- [ ] Ensure admin controls (Add Pattern, Edit, Delete) work with new structure

## Implementation Details

### Frontend Changes (TechMessageListView.vue)

1. **Add computed property for grouped messages**:
   ```typescript
   const groupedMessages = computed(() => {
     // Group by category, then by severity
     const grouped: Record<string, Record<string, TechMessage[]>> = {}

     techMessages.value.forEach(msg => {
       if (!grouped[msg.category]) {
         grouped[msg.category] = {}
       }
       if (!grouped[msg.category][msg.severity]) {
         grouped[msg.category][msg.severity] = []
       }
       grouped[msg.category][msg.severity].push(msg)
     })

     return grouped
   })
   ```

2. **Update template to use nested expansion panels**:
   - Outer panel: Category level
   - Inner panel: Severity level
   - Innermost panel: Individual messages

3. **Severity ordering**:
   - Define severity order array: `['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']`
   - Sort severities within each category group

4. **Category ordering**:
   - Sort categories alphabetically for consistency

### UI Structure
```
📁 Category 1 (Network) - 15 messages
  ├─ 🔴 CRITICAL (3)
  │   ├─ Message 1
  │   ├─ Message 2
  │   └─ Message 3
  ├─ 🟠 HIGH (5)
  │   └─ ...
  ├─ 🟡 MEDIUM (6)
  └─ 🟢 LOW (1)

📁 Category 2 (Database) - 8 messages
  ├─ 🔴 CRITICAL (2)
  └─ ...
```

## Review

### Changes Made

**File Modified**: `frontend/src/views/TechMessageListView.vue`

1. **Added computed properties** (lines 338-382):
   - `severityOrder`: Constant defining priority order (CRITICAL → HIGH → MEDIUM → LOW)
   - `groupedMessages`: Groups tech messages by category, then by severity
   - `sortedCategories`: Returns alphabetically sorted category names

2. **Added helper functions**:
   - `getCategoryMessageCount()`: Counts total messages in a category
   - `getActiveSeverities()`: Returns sorted severities present in a category
   - `truncatePattern()`: Truncates long patterns for display (max 50 chars)

3. **Updated template** (lines 36-103):
   - Implemented 3-level nested expansion panels:
     - **Level 1**: Category (with folder icon, total message count)
     - **Level 2**: Severity (with colored chip, message count per severity)
     - **Level 3**: Individual messages (with truncated pattern preview)

4. **Preserved existing functionality**:
   - Filter dropdowns still work (filter happens before grouping)
   - Admin controls (Add Pattern, Edit, Delete) remain functional
   - Pagination works as before (groups based on current page data)
   - All action levels display correctly within message details

### Display Structure

```
📁 Database (8 messages)
  ├─ 🔴 CRITICAL (2 messages)
  │   ├─ Pattern: Connection timeout...
  │   └─ Pattern: Deadlock detected...
  ├─ 🟡 MEDIUM (6 messages)

📁 Network (15 messages)
  ├─ 🔴 CRITICAL (3 messages)
  ├─ 🟠 HIGH (5 messages)
  ├─ 🟡 MEDIUM (6 messages)
  └─ 🟢 LOW (1 message)
```

### Benefits

- **Improved organization**: Messages logically grouped by category and severity
- **Better navigation**: Users can expand categories and severities of interest
- **Clear hierarchy**: Visual separation makes it easier to find specific messages
- **Severity prioritization**: Critical messages always appear first within each category
- **Minimal code impact**: Frontend-only change, no backend modifications required
