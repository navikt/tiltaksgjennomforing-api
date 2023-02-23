package no.nav.tag.tiltaksgjennomforing.infrastruktur.cache;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.List;


// @Component
public class CacheCustomizer implements CacheManagerCustomizer<ConcurrentMapCacheManager> {

    @Override
    public void customize(ConcurrentMapCacheManager cacheManager) {
        cacheManager.setCacheNames(List.of(
                "PDL_RESPONSE_CACHE",
                "GEO_ENHET_CACHE",
                "OPPFOLGING_NAVN_CACHE",
                "OPPFOLGING_ENHET_CACHE"
        ));
    }
}

