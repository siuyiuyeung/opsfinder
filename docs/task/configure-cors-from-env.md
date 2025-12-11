# Task: Configure CORS to Use ALLOWED_ORIGINS Environment Variable

## Analysis

After reading the codebase, I've identified the following issues:

1. **application.yml** (development) uses `CORS_ALLOWED_ORIGINS` environment variable
2. **application-prod.yml** (production) uses `ALLOWED_ORIGINS` environment variable
3. **SecurityConfig.java** has hardcoded CORS origins and doesn't read from configuration
4. The YAML files define CORS configuration that's not being used

**Current State:**
- `SecurityConfig.java:117-121` - Hardcoded origins: localhost:3000, localhost:5173, localhost:8080
- `application.yml:70` - `cors.allowed-origins: ${CORS_ALLOWED_ORIGINS:...}`
- `application-prod.yml:60-64` - Full CORS config with `${ALLOWED_ORIGINS:...}`

**Desired State:**
- Use consistent environment variable naming across all profiles
- SecurityConfig reads CORS configuration from application.yml
- Support comma-separated list of origins from environment variable

## Todo List

- [ ] Unify environment variable naming (use `ALLOWED_ORIGINS` for both dev and prod)
- [ ] Update application.yml to use `ALLOWED_ORIGINS` instead of `CORS_ALLOWED_ORIGINS`
- [ ] Add all CORS configuration properties to application.yml (methods, headers, credentials)
- [ ] Create CorsProperties configuration class to bind CORS settings from YAML
- [ ] Update SecurityConfig.java to inject and use CorsProperties
- [ ] Update .env.example with documentation for ALLOWED_ORIGINS

## Implementation Approach

**Simple and Minimal Changes:**

1. **Create `CorsProperties.java`** - Configuration properties class with `@ConfigurationProperties("cors")`
   - Fields: allowedOrigins (List<String>), allowedMethods, allowedHeaders, allowCredentials, maxAge

2. **Update application.yml** - Change `CORS_ALLOWED_ORIGINS` to `ALLOWED_ORIGINS` and add missing properties

3. **Update SecurityConfig.java** - Inject CorsProperties and use its values in corsConfigurationSource()

4. **Update .env.example** - Document the ALLOWED_ORIGINS usage

This approach is clean, type-safe, and follows Spring Boot best practices.

## Review

### Changes Completed

All tasks have been successfully completed. The CORS configuration is now fully environment-driven and follows Spring Boot best practices.

#### 1. Created CorsProperties.java
- **Location**: `src/main/java/com/igsl/opsfinder/config/CorsProperties.java`
- **Purpose**: Type-safe configuration properties class annotated with `@ConfigurationProperties("cors")`
- **Fields**: allowedOrigins, allowedMethods, allowedHeaders, exposedHeaders, allowCredentials, maxAge
- **Benefits**: Automatic binding from YAML, IDE autocomplete support, validation support

#### 2. Updated application.yml
- **Changed**: `CORS_ALLOWED_ORIGINS` → `ALLOWED_ORIGINS` (unified naming)
- **Added**: Complete CORS configuration (methods, headers, exposed-headers, credentials, max-age)
- **Default**: Development-friendly localhost origins (ports 3000, 5173, 8080)

#### 3. Updated application-prod.yml
- **Added**: Missing properties (exposed-headers, max-age, OPTIONS method, X-Requested-With header)
- **Maintained**: Production-focused security with restrictive defaults
- **Default**: `https://opsfinder.example.com` (requires override in production)

#### 4. Updated SecurityConfig.java
- **Added**: Injection of CorsProperties via @Autowired
- **Updated**: corsConfigurationSource() method to use properties instead of hardcoded values
- **Removed**: Hardcoded origins (localhost:3000, localhost:5173, localhost:8080)
- **Improved**: Documentation explaining the configuration source

#### 5. Updated .env.example
- **Enhanced**: Comprehensive documentation for ALLOWED_ORIGINS
- **Added**: Format explanation (comma-separated, no spaces)
- **Added**: Environment-specific examples (single domain, multiple domains)
- **Added**: Security warning about wildcard usage

### How to Use

**Development (Local)**:
- No changes needed - defaults work with common dev ports
- Optionally set in `.env`: `ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173`

**Production (Docker/Deployment)**:
1. Copy `.env.example` to `.env`
2. Set `ALLOWED_ORIGINS=https://your-actual-domain.com`
3. Add multiple domains if needed: `ALLOWED_ORIGINS=https://domain1.com,https://domain2.com`

**Testing**:
- Start the application and verify CORS headers in browser DevTools
- Successful requests should show `Access-Control-Allow-Origin` header
- Blocked requests will show CORS errors in console

### Technical Benefits

1. **Single Source of Truth**: CORS config in YAML files only
2. **Environment-Specific**: Different configs for dev vs prod profiles
3. **Type-Safe**: Spring Boot validates configuration at startup
4. **Maintainable**: No code changes needed to adjust CORS settings
5. **Secure by Default**: Production profile requires explicit origin configuration
6. **Flexible**: Supports comma-separated list for multiple origins
