package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GjeldendeTilskuddsperiodeJobbService {
    private final GjeldendeTilskuddsperiodeService gjeldendeTilskuddsperiodeService;

    public GjeldendeTilskuddsperiodeJobbService(GjeldendeTilskuddsperiodeService gjeldendeTilskuddsperiodeService) {
        this.gjeldendeTilskuddsperiodeService = gjeldendeTilskuddsperiodeService;
    }

    @Async
    public void start() {
        log.info("Jobb for å oppdatere gjeldedeTilskuddsperiode-felt startet...");

        int antallAvtalerBehandlet = 0;
        Slice<Avtale> avtaler = gjeldendeTilskuddsperiodeService.hentAvtaler();

        do {
            gjeldendeTilskuddsperiodeService.settGjeldendeTilskuddsperiode(avtaler.getContent());
            antallAvtalerBehandlet += avtaler.getNumberOfElements();
            avtaler = gjeldendeTilskuddsperiodeService.hentAvtaler(avtaler.nextPageable());
        } while (avtaler.hasNext());

        log.info(
            "Jobb for å oppdatere gjeldedeTilskuddsperiode-felt fullført! Behandlet {} avtaler.",
            antallAvtalerBehandlet
        );
    }
}
