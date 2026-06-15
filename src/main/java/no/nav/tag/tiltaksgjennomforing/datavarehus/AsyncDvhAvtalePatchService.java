package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncDvhAvtalePatchService {
    private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 100).withSort(Sort.Direction.ASC, "id");

    private final DvhAvtalePatchService dvhAvtalePatcher;

    @Async
    public CompletableFuture<Void> lagDvhPatchMeldingForAlleAvtaler() {
        return lagDvhPatchMeldingForAlleAvtaler(null);
    }

    @Async
    public CompletableFuture<Void> lagDvhPatchMeldingForAlleAvtaler(Tiltakstype tiltakstype) {
        log.info("Jobb for å patche meldinger til DVH startet...");

        Slice<Avtale> slice = null;
        int antallAvtalerSendt = 0;

        do {
            DvhAvtalePatcherRespons respons = dvhAvtalePatcher.patch(
                tiltakstype,
                Optional.ofNullable(slice).map(Slice::nextPageable).orElse(DEFAULT_PAGE)
            );
            slice = respons.slice();
            antallAvtalerSendt += respons.antallAvtalerSendt();
        } while (slice.hasNext());

        log.info("Jobb for å patche alle avtaler til DVH fullført! {} avtaler ble sendt.", antallAvtalerSendt);
        return CompletableFuture.completedFuture(null);
    }
}
