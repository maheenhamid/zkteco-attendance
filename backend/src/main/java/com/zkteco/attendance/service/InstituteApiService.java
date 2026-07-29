package com.zkteco.attendance.service;

import com.zkteco.attendance.dto.institute.ClassDTO;
import com.zkteco.attendance.dto.institute.ExternalApiEnvelope;
import com.zkteco.attendance.dto.institute.InstituteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thin, cached proxy over the external Shebashikkha institute/class APIs.
 * There is intentionally no local institute/class table - these calls are
 * the single source of truth, per the system's multi-tenant design.
 */
@Service
@Slf4j
public class InstituteApiService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final long cacheTtlSeconds;

    private final Map<String, CacheEntry<List<InstituteDTO>>> instituteCache = new ConcurrentHashMap<>();
    private final Map<Long, CacheEntry<List<ClassDTO>>> classCache = new ConcurrentHashMap<>();

    public InstituteApiService(RestTemplate restTemplate,
                                @Value("${app.institute-api.base-url}") String baseUrl,
                                @Value("${app.institute-api.cache-ttl-seconds}") long cacheTtlSeconds) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public List<InstituteDTO> listInstitutes() {
        CacheEntry<List<InstituteDTO>> cached = instituteCache.get("all");
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        try {
            ResponseEntity<ExternalApiEnvelope<InstituteDTO>> response = restTemplate.exchange(
                    baseUrl + "/institute/list", HttpMethod.GET, HttpEntity.EMPTY,
                    new ParameterizedTypeReference<ExternalApiEnvelope<InstituteDTO>>() {
                    });

            List<InstituteDTO> institutes = response.getBody() != null ? response.getBody().getItem() : Collections.emptyList();
            instituteCache.put("all", new CacheEntry<>(institutes, cacheTtlSeconds));
            return institutes;
        } catch (RestClientException e) {
            log.error("Failed to fetch institute list from external API", e);
            if (cached != null) {
                return cached.value;
            }
            return Collections.emptyList();
        }
    }

    public List<ClassDTO> listClasses(Long instituteId) {
        CacheEntry<List<ClassDTO>> cached = classCache.get(instituteId);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }

        try {
            ResponseEntity<ExternalApiEnvelope<ClassDTO>> response = restTemplate.exchange(
                    baseUrl + "/class/list?instituteId={id}", HttpMethod.GET, HttpEntity.EMPTY,
                    new ParameterizedTypeReference<ExternalApiEnvelope<ClassDTO>>() {
                    }, instituteId);

            List<ClassDTO> classes = response.getBody() != null ? response.getBody().getItem() : Collections.emptyList();
            classCache.put(instituteId, new CacheEntry<>(classes, cacheTtlSeconds));
            return classes;
        } catch (RestClientException e) {
            log.error("Failed to fetch class list for institute {} from external API", instituteId, e);
            if (cached != null) {
                return cached.value;
            }
            return Collections.emptyList();
        }
    }

    private static final class CacheEntry<T> {
        private final T value;
        private final Instant expiresAt;

        private CacheEntry(T value, long ttlSeconds) {
            this.value = value;
            this.expiresAt = Instant.now().plusSeconds(ttlSeconds);
        }

        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
