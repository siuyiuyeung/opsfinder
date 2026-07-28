# Task: Fix Expired Token Returning 403 Instead of 401 (Silent Auth Failure)

## Analysis

### Symptom
Frontend calls an API with an expired JWT. Backend responds **403**. The axios response
interceptor ignores it — no token refresh, no logout, no redirect. The user stays on the
page seeing stale data and silently failing actions, with no idea their session ended.

### Root Cause (backend)

`SecurityConfig.java` (`filterChain`, lines 57–95) never configures an
`AuthenticationEntryPoint`. With no `formLogin()` / `httpBasic()` registered, Spring
Security falls back to `Http403ForbiddenEntryPoint`, so **every unauthenticated request
gets 403**.

The path an expired token takes:

1. `JwtAuthenticationFilter.doFilterInternal` (line 43) calls `tokenProvider.validateToken(jwt)`.
2. `JwtTokenProvider` (line 149) catches `ExpiredJwtException` and returns `false`.
3. The filter sets no `Authentication`, swallows the failure, and calls `filterChain.doFilter(...)`.
4. Request reaches `.anyRequest().authenticated()` (line 88) as anonymous.
5. `ExceptionTranslationFilter` invokes the default entry point → **403**.

This conflates two distinct conditions:

| Status | Correct meaning | Client action |
|--------|-----------------|---------------|
| 401 Unauthorized | Missing / invalid / expired credentials | Re-authenticate (refresh, else login) |
| 403 Forbidden | Authenticated, but lacks permission | Do NOT retry — show "access denied" |

Note `ApiKeyAuthenticationFilter.sendUnauthorized` (line 110) already returns **401**
for a bad API key. The JWT path is the inconsistent one.

### Root Cause (frontend)

`frontend/src/services/api.ts` lines 72–82:

```ts
function isAuthError(error: any): boolean {
  const status = error.response?.status
  if (status === 401) return true

  // 403 with no token in localStorage indicates session issue
  if (status === 403 && !localStorage.getItem('accessToken')) {
    return true
  }
  return false
}
```

An expired token **is still present** in `localStorage`, so the 403 branch is `false`,
`isAuthError` returns `false`, and the interceptor falls straight through to
`Promise.reject(error)` at line 146. The refresh-and-retry queue (lines 95–144) and
`handleSessionExpired()` (lines 39–49) never run.

This heuristic is also wrong in the other direction: a genuine permission denial
(e.g. a VIEWER hitting an ADMIN endpoint) would be misread as a session problem if the
token happened to be absent, triggering a spurious refresh/logout.

### What already works (no new UI needed)

The full session-expiry experience is already built and merely never triggered:

- `handleSessionExpired()` — `api.ts:39-49` — sets flag, clears session, redirects to Login
- `authStore.setSessionExpired()` / `clearSession()` — `stores/auth.ts:155-172`
- Session-expired banner — `views/LoginView.vue:38` (`v-if="authStore.sessionExpired"`)
- Flag reset on successful login — `stores/auth.ts:88`
- Refresh-token queue to avoid concurrent-refresh races — `api.ts:16-34, 96-108`

So the fix is to make the trigger fire correctly, not to build new behavior.

### Desired behavior

Expired access token → interceptor detects 401 → attempts `/auth/refresh`:
- **refresh succeeds** → original request retried transparently, user never interrupted
- **refresh fails / no refresh token** → session cleared, redirect to Login, banner shown

Real permission denial (403) → rejected to the caller as a normal error, no logout.

### Compatibility check

- `/api/auth/login` is `permitAll`, so it never reaches the entry point. The
  "pending approval" **403** from `GlobalExceptionHandler.handleUserNotApprovedException`
  (line 157–169) is unaffected, and `stores/auth.ts:92` keeps working.
- `@PreAuthorize` denials for an *authenticated* user route to `AccessDeniedHandler`, not
  the entry point → still 403. Correct.
- API-key clients already receive 401 from `ApiKeyAuthenticationFilter`; behavior unchanged.

## Todo List

- [x] Create `src/main/java/com/igsl/opsfinder/security/JwtAuthenticationEntryPoint.java` —
      implements `AuthenticationEntryPoint`, writes **401** with an `ErrorResponse` JSON body
      (`timestamp`, `status`, `error`, `message`, `path`) to match existing error shape
- [x] Create `src/main/java/com/igsl/opsfinder/security/RestAccessDeniedHandler.java` —
      implements `AccessDeniedHandler`, writes **403** with the same `ErrorResponse` shape
      so permission denials stay distinct and consistently formatted
- [x] Wire both into `SecurityConfig.filterChain` via
      `.exceptionHandling(e -> e.authenticationEntryPoint(...).accessDeniedHandler(...))`
- [x] Simplify `isAuthError` in `frontend/src/services/api.ts` to `status === 401`
      (delete the localStorage-based 403 branch) and update the doc comment
- [x] Verify `handleSessionExpired` is reached when refresh fails, including the
      no-refresh-token case (`api.ts:115-117`)
- [x] Unit test: entry point returns 401 with correct JSON body
- [x] Unit test: access denied handler returns 403 with correct JSON body
- [x] Compile backend (`./gradlew compileJava`) and typecheck frontend
- [ ] **Not done — blocked:** full-context integration tests (expired JWT → 401,
      VIEWER on ADMIN endpoint → 403, no header → 401) and manual end-to-end check.
      See "Verification status" below.

## Scope

3 files modified, 2 added (plus 2 test files).
No changes to controllers, services, or UI components.

## Review

### Changes made

**Added — `security/JwtAuthenticationEntryPoint.java`**
Implements `AuthenticationEntryPoint`. Returns **401** with an `ErrorResponse` JSON body.
Registering it replaces Spring's default `Http403ForbiddenEntryPoint`, which was the
source of the wrong status code.

**Added — `security/RestAccessDeniedHandler.java`**
Implements `AccessDeniedHandler`. Returns **403** with the same `ErrorResponse` shape,
keeping permission denials clearly distinct from authentication failures.

**Modified — `config/SecurityConfig.java`**
Added both beans and an `.exceptionHandling(...)` block between `sessionManagement` and
`authorizeHttpRequests` (lines 71–75). No other security rules touched.

**Modified — `frontend/src/services/api.ts`**
`isAuthError` reduced to `error.response?.status === 401`. The
`403 && !localStorage.getItem('accessToken')` branch is deleted — it was the reason an
expired token produced a silent failure, and it could also have misfired on genuine
permission errors.

**Added — 2 unit tests** covering both handlers (status, content type, and every JSON field).

### Note for future work

Both handlers use `tools.jackson.databind.ObjectMapper`, not
`com.fasterxml.jackson.databind.ObjectMapper`. Spring Boot 4.0 ships **Jackson 3**
(`tools.jackson.core:jackson-databind:3.0.2`); the Jackson 2 `com.fasterxml` databind
package is not on the compile classpath, only `jackson-annotations`.

### Resulting behavior

| Scenario | Before | After |
|----------|--------|-------|
| Expired access token, valid refresh token | 403, silently ignored | 401 → silent refresh → request retried, user uninterrupted |
| Expired access + expired/missing refresh | 403, silently ignored | 401 → refresh fails → session cleared → redirect to Login with expiry banner |
| No Authorization header | 403 (frontend treated as session issue) | 401 → same refresh-then-logout flow |
| VIEWER hits ADMIN endpoint | 403, could be misread as session issue | 403 → rejected to caller, no refresh, no logout |
| Login with pending-approval account | 403 + "pending approval" message | Unchanged (`/api/auth/login` is `permitAll`, never reaches the entry point) |
| Invalid API key | 401 from `ApiKeyAuthenticationFilter` | Unchanged |

No new frontend UI was required — `handleSessionExpired`, `authStore.clearSession`, the
refresh queue, and the LoginView expiry banner all already existed and are now reachable.

### Verification status

Done:
- `./gradlew compileJava` — passes
- `./gradlew test --tests "com.igsl.opsfinder.security.*"` — 2/2 pass
- `npm run type-check` — 11 errors, all pre-existing in `vuetify.ts`, `DeviceListView.vue`,
  `ExcelFileView.vue`, `TechMessageListView.vue`, `vite.config.ts`. **Zero in `api.ts`.**

Not done, and why:
- `OpsFinderApplicationTests.contextLoads` **already failed before this change** —
  Liquibase cannot reach PostgreSQL (`java.net.ConnectException`). There is no
  `src/test/resources/application-test.yml`, so `@SpringBootTest` requires a live database
  even though H2 is on the test classpath.
- Docker daemon is not running and the app is not running locally, so the full-context
  integration tests and the manual browser check could not be executed.
- Consequence: the two handlers are verified in isolation, and the `SecurityConfig` wiring
  is verified by compilation and inspection only — **it has not been exercised against a
  live request.**

Recommended follow-up (separate task): add `src/test/resources/application-test.yml` with an
H2 datasource so `@SpringBootTest` runs without PostgreSQL. That would fix the pre-existing
`contextLoads` failure and enable the integration tests listed above.
