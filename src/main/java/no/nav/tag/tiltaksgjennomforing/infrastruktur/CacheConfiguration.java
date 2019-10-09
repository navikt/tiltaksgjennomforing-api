package no.nav.tag.tiltaksgjennomforing.infrastruktur;

import static java.util.Arrays.asList;

import java.time.Duration;
import java.util.HashSet;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.AbacCacheEventLogger;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.StsCacheEventLogger;
 

@Configuration
@EnableCaching
public class CacheConfiguration {

    public final static String STS_CACHE = "sts_cache";
    public final static String ABAC_CACHE = "abac_cache";

    @Bean
    public JCacheManagerCustomizer cacheConfigurationCustomizer() {
        return cm -> {
            cm.createCache(ABAC_CACHE, cacheConfiguration(new AbacCacheEventLogger(), 10000, Duration.ofMinutes(30)));
            cm.createCache(STS_CACHE, cacheConfiguration(new StsCacheEventLogger(), 1, Duration.ofMinutes(59)));
        };
    }

    private javax.cache.configuration.Configuration<Object, Object> cacheConfiguration(CacheEventListener eventListener, int size, Duration duration) {
      org.ehcache.config.CacheConfiguration<Object, Object> config = CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(size))
                .add(CacheEventListenerConfigurationBuilder.newEventListenerConfiguration(eventListener, new HashSet<EventType>(asList(EventType.values()))))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(duration))
                .build();
        return Eh107Configuration.fromEhcacheCacheConfiguration(config);
    }
}
