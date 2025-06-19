package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GjeldendeTilskuddsperiodeJobbService {
    private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 500).withSort(Sort.Direction.ASC, "id");
    private final GjeldendeTilskuddsperiodeService gjeldendeTilskuddsperiodeService;

    public GjeldendeTilskuddsperiodeJobbService(GjeldendeTilskuddsperiodeService gjeldendeTilskuddsperiodeService) {
        this.gjeldendeTilskuddsperiodeService = gjeldendeTilskuddsperiodeService;
    }

    @Async
    public CompletableFuture<Void> start() {
        log.info("Jobb for å oppdatere gjeldedeTilskuddsperiode-felt startet...");

        int antallAvtalerBehandlet = 0;
        Slice<Avtale> slice = null;

        do {
            slice = gjeldendeTilskuddsperiodeService.settGjeldendeTilskuddsperiode(
                Optional.ofNullable(slice).map(Slice::nextPageable).orElse(DEFAULT_PAGE)
            );
            antallAvtalerBehandlet += slice.getNumberOfElements();
        } while (slice.hasNext());

        log.info(
            "Jobb for å oppdatere gjeldedeTilskuddsperiode-felt fullført! Behandlet {} avtaler.",
            antallAvtalerBehandlet
        );

        return CompletableFuture.completedFuture(null);
    }
}
