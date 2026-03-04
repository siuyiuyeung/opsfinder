package com.igsl.opsfinder.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.igsl.opsfinder.dto.request.ApiKeyCreateRequest;
import com.igsl.opsfinder.dto.response.ApiKeyCreatedResponse;
import com.igsl.opsfinder.dto.response.ApiKeyResponse;
import com.igsl.opsfinder.dto.response.ApiKeyStatsResponse;
import com.igsl.opsfinder.dto.response.ApiKeyUsageLogResponse;
import com.igsl.opsfinder.entity.ApiKey;
import com.igsl.opsfinder.entity.ApiKeyUsageLog;
import com.igsl.opsfinder.entity.User;
import com.igsl.opsfinder.exception.RateLimitExceededException;
import com.igsl.opsfinder.exception.ResourceNotFoundException;
import com.igsl.opsfinder.mapper.ApiKeyMapper;
import com.igsl.opsfinder.repository.ApiKeyRepository;
import com.igsl.opsfinder.repository.ApiKeyUsageLogRepository;
import com.igsl.opsfinder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for API key management: creation, validation, rate limiting, and usage logging.
 */
@Service
public class ApiKeyService {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyService.class);
    private static final String KEY_PREFIX = "opsfinder_";
    private static final int KEY_BYTES = 32;

    @Value("${api-key.rate-limit-default-per-hour:1000}")
    private int defaultRateLimitPerHour;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ApiKeyUsageLogRepository usageLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    // Caffeine rate-limit counter cache: keyed by keyHash, 1-hour fixed window
    private final Cache<String, AtomicInteger> rateLimitCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    // --- Key Generation ---

    private String generatePlainTextKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[KEY_BYTES];
        random.nextBytes(bytes);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return KEY_PREFIX + encoded;
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    // --- CRUD ---

    @Transactional
    public ApiKeyCreatedResponse createApiKey(ApiKeyCreateRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        String plainTextKey = generatePlainTextKey();
        String keyHash = sha256Hex(plainTextKey);
        String keyPrefixDisplay = plainTextKey.substring(0, Math.min(16, plainTextKey.length()));

        int rateLimit = (request.getRateLimitPerHour() != null)
                ? request.getRateLimitPerHour()
                : defaultRateLimitPerHour;

        ApiKey apiKey = ApiKey.builder()
                .name(request.getName())
                .description(request.getDescription())
                .keyHash(keyHash)
                .keyPrefix(keyPrefixDisplay)
                .user(user)
                .active(true)
                .expiresAt(request.getExpiresAt())
                .rateLimitPerHour(rateLimit)
                .usageCount(0L)
                .build();

        apiKey = apiKeyRepository.save(apiKey);
        logger.info("Created API key '{}' for user '{}'", apiKey.getName(), currentUsername);

        ApiKeyCreatedResponse response = apiKeyMapper.toCreatedResponse(apiKey);
        response.setPlainTextKey(plainTextKey);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<ApiKeyResponse> listApiKeys(Pageable pageable) {
        return apiKeyRepository.findAll(pageable).map(apiKeyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ApiKeyResponse getApiKey(Long id) {
        ApiKey apiKey = findById(id);
        return apiKeyMapper.toResponse(apiKey);
    }

    @Transactional
    @CacheEvict(value = "apiKeys", key = "#id")
    public ApiKeyResponse revokeApiKey(Long id) {
        ApiKey apiKey = findById(id);
        apiKey.setActive(false);
        apiKey = apiKeyRepository.save(apiKey);
        logger.info("Revoked API key id={}", id);
        return apiKeyMapper.toResponse(apiKey);
    }

    @Transactional
    @CacheEvict(value = "apiKeys", key = "#id")
    public void deleteApiKey(Long id) {
        ApiKey apiKey = findById(id);
        apiKeyRepository.delete(apiKey);
        logger.info("Deleted API key id={}", id);
    }

    private ApiKey findById(Long id) {
        return apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API key not found: " + id));
    }

    // --- Usage Logs ---

    public Page<ApiKeyUsageLogResponse> getUsageLogs(
            Long id, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        // ensure key exists
        findById(id);
        Page<ApiKeyUsageLog> logs = (from != null && to != null)
                ? usageLogRepository.findByApiKeyIdAndRequestedAtBetween(id, from, to, pageable)
                : usageLogRepository.findByApiKeyId(id, pageable);
        return logs.map(apiKeyMapper::toUsageLogResponse);
    }

    public ApiKeyStatsResponse getStats() {
        ApiKeyStatsResponse stats = new ApiKeyStatsResponse();
        stats.setTotalKeys(apiKeyRepository.count());
        stats.setActiveKeys(apiKeyRepository.countActive());
        stats.setTotalRequests(usageLogRepository.countTotal());
        stats.setRequestsLast24h(usageLogRepository.countSince(LocalDateTime.now().minusHours(24)));
        return stats;
    }

    // --- Authentication & Rate Limiting ---

    /**
     * Lookup and validate an API key by its SHA-256 hash.
     * Result is cached for 5 minutes to reduce DB reads.
     */
    @Cacheable(value = "apiKeys", key = "#keyHash")
    public Optional<ApiKey> findAndValidateApiKey(String keyHash) {
        return apiKeyRepository.findByKeyHash(keyHash);
    }

    /**
     * Check rate limit for the given key hash.
     * Uses a Caffeine counter that expires after 1 hour.
     *
     * @throws RateLimitExceededException if the limit is exceeded
     */
    public void checkRateLimit(ApiKey apiKey) {
        AtomicInteger counter = rateLimitCache.get(
                apiKey.getKeyHash(),
                k -> new AtomicInteger(0));
        int current = counter.incrementAndGet();
        if (current > apiKey.getRateLimitPerHour()) {
            throw new RateLimitExceededException(
                    "Rate limit exceeded for API key: " + apiKey.getKeyPrefix());
        }
    }

    /**
     * Fire-and-forget: persist a usage log entry.
     */
    @Async("apiKeyAsyncExecutor")
    public void logUsage(Long apiKeyId, String endpoint, String httpMethod,
                         String clientIp, int responseStatus, long responseTimeMs) {
        try {
            ApiKeyUsageLog log = ApiKeyUsageLog.builder()
                    .apiKeyId(apiKeyId)
                    .endpoint(endpoint)
                    .httpMethod(httpMethod)
                    .clientIp(clientIp)
                    .responseStatus(responseStatus)
                    .responseTimeMs(responseTimeMs)
                    .build();
            usageLogRepository.save(log);
        } catch (Exception e) {
            logger.warn("Failed to persist usage log for api key id={}: {}", apiKeyId, e.getMessage());
        }
    }

    /**
     * Fire-and-forget: update lastUsedAt and increment usageCount.
     */
    @Async("apiKeyAsyncExecutor")
    public void updateLastUsed(Long apiKeyId) {
        try {
            apiKeyRepository.findById(apiKeyId).ifPresent(key -> {
                key.setLastUsedAt(LocalDateTime.now());
                key.setUsageCount(key.getUsageCount() + 1);
                apiKeyRepository.save(key);
            });
        } catch (Exception e) {
            logger.warn("Failed to update lastUsedAt for api key id={}: {}", apiKeyId, e.getMessage());
        }
    }

    /**
     * Compute SHA-256 hex of the incoming raw key string.
     */
    public String hashKey(String rawKey) {
        return sha256Hex(rawKey);
    }
}
