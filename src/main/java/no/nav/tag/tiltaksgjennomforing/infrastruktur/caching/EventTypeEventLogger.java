package no.nav.tag.tiltaksgjennomforing.infrastruktur.caching;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventTypeEventLogger implements CacheEventListener<Object, Object> {

  @Override
  public void onEvent(
    CacheEvent<? extends Object, ? extends Object> cacheEvent) {
      log.debug("Cacheevent: {}", cacheEvent.getType());
  }

}