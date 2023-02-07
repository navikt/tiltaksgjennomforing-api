package no.nav.tag.tiltaksgjennomforing.infrastruktur.restservicecache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventAndKeyHashEventLogger implements CacheEventListener<Object, Object> {

  @Override
  public void onEvent(
    CacheEvent<? extends Object, ? extends Object> cacheEvent) {
      log.info("Cacheevent: {}, key-hash: {}", cacheEvent.getType(), cacheEvent.getKey().hashCode());
  }

}