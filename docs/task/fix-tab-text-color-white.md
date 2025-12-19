# Task: Fix Tab Text Color to White

## Analysis
After reading `frontend/src/views/TechMessageSearchView.vue:5-14`, I found that:
- The `<v-tabs>` component has `bg-color="primary"` which creates a dark background
- The tab text for "Quick Search" and "Management" doesn't have an explicit white color
- This makes the text difficult to read against the dark background

## Solution
Add explicit white text color to the tabs by adding a custom CSS class that forces both text and icons to be white, matching the style of the "+ Add Pattern" button.

## Todo List
- [x] Add `class="white-text"` to both `<v-tab>` elements in TechMessageSearchView.vue
- [x] Add CSS styles for `.white-text` class with `!important` to override Vuetify defaults

## Changes
File: `frontend/src/views/TechMessageSearchView.vue`
- Line 6: Added `class="white-text"` to the "Quick Search" tab
- Line 10: Added `class="white-text"` to the "Management" tab
- Lines 484-490: Added CSS styles for white text and icons with `!important` override

## Review
Successfully fixed the tab text color issue by adding a custom `white-text` CSS class that forces both the text and icons to white using `!important` declarations. The "Quick Search" and "Management" tabs now display white text and icons against the primary (dark) background, matching the appearance of the "+ Add Pattern" button.

**Changes Made:**
- Modified `frontend/src/views/TechMessageSearchView.vue:6` - Added `class="white-text"` to Quick Search tab
- Modified `frontend/src/views/TechMessageSearchView.vue:10` - Added `class="white-text"` to Management tab
- Added CSS styles in `<style scoped>` section:
  - `.white-text { color: white !important; }`
  - `.white-text .v-icon { color: white !important; }`

**Impact:**
- Minimal code change (2 classes added + 2 CSS rules)
- Improves readability significantly
- Consistent styling with other primary-colored buttons
- No functional changes, only visual improvement
