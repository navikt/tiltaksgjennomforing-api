package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbacCacheEventLogger implements CacheEventListener<Object, Object> {

  @Override
  public void onEvent(
    CacheEvent<? extends Object, ? extends Object> cacheEvent) {
      log.debug("Cacheevent: {}, key-hash: {}", cacheEvent.getType(), cacheEvent.getKey().hashCode());
  }

}