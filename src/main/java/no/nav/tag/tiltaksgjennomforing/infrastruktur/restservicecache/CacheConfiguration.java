package no.nav.tag.tiltaksgjennomforing.infrastruktur.restservicecache;

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

@Configuration
@EnableCaching
public class CacheConfiguration {

    public final static String STS_CACHE = "sts_cache";
    public final static String ABAC_CACHE = "abac_cache";
    public static final String AXSYS_CACHE = "axsys_cache";

    @Bean
    public JCacheManagerCustomizer cacheConfigurationCustomizer() {
        return cm -> {
            EventAndKeyHashEventLogger eventAndKeyHashEventLogger = new EventAndKeyHashEventLogger();
            cm.createCache(ABAC_CACHE, cacheConfiguration(eventAndKeyHashEventLogger, 10000, Duration.ofMinutes(30)));
            cm.createCache(STS_CACHE, cacheConfiguration(new EventTypeEventLogger(), 1, Duration.ofMinutes(59)));
            cm.createCache(AXSYS_CACHE, cacheConfiguration(eventAndKeyHashEventLogger, 1000, Duration.ofMinutes(60)));
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
