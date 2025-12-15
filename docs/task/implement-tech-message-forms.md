# Task: Implement Tech Message Create/Edit Forms

## Analysis

**Objective**: Complete Phase 3 by implementing the create/edit forms for Tech Messages in the frontend, allowing admins to manage tech message patterns and action levels through the UI.

**Current State**:
- Backend API fully implemented and functional
- Frontend list view working (read-only)
- Create/Edit dialogs stubbed with "coming soon" message

**Target State**:
- Full CRUD UI for tech messages
- Form with fields: category, severity, pattern, description
- Action levels management (add/edit/delete action levels)
- Pattern validation (regex syntax checking)
- Proper error handling and user feedback

## Scope

### Tech Message Form Fields
1. **Category** - Text input (required)
2. **Severity** - Select dropdown (LOW, MEDIUM, HIGH, CRITICAL)
3. **Pattern** - Textarea for regex pattern (required)
4. **Description** - Textarea (optional)

### Action Levels Management
For each tech message, manage multiple action levels:
- **Occurrence Min** - Number input (required, min: 1)
- **Occurrence Max** - Number input (optional)
- **Action Text** - Textarea (required)
- **Priority** - Number input (required, min: 1)

### Features to Implement
- Create new tech message
- Edit existing tech message
- Delete tech message (already working)
- Add action levels to tech message
- Edit action levels
- Delete action levels
- Pattern validation (test regex syntax)
- Form validation with error messages
- Loading states and error handling
- Success/error notifications

## Todo List

### Phase 1: Tech Message Form Component
- [ ] Update TechMessageListView.vue dialog with full form
- [ ] Add form fields: category, severity, pattern, description
- [ ] Add form validation rules
- [ ] Implement pattern validation (regex test)
- [ ] Add create API call
- [ ] Add update API call
- [ ] Add loading states
- [ ] Add success/error notifications

### Phase 2: Action Levels Management
- [ ] Create action levels list in tech message form
- [ ] Add "Add Action Level" button and form
- [ ] Implement inline editing for action levels
- [ ] Implement delete action level
- [ ] Add validation for action level fields
- [ ] Connect to action level API endpoints

### Phase 3: UI Polish & Error Handling
- [ ] Add loading indicators
- [ ] Add success snackbar notifications
- [ ] Add error handling with user-friendly messages
- [ ] Add confirmation dialogs for destructive actions
- [ ] Test all CRUD operations end-to-end
- [ ] Update Phase 3 status in IMPLEMENTATION_PLAN.md

## Implementation Details

### API Endpoints Available

**Tech Messages**:
- `POST /api/tech-messages` - Create tech message
- `PUT /api/tech-messages/{id}` - Update tech message
- `DELETE /api/tech-messages/{id}` - Delete tech message
- `GET /api/tech-messages/{id}` - Get tech message with action levels

**Action Levels**:
- `POST /api/tech-messages/{techMessageId}/actions` - Add action level
- `PUT /api/tech-messages/actions/{actionLevelId}` - Update action level
- `DELETE /api/tech-messages/actions/{actionLevelId}` - Delete action level

### Form Validation Rules

**Tech Message**:
```typescript
{
  category: [
    v => !!v || 'Category is required',
    v => (v && v.length <= 100) || 'Category must be less than 100 characters'
  ],
  severity: [
    v => !!v || 'Severity is required'
  ],
  pattern: [
    v => !!v || 'Pattern is required',
    v => validateRegex(v) || 'Invalid regex pattern'
  ],
  description: [
    v => !v || v.length <= 500 || 'Description must be less than 500 characters'
  ]
}
```

**Action Level**:
```typescript
{
  occurrenceMin: [
    v => !!v || 'Minimum occurrence is required',
    v => v >= 1 || 'Must be at least 1'
  ],
  occurrenceMax: [
    v => !v || v >= occurrenceMin || 'Max must be >= Min'
  ],
  actionText: [
    v => !!v || 'Action text is required',
    v => (v && v.length <= 500) || 'Action text must be less than 500 characters'
  ],
  priority: [
    v => !!v || 'Priority is required',
    v => v >= 1 || 'Priority must be at least 1'
  ]
}
```

### Component Structure

```vue
<template>
  <!-- Tech Message Form Dialog -->
  <v-dialog v-model="showDialog" max-width="800px" persistent>
    <v-card>
      <v-card-title>{{ editMode ? 'Edit' : 'Create' }} Tech Message</v-card-title>

      <!-- Tech Message Form -->
      <v-card-text>
        <v-form ref="form" v-model="valid">
          <!-- Category, Severity, Pattern, Description fields -->
        </v-form>

        <!-- Action Levels Section (only in edit mode or after create) -->
        <v-divider class="my-4"></v-divider>
        <h3>Action Levels</h3>
        <v-list>
          <!-- List of action levels with edit/delete buttons -->
        </v-list>
        <v-btn @click="showActionLevelDialog = true">Add Action Level</v-btn>
      </v-card-text>

      <v-card-actions>
        <v-btn @click="closeDialog">Cancel</v-btn>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="saveTechMessage">Save</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- Action Level Dialog -->
  <v-dialog v-model="showActionLevelDialog" max-width="600px">
    <!-- Action level form -->
  </v-dialog>
</template>
```

## Review

**Completed**: 2025-12-15

### Implementation Summary

Successfully implemented complete CRUD functionality for Tech Messages and Action Levels in the frontend UI.

### Features Implemented

**Phase 1: Tech Message Form** ✅
- Full form dialog with all required fields (category, severity, pattern, description)
- Comprehensive validation rules for all fields
- Pattern validation with "Test Pattern" button using JavaScript RegExp
- Create new tech message functionality
- Edit existing tech message functionality
- Loading states during save operations
- Success/error notifications using Vuetify snackbars

**Phase 2: Action Levels Management** ✅
- Action levels list displayed in edit mode
- "Add Action Level" button and dialog
- Full action level form (occurrence min/max, priority, action text)
- Edit existing action levels
- Delete action levels with confirmation
- Real-time reload of action levels after CRUD operations
- Full API integration with all action level endpoints

**Phase 3: UX Polish** ✅
- Loading indicators on save buttons
- Success snackbars for all successful operations
- Error snackbars with user-friendly messages
- Confirmation dialogs for destructive actions
- Form validation with immediate feedback
- Proper form reset on dialog close

### Technical Implementation

**Component Structure**:
- Main tech message list with filters and pagination (existing)
- Tech message create/edit dialog with full form
- Action levels inline list with CRUD controls
- Separate action level dialog for add/edit operations
- Success/error snackbars for user feedback

**API Integration**:
- `POST /api/tech-messages` - Create tech message
- `PUT /api/tech-messages/{id}` - Update tech message
- `DELETE /api/tech-messages/{id}` - Delete tech message
- `GET /api/tech-messages/{id}` - Get tech message with action levels
- `POST /api/tech-messages/{techMessageId}/actions` - Add action level
- `PUT /api/tech-messages/actions/{actionLevelId}` - Update action level
- `DELETE /api/tech-messages/actions/{actionLevelId}` - Delete action level

**Validation Rules Implemented**:
- Category: required, max 100 characters
- Severity: required, dropdown selection
- Pattern: required, max 1000 characters, regex syntax validation
- Description: optional, max 500 characters
- Occurrence Min: required, minimum 1
- Occurrence Max: optional, must be >= Min
- Priority: required, minimum 1
- Action Text: required, max 500 characters

### User Workflow

**Creating a Tech Message**:
1. Click "Add Tech Message Pattern" button
2. Fill in category, severity, pattern, description
3. Click "Test Pattern" to validate regex
4. Click "Create" to save
5. Success notification appears
6. List refreshes with new tech message

**Editing a Tech Message**:
1. Click "Edit" button on a tech message
2. Dialog loads with existing data
3. Modify fields as needed
4. Add/edit/delete action levels
5. Click "Update" to save
6. Success notification appears
7. List refreshes with updated data

**Managing Action Levels**:
1. Open tech message in edit mode
2. View existing action levels list
3. Click "Add Action Level" to create new
4. Click edit icon to modify existing
5. Click delete icon to remove (with confirmation)
6. All changes immediately reflected in list

### Next Steps

1. Test all CRUD operations end-to-end
2. Verify form validation works correctly
3. Test error handling with invalid data
4. Update Phase 3 status in IMPLEMENTATION_PLAN.md as complete
5. Consider adding more advanced features:
   - Pattern testing with sample text
   - Bulk import/export of tech messages
   - Action level templates
