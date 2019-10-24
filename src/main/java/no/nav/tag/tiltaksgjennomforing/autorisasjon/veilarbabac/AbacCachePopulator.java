package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;


import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.stream.StreamSupport;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class AbacCachePopulator {

    private final VeilarbabacClient veilarbabacClient;

    private final AvtaleRepository avtaleRepository;

    //Forsøkte å lage en konstant basert på TTL-innstilling i CacheConfiguration, men det godtok ikke kompilatoren
    private static final int RUN_RATE = 60*30*1000;

    @Scheduled(fixedRate=RUN_RATE, initialDelay=5000)
    @Timed
    public void populateCache() {
        long startTid = System.currentTimeMillis();
        Iterable<Avtale> avtaler = avtaleRepository.findAll();
        Set<NavIdent> veiledere = StreamSupport.stream(avtaler.spliterator(), false).map(avtale -> avtale.getVeilederNavIdent()).collect(toSet());
        Set<String> deltakere = StreamSupport.stream(avtaler.spliterator(), false).map(avtale -> avtale.getDeltakerFnr().asString()).collect(toSet());
        veiledere.forEach(veileder -> {
            deltakere.forEach(deltaker -> {
                veilarbabacClient.evict(veileder, deltaker, TilgangskontrollAction.read);
                veilarbabacClient.sjekkTilgang(veileder, deltaker, TilgangskontrollAction.read);
            });
        });
        log.info("populateCache ferdig. Veiledere: {}, deltakere: {}, tid: {} ms", veiledere.size(), deltakere.size(), System.currentTimeMillis() - startTid);
    }
}
