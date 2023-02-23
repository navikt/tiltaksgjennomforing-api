package no.nav.tag.tiltaksgjennomforing.infrastruktur.cache;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;


//@Configuration
//@EnableCaching
@RequiredArgsConstructor
public class JavaxCacheConfiguration {

    private final EventAndKeyHashEventLogger eventAndKeyHashEventLogger;

    private final EventTypeEventLogger eventTypeEventLogger;

    public final static String STS_CACHE = "sts_cache";
    public final static String ABAC_CACHE = "abac_cache";
    public static final String AXSYS_CACHE = "axsys_cache";

    public static final String PDL_CACHE = "pdl";

    public static final String NORGNAVN_CACHE = "norgnavn";

    public static final String NORG_GEO_ENHET = "norggeoenhet";

    public static final String ARENA_CACHCE = "arena";

    //@Bean
    public CacheManager cacheManager() {



/*        cacheManager.createCache(
                ABAC_CACHE,
                cacheConfiguration(eventAndKeyHashEventLogger, 500, Duration.ofMinutes(30))
        );
        cacheManager.createCache(
                STS_CACHE,
                cacheConfiguration(eventTypeEventLogger, 100, Duration.ofMinutes(59))
        );
        cacheManager.createCache(
                AXSYS_CACHE,
                cacheConfiguration(eventAndKeyHashEventLogger, 500, Duration.ofMinutes(60))
        );
        cacheManager.createCache(
                PDL_CACHE,
                cacheConfiguration(eventAndKeyHashEventLogger, 1000, Duration.ofMinutes(60))
        );
        cacheManager.createCache(
                NORGNAVN_CACHE,
                cacheConfiguration(eventAndKeyHashEventLogger, 1000, Duration.ofMinutes(60))
        );
        cacheManager.createCache(
                NORG_GEO_ENHET,
                cacheConfiguration(eventAndKeyHashEventLogger, 1000, Duration.ofMinutes(60))
        );*/
        CacheConfiguration<Object, Object> cachecConfig = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Object.class,
                        Object.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .offheap(10, MemoryUnit.MB)
                                .build())
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(10)))
                .build();

        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        javax.cache.configuration.Configuration<Object, Object> configuration = Eh107Configuration.fromEhcacheCacheConfiguration(cachecConfig);
        cacheManager.createCache("arena", configuration);
        return cacheManager;
    }

    private javax.cache.configuration.Configuration<Object, Object> cacheConfiguration(
            CacheEventListener<?, ?> eventListener,
            int size,
            Duration duration
    ) {
        CacheConfiguration<Object, Object> cachecConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Object.class,
                        Object.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .offheap(size, MemoryUnit.MB)
                                .build()
                )
                .withService(getCacheEventListenerBuilder(eventListener))
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(duration))
                .build();
        return Eh107Configuration.fromEhcacheCacheConfiguration(cachecConfig);
    }

    private CacheEventListenerConfigurationBuilder getCacheEventListenerBuilder(CacheEventListener<?, ?> eventListener) {
        return CacheEventListenerConfigurationBuilder
                .newEventListenerConfiguration(eventListener, EventType.CREATED, EventType.UPDATED)
                .unordered().asynchronous();
    }
}
