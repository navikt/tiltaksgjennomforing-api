package no.nav.tag.tiltaksgjennomforing.infrastruktur.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// @Configuration
// @EnableCaching
public class CaffeineCachingConfig {

    public final static String STS_CACHE = "sts_cache";
    public final static String ABAC_CACHE = "abac_cache";
    public static final String AXSYS_CACHE = "axsys_cache";

    public static final String PDL_CACHE = "pdl";

    public static final String NORGNAVN_CACHE = "norgnavn";

    public static final String NORG_GEO_ENHET = "norggeoenhet";

    public static final String ARENA_CACHCE = "arena";

    // @Autowired
    private CacheDto cacheDto;

    // @Bean
    public CacheManager cacheManager() {
        List<CacheDto.Cache> caffeines = cacheDto.getCaffeines();
        List<CaffeineCache> caffeineCaches = caffeines.parallelStream()
                .map(this::buildCache)
                .collect(Collectors.toList());
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(caffeineCaches);
        return manager;
    }

    private CaffeineCache buildCache(CacheDto.Cache cacheConfig) {
        return new CaffeineCache(cacheConfig.getName(), Caffeine.newBuilder()
                .expireAfterWrite(cacheConfig.getExpiryInMinutes(), TimeUnit.MINUTES)
                .build());
    }
}