package no.nav.tag.tiltaksgjennomforing.infrastruktur.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

//@Service
@Slf4j
public class CacheEvictData {

    @Caching(evict = {
            @CacheEvict(value = "pdlResponse", allEntries = true),
            @CacheEvict(value = "geoenhet", allEntries = true),
            @CacheEvict(value = "oppfolgingnavn", allEntries = true),
            @CacheEvict(value = "oppfolgingenhet", allEntries = true)
    })
   // @Scheduled(fixedRateString = "${caching.spring.evict-rate}")
    public void emptyCaching() {
      log.info("tømmer cache for geo- og oppfølingsenhet i avtaleløsningen");
    }
}
