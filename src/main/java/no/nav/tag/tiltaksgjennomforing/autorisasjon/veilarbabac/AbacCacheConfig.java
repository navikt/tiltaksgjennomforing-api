package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class AbacCacheConfig {

    final static String ABAC_CACHE = "abac_cache";

    @Bean
    public CaffeineCache abacCache() {
        return new CaffeineCache(ABAC_CACHE,
                Caffeine.newBuilder()
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .recordStats()
                    .build());
    }
}