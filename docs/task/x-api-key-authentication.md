# Task: X-API-Key Authentication Feature

## Analysis

OpsFinder uses stateless JWT authentication. External clients (CI pipelines, scripts,
integrations) need a second authentication method that doesn't require a session.
Added X-API-Key authentication following modern API key best practices (Stripe/GitHub
style): opaque high-entropy tokens, hash-only storage, one-time plaintext display on
creation, per-key rate limiting, and full usage audit logging.

## Todo List

- [x] 1. Create Liquibase migration `changelog-006-api-keys.yaml`
- [x] 2. Update `db.changelog-master.yaml` to include the new migration
- [x] 3. Create `ApiKey.java` entity (extends BaseEntity, EAGER user fetch)
- [x] 4. Create `ApiKeyUsageLog.java` entity (does NOT extend BaseEntity — append-only + async context)
- [x] 5. Create `ApiKeyRepository.java`
- [x] 6. Create `ApiKeyUsageLogRepository.java`
- [x] 7. Create `RateLimitExceededException.java`
- [x] 8. Create DTOs: `ApiKeyCreateRequest`, `ApiKeyResponse`, `ApiKeyCreatedResponse`, `ApiKeyUsageLogResponse`, `ApiKeyStatsResponse`
- [x] 9. Create `ApiKeyMapper.java` (MapStruct)
- [x] 10. Create `AsyncConfig.java` (enables `@EnableAsync` + `@EnableCaching` + thread pool)
- [x] 11. Create `ApiKeyService.java` (key generation, CRUD, rate limiting, usage logging)
- [x] 12. Create `ApiKeyAuthenticationFilter.java` (Spring Security filter)
- [x] 13. Create `ApiKeyController.java`
- [x] 14. Update `SecurityConfig.java` — register filter + expose `X-API-Key` header in CORS + permit static assets
- [x] 15. Update `application.yml` — add `X-API-Key` to `cors.allowed-headers` + api-key config section
- [x] 16. Update `GlobalExceptionHandler.java` — handle `RateLimitExceededException` → 429
- [x] 17. Create `SpaController.java` — catch-all GET → `index.html` for SPA refresh support
- [x] 18. Frontend: `src/types/apikey.ts` — TypeScript interfaces
- [x] 19. Frontend: `src/services/apikey.service.ts` — API calls
- [x] 20. Frontend: `src/views/ApiKeyManagementView.vue` — admin management page
- [x] 21. Frontend: router + App.vue — add `/key-management` route and nav item

## Review

### Backend changes

**New files:**
- `changelog-006-api-keys.yaml` — creates `api_keys` and `api_key_usage_logs` tables with indexes and FK constraints
- `ApiKey.java` — entity with SHA-256 hash storage, prefix display, rate limit config, EAGER user fetch to prevent LazyInitializationException
- `ApiKeyUsageLog.java` — append-only log entity, no BaseEntity (async context has no SecurityContext)
- `ApiKeyRepository.java` / `ApiKeyUsageLogRepository.java`
- `RateLimitExceededException.java`
- DTOs: `ApiKeyCreateRequest`, `ApiKeyResponse`, `ApiKeyCreatedResponse` (includes `plainTextKey`), `ApiKeyUsageLogResponse`, `ApiKeyStatsResponse`
- `ApiKeyMapper.java` — MapStruct, maps `user.username` and `user.id`
- `AsyncConfig.java` — `@EnableAsync` + `@EnableCaching`, dedicated `apiKeyAsyncExecutor` thread pool (2-4 threads, queue 500)
- `ApiKeyService.java` — key generation (`opsfinder_` + 32-byte base64url), SHA-256 hashing, Caffeine rate-limit counter (1-hr expiry), `@Cacheable("apiKeys")` on lookup, `@Async` fire-and-forget logging and lastUsedAt update
- `ApiKeyAuthenticationFilter.java` — runs before JWT filter; if no `X-API-Key` header passes through; validates hash, checks active/expiry/rate-limit, sets SecurityContext, wraps chain in try-finally for usage logging
- `ApiKeyController.java` — `POST/GET/PATCH/DELETE /api/admin/api-keys/**`, all require ADMIN role
- `SpaController.java` — forwards non-API GET requests to `index.html` for SPA routing on refresh; regex uses `$` anchor (`(?!api$|...)`) to correctly exclude `/api/**` while allowing `/key-management`

**Modified files:**
- `SecurityConfig.java` — added `ApiKeyAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`; added static asset paths to `permitAll()`
- `application.yml` — added `X-API-Key` to `cors.allowed-headers`; added `PATCH` to allowed methods; added `api-key:` config section
- `GlobalExceptionHandler.java` — `RateLimitExceededException` → HTTP 429 with `Retry-After: 3600` header
- `db.changelog-master.yaml` — added changelog-006 include
- `build.gradle` — changed PostgreSQL driver from `runtimeOnly` to `implementation` (required for IntelliJ run configuration)

### Frontend changes

**New files:**
- `src/types/apikey.ts` — TypeScript interfaces for all API key DTOs + `PageResponse<T>`
- `src/services/apikey.service.ts` — all API calls (create, list, revoke, delete, usage logs, stats)
- `src/views/ApiKeyManagementView.vue` — stats cards, keys table, create dialog (one-time plaintext display with clipboard copy), revoke/delete confirmation dialogs, paginated usage logs dialog

**Modified files:**
- `src/router/index.ts` — added `/key-management` route (admin-only)
- `src/App.vue` — added "API Keys" nav item pointing to `/key-management`

### Key design decisions

- Route is `/key-management` (not `/api-keys`) — avoids SpaController regex conflict with the `/api/**` exclusion pattern
- `FetchType.EAGER` on `ApiKey.user` — always needed in mapper and filter; avoids detached-entity issues with `@Cacheable`
- Both JWT and ApiKey filters anchor to `UsernamePasswordAuthenticationFilter` — custom filter classes have no registered Spring Security order
- `@Async` usage logging is fire-and-forget; failures are logged but never surface to the caller
