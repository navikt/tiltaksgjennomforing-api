package no.nav.tag.tiltaksgjennomforing.persondata;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PersondataDiskresjonskodeCache {
    private final Cache<Fnr, Diskresjonskode> cache;

    public PersondataDiskresjonskodeCache() {
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(24))
            .maximumSize(25_000)
            .build();
    }

    public Optional<Diskresjonskode> get(Fnr fnr) {
        return Optional.ofNullable(cache.getIfPresent(fnr));
    }

    public Map<Fnr, Diskresjonskode> getIfPresent(Set<Fnr> fnrSet) {
        return fnrSet.stream()
            .map(fnr -> Map.entry(fnr, Optional.ofNullable(cache.getIfPresent(fnr))))
            .filter(entry -> entry.getValue().isPresent())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get()
            ));
    }

    public void put(Fnr fnr, Diskresjonskode diskresjonskode) {
        cache.put(fnr, diskresjonskode);
    }

    public void putAll(Map<Fnr, Diskresjonskode> diskresjonskodeMap) {
        cache.putAll(diskresjonskodeMap);
    }

    public static Map<Fnr, Diskresjonskode> concat(Map<Fnr, Diskresjonskode> a, Map<Fnr, Diskresjonskode> b) {
        return Stream
            .concat(a.entrySet().stream(), b.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
