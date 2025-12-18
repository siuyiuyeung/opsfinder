# OpsFinder Implementation Plan

## Project Overview

**OpsFinder** is a Production Operations Knowledge Base & Incident Tracking System - a mobile-first PWA web application for managing device/VM inventory, tech message knowledge base, and production incident tracking with real-time notifications.

**Current State**: Fresh Spring Boot 4.0.0 project with minimal setup (only starter dependencies)

**Target State**: Full-featured mobile web app with offline support, real-time notifications, and comprehensive incident tracking

---

## Technology Stack

### Backend
- **Framework**: Spring Boot 4.0.0 + Java 21
- **Database**: PostgreSQL 15+ (full-text search with ts_vector, JSONB support)
- **Security**: Spring Security + JWT authentication
- **Real-time**: Spring WebSocket (STOMP protocol)
- **Migration**: Liquibase
- **Cache**: Caffeine (in-memory)
- **Utilities**: Lombok, MapStruct, Hibernate Validator

### Frontend
- **Framework**: Vue 3 + Composition API + TypeScript
- **Build**: Vite
- **UI Library**: Vuetify 3 (Material Design, mobile-first)
- **State**: Pinia
- **Routing**: Vue Router
- **PWA**: Vite PWA Plugin + Service Worker
- **Offline Storage**: IndexedDB (Dexie.js)
- **Real-time**: SockJS + STOMP client
- **HTTP**: Axios

---

## Architecture Design

### System Architecture
```
Mobile Browser (PWA)
    ↓ (REST + WebSocket)
Spring Boot Backend
    ↓
PostgreSQL 15+ (Full-text Search + JSONB)
```

### Layered Architecture
```
Controllers → Services → Repositories → Entities
```

### Key Features
1. **Device/VM Inventory** (<10K devices) - Full-text keyword search
2. **Tech Message Knowledge Base** - Regex pattern matching, multi-level actions
3. **Incident Tracking** - Production error logging with staff responses
4. **Real-time Notifications** - WebSocket push for new incidents
5. **PWA Offline Support** - Service Worker + IndexedDB with sync queue
6. **Role-based Access** - Admin, Operator, Viewer roles

---

## Database Schema

### Tables
1. **users** - Authentication (username, password, role)
2. **devices** - VM/device inventory with search_vector (ts_vector) for full-text search
3. **tech_messages** - Tech message patterns (regex), categories, severity
4. **action_levels** - Multi-level actions based on occurrence frequency
5. **incidents** - Production error occurrences with JSONB for error variables

### Key Indexes
- GIN index on `devices.search_vector` for full-text search
- B-tree indexes on foreign keys and common filter fields (zone, type, severity, occurred_at)
- JSONB GIN index on `incidents.error_variables`

---

## Implementation Phases

### Phase 1: Foundation & Authentication (Week 1) ✓ CURRENT PHASE

**Objective**: Set up project infrastructure, PostgreSQL, and JWT authentication

**Backend Tasks**:
1. ✓ Update `build.gradle` with all dependencies
2. ✓ Configure `application.yml` (PostgreSQL, JPA, Liquibase, JWT)
3. ✓ Create Liquibase changeset: `changelog-001-users.yaml`
4. ✓ Implement entities: `BaseEntity.java`, `User.java`
5. ✓ Implement security: `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig`
6. ✓ Implement authentication: `AuthService`, `AuthController`, DTOs
7. ✓ Create global exception handler
8. ✓ Write authentication tests

**Frontend Tasks**:
1. ✅ Initialize Vue 3 + Vite + TypeScript project
2. ✅ Install dependencies (Vuetify, Pinia, Vue Router, etc.)
3. ✅ Configure Vuetify and Vue Router
4. ✅ Create auth store, Axios instance, LoginView
5. ✅ Test end-to-end authentication flow

**Critical Files**:
- `build.gradle`
- `src/main/resources/application.yml`
- `src/main/java/com/igsl/opsfinder/config/SecurityConfig.java`
- `src/main/java/com/igsl/opsfinder/security/JwtTokenProvider.java`
- `frontend/src/stores/auth.ts`

---

### Phase 2: Device Management (Week 2) ✅ COMPLETE

**Objective**: Implement device CRUD with PostgreSQL full-text search

**Backend Tasks**: ✅ Complete
**Frontend Tasks**: ✅ Complete (full CRUD, search, filters, pagination working)

[See detailed tasks in full plan below...]

---

### Phase 3: Tech Message Knowledge Base (Week 3) ✅ COMPLETE

**Objective**: Build tech message KB with regex pattern matching

**Backend Tasks**: ✅ Complete
**Frontend Tasks**: ✅ Complete (full CRUD UI with action levels management)

[See detailed tasks in full plan below...]

---

### Phase 4: Incident Tracking & Real-time Notifications (Week 4) ⏳ NOT STARTED

**Objective**: Implement incident logging with WebSocket push notifications

**Backend Tasks**: ⏳ Pending
**Frontend Tasks**: ⏳ Stubbed (routes exist, components show "Coming Soon")

[See detailed tasks in full plan below...]

---

### Phase 5: PWA & Offline Support (Week 5) ⏳ NOT STARTED

**Objective**: Enable offline-first architecture with service worker and sync queue

**Backend Tasks**: ⏳ Pending
**Frontend Tasks**: ⏳ Not started (dependencies installed but not configured)

[See detailed tasks in full plan below...]

---

## Progress Tracking

### Current Status
- **Active Phase**: Phase 3 - Tech Message Knowledge Base (partial) / Phase 4 - Incident Tracking (next)
- **Completion**: Phases 1-2 complete, Phase 3 read-only complete
- **Next Steps**: Complete Tech Message KB forms OR implement Incident tracking

### Phase Completion Checklist

#### Phase 1: Foundation & Authentication ✅ COMPLETE
- [x] User can login with username/password
- [x] JWT token is generated and validated
- [x] Protected endpoints require authentication

#### Phase 2: Device Management ✅ COMPLETE
- [x] User can search devices by any keyword
- [x] Search results are ranked by relevance
- [x] User can create/edit/delete devices (role-based)

#### Phase 3: Error Knowledge Base ⏳ PARTIAL
- [x] View error patterns with severity and categories
- [x] Filter by category and severity
- [x] Pagination and expansion panels
- [ ] Admin can create error patterns with regex (form stubbed)
- [ ] Admin can edit existing error patterns (form stubbed)

#### Phase 4: Incident Tracking
- [ ] User can create incident with error matching
- [ ] Staff can log actions/responses
- [ ] Real-time notifications appear for new incidents

#### Phase 5: PWA & Offline
- [ ] App works offline (read cached data)
- [ ] User can create incidents offline
- [ ] Operations sync automatically when online
- [ ] PWA can be installed on mobile devices

---

## Timeline Summary

- **Week 1**: Foundation & Authentication ✅ COMPLETE
- **Week 2**: Device Management ✅ COMPLETE
- **Week 3**: Error Knowledge Base ⏳ IN PROGRESS (read-only done)
- **Week 4**: Incident Tracking & Notifications ← NEXT UP
- **Week 5**: PWA & Offline Support

**Total Duration**: 5 weeks for MVP

---

## Quick Reference

### Key Technologies
- **Backend**: Spring Boot 4.0 + Java 21 + PostgreSQL 15+
- **Frontend**: Vue 3 + TypeScript + Vuetify 3
- **Security**: JWT with Spring Security
- **Real-time**: WebSocket (STOMP over SockJS)
- **Offline**: Service Worker + IndexedDB

### Important Patterns
- **Full-text Search**: PostgreSQL `ts_vector` + GIN index
- **Error Matching**: Java regex with Pattern.compile()
- **Action Levels**: Frequency-based determination
- **Offline Sync**: Queue + last-write-wins conflict resolution

### Project Structure
```
OpsFinder/
├── src/main/java/com/igsl/opsfinder/    (Backend - Spring Boot)
├── src/main/resources/                  (Config + Liquibase)
├── frontend/                            (Frontend - Vue 3)
├── CLAUDE.md                            (Project instructions)
└── IMPLEMENTATION_PLAN.md              (This file)
```

---

## Implementation Notes

### What's Working (2025-12-16)
- ✅ Full authentication flow with JWT token refresh
- ✅ Device CRUD with full-text search, filters, and pagination
- ✅ Tech Message KB full CRUD interface with action levels management
- ✅ Role-based access control (Admin, Operator, Viewer)
- ✅ Docker multi-stage build with Nginx reverse proxy
- ✅ Production-ready deployment configuration
- ✅ **CORS environment configuration** - ALLOWED_ORIGINS now reads from environment variables (2025-12-11)
  - See: `docs/task/configure-cors-from-env.md`
- ✅ **Fixed SQL syntax error in full-text search** - Removed duplicate ORDER BY clause (2025-12-12)
  - See: `docs/task/fix-duplicate-order-by-sql-error.md`
- ✅ **Renamed "Error Knowledge Base" to "Tech Message"** - Comprehensive refactoring removing all "error" terminology from domain code (2025-12-15)
  - See: `docs/task/rename-error-to-tech-message.md`
- ✅ **Tech Message CRUD Forms** - Complete create/edit/delete UI for tech messages and action levels (2025-12-15)
  - See: `docs/task/implement-tech-message-forms.md`
- ✅ **Quick Search Interface with Fuzzy Matching** - Optimized for daily incident lookup with hybrid fuzzy keyword + exact pattern search (2025-12-16)
  - See: `docs/task/quick-search-implementation.md`

- ✅ **Fixed PostgreSQL Type Inference Bug in Fuzzy Search** - Resolved "function lower(bytea) does not exist" error by converting JPQL to native SQL with explicit type casting (2025-12-18)
  - See: `docs/task/fix-pattern-column-type.md`
  - See: `docs/task/enhance-ux-for-incident-search.md`
  - Features: Search-as-you-type, occurrence-based actions, search history, copy-to-clipboard
  - Backend: Fuzzy multi-keyword search with relevance scoring
  - Frontend: Two-mode interface (Quick Search + Management)
- ✅ **Search Input Single-Line Display** - Changed search textarea from 4 rows to 1 row for more compact UI (2025-12-18)
  - See: `docs/task/search-input-single-line.md`
- ✅ **Improved Search Results Display** - Enhanced readability with collapsible sections and vertical chip layout (2025-12-18)
  - See: `docs/task/show-pattern-in-search-results.md`
  - See: `docs/task/improve-action-level-display.md`
  - Features: Pattern visible for all matches, collapsible details, full-width action text, vertical chip layout
  - UX: Cleaner default view, better mobile experience, improved action text readability
- ✅ **Tech Messages Grouped by Category and Severity** - Reorganized management view with hierarchical grouping (2025-12-18)
  - See: `docs/task/group-tech-messages-by-category-severity.md`
  - Features: 3-level nested structure (Category → Severity → Messages), alphabetically sorted categories, severity-prioritized display (CRITICAL → HIGH → MEDIUM → LOW)
  - UX: Better organization, clear hierarchy, improved navigation with message counts at each level

### What's Stubbed
- ⏳ Incident tracking views (placeholder "Coming Soon" messages)

### What's Not Started
- ⏳ WebSocket real-time notifications (packages installed)
- ⏳ Service Worker PWA configuration (plugin installed)
- ⏳ IndexedDB offline sync queue (Dexie installed)

### Recommended Next Steps
1. **Phase 4 - Incident Tracking**: Build incident logging with real-time WebSocket notifications
2. **Phase 5 - PWA & Offline**: Configure service worker, IndexedDB sync queue, offline-first architecture
3. **UX Polish**: Add keyboard shortcuts (Ctrl+K), dark mode, mobile PWA optimizations
4. **Analytics**: Track most-searched issues, common patterns, response times

---

## Notes

- This plan is saved in the project directory for version control
- Track progress by checking off items in the Phase Completion Checklist
- Refer to the full plan details in this file for specific implementation steps
- Update progress markers (✓, ⏳, ⏸️) as work progresses

**Last Updated**: 2025-12-16
