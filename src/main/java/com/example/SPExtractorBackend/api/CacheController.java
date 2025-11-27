package com.example.SPExtractorBackend.api;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@CrossOrigin(origins = "https://localhost:8443", allowedHeaders = {"Authorization", "Content-Type"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class CacheController {

    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);
    
    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        logger.info("[CACHE STATS] Fetching cache statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            
            if (cache instanceof CaffeineCache caffeineCache) {
                Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                CacheStats cacheStats = nativeCache.stats();
                
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("size", nativeCache.estimatedSize());
                cacheInfo.put("hitCount", cacheStats.hitCount());
                cacheInfo.put("missCount", cacheStats.missCount());
                cacheInfo.put("hitRate", "%.2f%%".formatted(cacheStats.hitRate() * 100));
                cacheInfo.put("evictionCount", cacheStats.evictionCount());
                cacheInfo.put("loadSuccessCount", cacheStats.loadSuccessCount());
                
                stats.put(cacheName, cacheInfo);
                
                logger.info("[CACHE STATS] Cache '{}': {} entries, Hit rate: {}", 
                    cacheName, nativeCache.estimatedSize(), cacheInfo.get("hitRate"));
            }
        }
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        logger.warn("[CACHE CLEAR] Clearing all caches");
        
        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                logger.info("[CACHE CLEAR] Cleared cache: {}", cacheName);
            }
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All caches cleared successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        logger.warn("[CACHE CLEAR] Clearing cache: {}", cacheName);
        
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        Map<String, String> response = new HashMap<>();
        
        if (cache != null) {
            cache.clear();
            logger.info("[CACHE CLEAR] Cache '{}' cleared successfully", cacheName);
            response.put("message", "Cache '" + cacheName + "' cleared successfully");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("[WARNING] Cache '{}' not found", cacheName);
            response.put("error", "Cache '" + cacheName + "' not found");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
