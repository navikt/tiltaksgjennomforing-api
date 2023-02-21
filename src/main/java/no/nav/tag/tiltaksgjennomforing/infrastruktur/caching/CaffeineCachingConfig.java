package no.nav.tag.tiltaksgjennomforing.infrastruktur.caching;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

//@Configuration
public class CaffeineCachingConfig {

  //  @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> caffeineCacheBuilder = Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterAccess(30, TimeUnit.MINUTES);
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "pdl",
                "norgnavn",
                "norggeoenhet",
                "arena"
        );
        cacheManager.setCaffeine(caffeineCacheBuilder);
        return cacheManager;
    }
}
