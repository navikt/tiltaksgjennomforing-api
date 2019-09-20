package no.nav.tag.tiltaksgjennomforing.infrastruktur.sts;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class StsCacheConfig {

    final static String STS_CACHE = "sts_cache";

    @Bean
    public CaffeineCache stsCache() {
        return new CaffeineCache(STS_CACHE,
                Caffeine.newBuilder()
                    .maximumSize(1)
                    .expireAfterWrite(59, TimeUnit.MINUTES)
                    .recordStats()
                    .build());
    }
}