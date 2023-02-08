package no.nav.tag.tiltaksgjennomforing.caching;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
