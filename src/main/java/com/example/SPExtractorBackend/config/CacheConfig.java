package com.example.SPExtractorBackend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    CacheManager cacheManager() {
        logger.info("[CACHE] Initializing Cache Manager with Caffeine");
        
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("sites", "drives", "files");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
                .evictionListener((key, value, cause) -> {
                    logger.info("[CACHE EVICTION] Key: {} evicted due to: {}", key, cause);
                }));
        
        logger.info("[CACHE] Cache Manager initialized with caches: sites, drives, files");
        logger.info("[CACHE] Cache config: max 10000 entries, 24h expiration");
        
        return cacheManager;
    }
}
