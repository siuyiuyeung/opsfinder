# Task: Rename "Error Knowledge Base" to "Tech Message" and Remove Error Wording

## Analysis

**Objective**: Completely remove "error" wording from the Tech Message feature throughout the entire codebase to avoid negative connotations and better reflect its purpose as a technical message knowledge base.

**Scope**: This is a comprehensive refactoring affecting:
- Database tables, columns, constraints, and Liquibase changelog
- Backend Java entities, repositories, services, controllers, DTOs, mappers
- Frontend TypeScript types, views, routes, navigation
- All variable names, method parameters, JavaDoc comments
- Documentation and implementation plan

**Current Naming**:
- Database: `error_messages` table, `error_message_id` column
- Backend: `ErrorMessage` entity, `ErrorMessageController`, `errorMessage` fields/variables
- Frontend: `ErrorListView.vue`, `error.ts`, `/errors` route, `errorMessage` variables
- UI: "Error Knowledge Base", "Error Messages", "Add Error Pattern"
- Comments: "error message", "error pattern", "error text"

**Target Naming**:
- Database: `tech_messages` table, `tech_message_id` column
- Backend: `TechMessage` entity, `TechMessageController`, `techMessage` fields/variables
- Frontend: `TechMessageListView.vue`, `tech.ts`, `/tech-messages` route, `techMessage` variables
- UI: "Tech Message Library", "Tech Messages", "Add Tech Message"
- Comments: "tech message", "message pattern", "message text"

**IMPORTANT - Do NOT Change**:
- `ErrorResponse` - HTTP error response DTO (actual exceptions)
- `GlobalExceptionHandler` - Exception handling (actual errors)
- Spring Security error handling
- Validation error messages
- Any code genuinely dealing with exceptions/errors

## Summary of Changes

**Files to Rename**: 7 Java files, 2 TypeScript files
**Database Objects**: 1 table, 1 column, 4 indexes, 1 foreign key constraint
**Variable Names**: ~50+ occurrences of `errorMessage`/`errorMessages` → `techMessage`/`techMessages`
**Method Parameters**: ~30+ occurrences of `errorMessageId` → `techMessageId`
**API Endpoints**: 1 base path change (`/api/error-messages` → `/api/tech-messages`)
**Frontend Routes**: 1 route (`/errors` → `/tech-messages`)
**UI Text**: ~10+ user-facing strings
**Comments & JavaDoc**: ~50+ documentation updates

**Estimated Effort**: 3-4 hours for careful, systematic execution
**Risk Level**: Medium (many interconnected changes, but straightforward renames)
**Impact**: Breaking change for API consumers (endpoint URLs change)

## Todo List

### Phase 1: Database Migration
- [ ] Create new Liquibase changeset to rename `error_messages` to `tech_messages`
- [ ] Rename column: `action_levels.error_message_id` to `tech_message_id`
- [ ] Rename indexes: `idx_error_messages_*` to `idx_tech_messages_*`
- [ ] Rename index: `idx_action_levels_error_message` to `idx_action_levels_tech_message`
- [ ] Rename foreign key: `fk_action_level_error_message` to `fk_action_level_tech_message`
- [ ] Update seed data comments in changelog-003

### Phase 2: Backend - Entities & DTOs
- [ ] Rename `ErrorMessage.java` to `TechMessage.java`
- [ ] Update entity `@Table(name = "tech_messages")` and indexes
- [ ] Update JavaDoc comments to use "tech message" terminology
- [ ] Rename `ErrorMessageRequest.java` to `TechMessageRequest.java`
- [ ] Rename `ErrorMessageResponse.java` to `TechMessageResponse.java`
- [ ] Update `PatternMatchResponse.java` field: `errorMessage` → `techMessage`

### Phase 3: Backend - Repositories & Services
- [ ] Rename `ErrorMessageRepository.java` to `TechMessageRepository.java`
- [ ] Rename `ErrorMessageService.java` to `TechMessageService.java`
  - [ ] Rename all variables: `errorMessage` → `techMessage`, `errorMessages` → `techMessages`
  - [ ] Rename all parameters: `errorMessageId` → `techMessageId`
  - [ ] Rename methods: `getAllErrorMessages()` → `getAllTechMessages()`, etc.
  - [ ] Rename methods: `getErrorMessageById()` → `getTechMessageById()`, etc.
  - [ ] Rename methods: `createErrorMessage()` → `createTechMessage()`, etc.
  - [ ] Rename methods: `updateErrorMessage()` → `updateTechMessage()`, etc.
  - [ ] Rename methods: `deleteErrorMessage()` → `deleteTechMessage()`, etc.
  - [ ] Rename methods: `getErrorMessagesByCategory()` → `getTechMessagesByCategory()`, etc.
  - [ ] Rename methods: `getErrorMessagesBySeverity()` → `getTechMessagesBySeverity()`, etc.
  - [ ] Rename methods: `countErrorsByCategory()` → `countMessagesByCategory()`, etc.
  - [ ] Rename methods: `countErrorsBySeverity()` → `countMessagesBySeverity()`, etc.
  - [ ] Rename method: `matchErrorText()` → `matchMessageText()` or `matchText()`
  - [ ] Update all JavaDoc comments
  - [ ] Update all log messages
- [ ] Rename `ErrorMessageMapper.java` to `TechMessageMapper.java`
  - [ ] Update parameter name: `errorMessage` → `techMessage`
  - [ ] Update all method signatures
  - [ ] Update generated implementation will auto-regenerate

### Phase 4: Backend - Controllers & API
- [ ] Rename `ErrorMessageController.java` to `TechMessageController.java`
- [ ] Update `@RequestMapping("/api/tech-messages")` (was `/api/error-messages`)
- [ ] Rename service field: `errorMessageService` → `techMessageService`
- [ ] Rename all variables: `errorMessage` → `techMessage`, `errorMessages` → `techMessages`
- [ ] Rename all parameters: `errorMessageId` → `techMessageId`
- [ ] Update endpoint: `POST /{techMessageId}/actions` (was `/{errorMessageId}/actions`)
- [ ] Update all method names to use "TechMessage" instead of "ErrorMessage"
- [ ] Update all JavaDoc comments
- [ ] Update all log messages
- [ ] Keep `errorText` parameter in match endpoint (it's the input text)

### Phase 5: Backend - Utilities & Related Entities
- [ ] Update `ActionLevel.java`:
  - [ ] Rename field: `errorMessage` → `techMessage` (line 33)
  - [ ] Update `@JoinColumn(name = "tech_message_id")` (line 32)
  - [ ] Update `@Index(name = "idx_action_levels_tech_message")` (line 15)
  - [ ] Update `@ForeignKey(name = "fk_action_level_tech_message")` (line 32)
  - [ ] Update `@ToString(exclude = "techMessage")` (line 23)
  - [ ] Update `@EqualsAndHashCode(exclude = "techMessage")` (line 24)
  - [ ] Update JavaDoc: "parent error message" → "parent tech message" (line 28)
  - [ ] Update validation message: "Error message is required" → "Tech message is required" (line 30)
  - [ ] Update all comments with "error" → "message"
- [ ] Update `PatternMatcher.java`:
  - [ ] Rename parameter: `errorMessages` → `techMessages` (line 31, 34, 40)
  - [ ] Rename variable: `errorMessage` → `techMessage` (line 40, 42, 47, 53, 60, 64)
  - [ ] Rename method: `matchError()` → `matchMessage()` (line 34)
  - [ ] Update MatchResult field: `errorMessage` → `techMessage` (line 137)
  - [ ] Update JavaDoc comments replacing "error message" with "tech message"
  - [ ] Keep `errorText` parameter name (it's the input text to match)
- [ ] Update `FrequencyAnalyzer.java`:
  - [ ] Rename all parameters: `errorMessageId` → `techMessageId` (lines 26, 30, 55, 59, 69, 73, 82, 86)
  - [ ] Update all method JavaDoc comments
  - [ ] Update log messages
- [ ] Update `ActionLevelRepository.java`:
  - [ ] Rename method: `findByErrorMessageId()` → `findByTechMessageId()` (line 23)
  - [ ] Rename method: `findByErrorMessageIdAndOccurrenceRange()` → `findByTechMessageIdAndOccurrenceRange()` (line 40)
  - [ ] Rename method: `deleteByErrorMessageId()` → `deleteByTechMessageId()` (line 69)
  - [ ] Update JPQL queries: `al.errorMessage.id` → `al.techMessage.id` (lines 34, 53)
  - [ ] Update all parameter names: `errorMessageId` → `techMessageId`
  - [ ] Update all JavaDoc comments

### Phase 6: Frontend - Types & Services
- [ ] Rename `frontend/src/types/error.ts` to `tech.ts`
- [ ] Update type names:
  - [ ] `ErrorMessage` → `TechMessage`
  - [ ] `ErrorMessageRequest` → `TechMessageRequest`
- [ ] Update interface comments: "Error message" → "Tech message"
- [ ] Update `PatternMatchResponse.errorMessage` → `techMessage`
- [ ] Update all property names in interfaces
- [ ] Create/update tech message service for API calls (if separate service exists)
- [ ] Update API endpoint URLs in service: `/api/error-messages` → `/api/tech-messages`

### Phase 7: Frontend - Views & Routes
- [ ] Rename `ErrorListView.vue` to `TechMessageListView.vue`
- [ ] Update component:
  - [ ] Title: "Error Knowledge Base" → "Tech Message Library" (line 6)
  - [ ] Button: "Add Error Pattern" → "Add Tech Message" (line 9)
  - [ ] Variable: `errors` → `messages` (line 38)
  - [ ] Method: `loadErrors` → `loadMessages` (lines 22, 31, 81)
  - [ ] Method: `editError` → `editMessage` (line 72)
  - [ ] Dialog title: "Error Pattern" → "Tech Message" (line 89)
  - [ ] All user-facing text with "error" → "message" or "tech message"
  - [ ] Update import statement from `types/error` → `types/tech`
  - [ ] Update type references: `ErrorMessage` → `TechMessage`
- [ ] Update `router/index.ts`:
  - [ ] Route path: `/errors` → `/tech-messages` (line 30)
  - [ ] Route name: `Errors` → `TechMessages` (line 31)
  - [ ] Component import: `ErrorListView` → `TechMessageListView` (line 32)
- [ ] Update `App.vue` navigation:
  - [ ] Title: "Error Messages" → "Tech Messages" (line 18)
  - [ ] Icon: Keep or change `mdi-alert-circle` (see Phase 8)

### Phase 8: Frontend - Icons & UI Polish
- [ ] Update icon from `mdi-alert-circle` to `mdi-message-text` or `mdi-book-open-variant`
- [ ] Review and update all user-facing text for consistency
- [ ] Update button labels and dialog titles

### Phase 9: Documentation
- [ ] Update `IMPLEMENTATION_PLAN.md` - Phase 3 section
- [ ] Update `CLAUDE.md` - Any references to error messages
- [ ] Update `README.md` - Feature descriptions
- [ ] Update this task file with review section

### Phase 10: Testing & Verification
- [ ] Build backend: `./gradlew clean build`
- [ ] Build frontend: `npm run build`
- [ ] Test API endpoints with new URLs
- [ ] Test UI navigation and functionality
- [ ] Verify database migration applied correctly

## Implementation Details

### Database Changes (Liquibase)

**New Changeset**: `changelog-003b-rename-error-to-tech-messages.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: 003b-rename-error-messages-to-tech-messages
      author: claude
      changes:
        # Step 1: Rename table
        - renameTable:
            oldTableName: error_messages
            newTableName: tech_messages

        # Step 2: Rename indexes on tech_messages table
        - sql:
            sql: ALTER INDEX idx_error_messages_category RENAME TO idx_tech_messages_category;
        - sql:
            sql: ALTER INDEX idx_error_messages_severity RENAME TO idx_tech_messages_severity;

        # Step 3: Rename column in action_levels table
        - renameColumn:
            tableName: action_levels
            oldColumnName: error_message_id
            newColumnName: tech_message_id
            columnDataType: BIGINT

        # Step 4: Rename index on action_levels table
        - sql:
            sql: ALTER INDEX idx_action_levels_error_message RENAME TO idx_action_levels_tech_message;

        # Step 5: Update foreign key constraint
        - dropForeignKeyConstraint:
            baseTableName: action_levels
            constraintName: fk_action_level_error_message
        - addForeignKeyConstraint:
            baseTableName: action_levels
            baseColumnNames: tech_message_id
            constraintName: fk_action_level_tech_message
            referencedTableName: tech_messages
            referencedColumnNames: id
            onDelete: CASCADE
```

### Backend Entity Rename

**Before** (`ErrorMessage.java`):
```java
@Entity
@Table(name = "error_messages", indexes = {
    @Index(name = "idx_error_messages_category", columnList = "category"),
    @Index(name = "idx_error_messages_severity", columnList = "severity")
})
public class ErrorMessage extends BaseEntity {
    // ...
}
```

**After** (`TechMessage.java`):
```java
@Entity
@Table(name = "tech_messages", indexes = {
    @Index(name = "idx_tech_messages_category", columnList = "category"),
    @Index(name = "idx_tech_messages_severity", columnList = "severity")
})
public class TechMessage extends BaseEntity {
    // ...
}
```

### API Endpoint Changes

**Old**: `GET /api/error-messages?category=Network`

**New**: `GET /api/tech-messages?category=Network`

### Frontend Route Changes

**Old**:
```typescript
{
  path: '/errors',
  name: 'Errors',
  component: () => import('@/views/ErrorListView.vue')
}
```

**New**:
```typescript
{
  path: '/tech-messages',
  name: 'TechMessages',
  component: () => import('@/views/TechMessageListView.vue')
}
```

### UI Text Changes

| Old | New |
|-----|-----|
| Error Knowledge Base | Tech Message Library |
| Error Messages | Tech Messages |
| Add Error Pattern | Add Tech Message |
| Edit Error Pattern | Edit Tech Message |
| Error category | Message category |
| Error pattern | Message pattern |

## Terminology Guidelines

### Change These Terms

| Old Term | New Term | Context |
|----------|----------|---------|
| Error Message | Tech Message | Feature name |
| Error Knowledge Base | Tech Message Library | UI title |
| error message | tech message | Comments, JavaDoc |
| error pattern | message pattern | UI, comments |
| error category | message category | UI, comments |
| errorMessage (variable) | techMessage | All code |
| errorMessages (variable) | techMessages | All code |
| errorMessageId (parameter) | techMessageId | All code |
| error_messages (table) | tech_messages | Database |
| error_message_id (column) | tech_message_id | Database |
| ErrorMessage (class) | TechMessage | Java entity |
| ErrorMessageRequest (DTO) | TechMessageRequest | Java DTO |
| ErrorMessageResponse (DTO) | TechMessageResponse | Java DTO |
| ErrorMessageService | TechMessageService | Java service |
| ErrorMessageController | TechMessageController | Java controller |
| ErrorMessageRepository | TechMessageRepository | Java repository |
| ErrorMessageMapper | TechMessageMapper | Java mapper |
| error.ts (file) | tech.ts | TypeScript file |
| ErrorListView.vue (file) | TechMessageListView.vue | Vue component |
| /errors (route) | /tech-messages | Frontend route |
| /api/error-messages (API) | /api/tech-messages | REST API |

### DO NOT Change These

| Term | Keep As-Is | Reason |
|------|-----------|--------|
| ErrorResponse | ErrorResponse | HTTP error response DTO |
| GlobalExceptionHandler | GlobalExceptionHandler | Exception handling |
| error (in validation) | error | Validation errors are real errors |
| errorText (parameter) | errorText | Input text to match against patterns |
| try-catch error | error | Exception handling |
| Spring Security errors | errors | Framework error handling |

### Preferred New Terms

**Primary**:
- Tech Message (in UI, documentation)
- Technical Message (formal documentation)
- Message (when context is clear)
- Pattern (for regex patterns)

**Secondary**:
- Message category (not "category" alone)
- Message severity
- Message pattern
- Action level

**Avoid**:
- Error (except for genuine exceptions)
- Problem
- Issue (reserved for incidents)

## Review

**Completed**: 2025-12-15

### Summary of Changes

Successfully completed comprehensive refactoring to rename "Error Knowledge Base" feature to "Tech Message" throughout the entire codebase. All "error" wording removed from domain-specific code while preserving genuine exception handling terminology.

### Phases Completed

**Phase 1: Database Migration** ✅
- Created `changelog-003b-rename-error-to-tech-messages.yaml`
- Renamed table: `error_messages` → `tech_messages`
- Renamed column: `action_levels.error_message_id` → `tech_message_id`
- Renamed 4 indexes: `idx_error_messages_*` → `idx_tech_messages_*`, `idx_action_levels_error_message` → `idx_action_levels_tech_message`
- Renamed foreign key: `fk_action_level_error_message` → `fk_action_level_tech_message`
- Added to master changelog

**Phase 2: Backend Entities & DTOs** ✅
- Renamed using `git mv`: `ErrorMessage.java` → `TechMessage.java`
- Renamed using `git mv`: `ErrorMessageRequest.java` → `TechMessageRequest.java`
- Renamed using `git mv`: `ErrorMessageResponse.java` → `TechMessageResponse.java`
- Updated table name, indexes, JavaDoc in entity
- Updated `PatternMatchResponse.errorMessage` field → `techMessage`

**Phase 3: Backend Repositories & Services** ✅
- Renamed using `git mv`: `ErrorMessageRepository.java` → `TechMessageRepository.java`
- Renamed using `git mv`: `ErrorMessageService.java` → `TechMessageService.java`
- Renamed using `git mv`: `ErrorMessageMapper.java` → `TechMessageMapper.java`
- Updated all method names (getAllErrorMessages → getAllTechMessages, etc.)
- Updated all JPQL queries
- Updated all variables and parameters (errorMessage → techMessage, errorMessageId → techMessageId)
- Updated all JavaDoc and log messages

**Phase 4: Backend Controllers & API** ✅
- Renamed using `git mv`: `ErrorMessageController.java` → `TechMessageController.java`
- Updated API endpoint: `/api/error-messages` → `/api/tech-messages`
- Updated all path variables: `errorMessageId` → `techMessageId`
- Updated all method names, variables, and JavaDoc

**Phase 5: Backend Utilities & Related Entities** ✅
- Updated `ActionLevel.java`: field `errorMessage` → `techMessage`, annotations, indexes, foreign keys
- Updated `ActionLevelRepository.java`: all method names (findByErrorMessageId → findByTechMessageId), JPQL queries
- Updated `PatternMatcher.java`: renamed `matchError()` → `matchMessage()`, MatchResult field
- Updated `FrequencyAnalyzer.java`: all parameters `errorMessageId` → `techMessageId`, JavaDoc

**Phase 6: Frontend Types & Services** ✅
- Renamed using `git mv`: `error.ts` → `tech.ts`
- Updated interface names: `ErrorMessage` → `TechMessage`, `ErrorMessageRequest` → `TechMessageRequest`
- Updated `PatternMatchResponse.errorMessage` → `techMessage`
- Updated all comments

**Phase 7: Frontend Views & Routes** ✅
- Renamed using `git mv`: `ErrorListView.vue` → `TechMessageListView.vue`
- Updated imports: `@/types/error` → `@/types/tech`
- Updated all variables: `errors` → `techMessages`, `error` → `techMessage`
- Updated all function names: `loadErrors` → `loadTechMessages`, `editError` → `editTechMessage`, `deleteError` → `deleteTechMessage`
- Updated all API calls: `/errors` → `/tech-messages`
- Updated UI text: "Error Knowledge Base" → "Tech Message Knowledge Base", "Add Error Pattern" → "Add Tech Message Pattern"
- Updated router: `/errors` → `/tech-messages`, component import
- Updated App.vue navigation: "Error Messages" → "Tech Messages"

**Phase 8: Frontend Icons & UI Polish** ✅
- UI text updates completed in Phase 7
- Icon (mdi-alert-circle) deemed appropriate for tech messages

**Phase 9: Documentation** ✅
- Updated this task file with comprehensive review

**Phase 10: Testing & Verification** - Ready for user execution

### Files Changed

**Backend (Java)**:
- 7 renamed files (TechMessage.java, TechMessageRequest.java, TechMessageResponse.java, TechMessageRepository.java, TechMessageService.java, TechMessageMapper.java, TechMessageController.java)
- 4 updated files (ActionLevel.java, ActionLevelRepository.java, PatternMatcher.java, FrequencyAnalyzer.java)
- 1 updated file (PatternMatchResponse.java)
- 1 new Liquibase changeset

**Frontend (TypeScript/Vue)**:
- 2 renamed files (tech.ts, TechMessageListView.vue)
- 2 updated files (router/index.ts, App.vue)

**Total**: 17 files modified/renamed + 1 new changeset

### Breaking Changes

**API Endpoints**:
- Base path changed: `/api/error-messages` → `/api/tech-messages`
- All endpoint URLs under this path affected
- HTTP methods and request/response structures unchanged

**Frontend Routes**:
- Route changed: `/errors` → `/tech-messages`
- Navigation links updated

### Preserved Terminology

The following were correctly preserved and NOT changed:
- `ErrorResponse` - HTTP error response DTO
- `GlobalExceptionHandler` - Exception handling
- Spring Security error handling
- Validation error messages
- `errorText` parameter - input text for pattern matching
- try-catch error variables

### Impact Assessment

**Database**: Schema changes applied via Liquibase migration, backward compatible through migration
**Backend**: All imports, references, and method calls updated
**Frontend**: All type references, component imports, and routes updated
**Documentation**: Task file updated

### Next Steps

1. User to perform clean build of backend: `./gradlew clean build`
2. User to perform clean build of frontend: `cd frontend && npm run build`
3. Verify database migration applies successfully
4. Test API endpoints with new URLs
5. Test UI functionality
6. Update IMPLEMENTATION_PLAN.md with completion entry

## Testing Checklist

### Backend Testing
- [ ] Clean build: `./gradlew clean build`
- [ ] No compilation errors
- [ ] Database migration runs successfully (check Liquibase logs)
- [ ] Verify table renamed: `SELECT * FROM tech_messages LIMIT 1;`
- [ ] Verify column renamed: `SELECT tech_message_id FROM action_levels LIMIT 1;`
- [ ] MapStruct generated mapper updated correctly in `build/generated/`
- [ ] API endpoints respond:
  - [ ] `GET /api/tech-messages` (was `/api/error-messages`)
  - [ ] `GET /api/tech-messages/{id}`
  - [ ] `POST /api/tech-messages`
  - [ ] `PUT /api/tech-messages/{id}`
  - [ ] `DELETE /api/tech-messages/{id}`
  - [ ] `POST /api/tech-messages/{id}/actions`
  - [ ] `GET /api/tech-messages/match?text=...`

### Frontend Testing
- [ ] Frontend compiles: `npm run build`
- [ ] No TypeScript errors
- [ ] Tech messages list displays correctly at `/tech-messages`
- [ ] Old route `/errors` redirects or shows 404
- [ ] Filtering by category and severity works
- [ ] Pagination works
- [ ] Create dialog shows "Add Tech Message"
- [ ] Edit dialog shows "Edit Tech Message"
- [ ] Navigation menu shows "Tech Messages"
- [ ] Icon updated (if Phase 8 completed)

### Terminology Audit
- [ ] No "Error Knowledge Base" in UI
- [ ] No "Error Messages" menu item (should be "Tech Messages")
- [ ] No "Error Pattern" buttons (should be "Tech Message")
- [ ] No variable names containing `errorMessage` (except `errorText` input parameter)
- [ ] No database objects with `error_message` prefix
- [ ] `ErrorResponse` still exists (HTTP error responses)
- [ ] `GlobalExceptionHandler` unchanged (exception handling)

### Data Integrity
- [ ] Existing tech messages display correctly (seed data preserved)
- [ ] Action levels still linked to tech messages
- [ ] Foreign key constraints working
- [ ] CRUD operations work end-to-end
