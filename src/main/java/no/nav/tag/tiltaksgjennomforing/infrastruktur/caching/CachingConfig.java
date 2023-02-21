package no.nav.tag.tiltaksgjennomforing.infrastruktur.caching;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

// @Configuration
// @EnableCaching
public class CachingConfig {

   // @Bean
    public CacheManager tiltakCacheManager() {
       SimpleCacheManager cacheManager = new SimpleCacheManager();
       cacheManager.setCaches(
               Arrays.asList(
                       new ConcurrentMapCache("pdl"),
                       new ConcurrentMapCache("norgnavn"),
                       new ConcurrentMapCache("norggeoenhet"),
                       new ConcurrentMapCache("arena")
               )
       );
       return cacheManager;
    }
}
