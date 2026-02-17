package com.healthmetrics.tracker.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration using in-memory ConcurrentMapCache.
 * For production, consider using Redis or Caffeine for cache eviction policies.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String FACILITIES_CACHE = "facilities";
    public static final String INDICATORS_CACHE = "indicators";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(FACILITIES_CACHE, INDICATORS_CACHE);
    }
}
