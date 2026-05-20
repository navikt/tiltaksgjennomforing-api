package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class DvhAvtalePatchService {

    private final AvtaleRepository avtaleRepository;
    private final DvhAvtalePatcher dvhAvtalePatcher;

    @Async
    public void lagDvhPatchMeldingForAlleAvtaler() {
        AtomicInteger antallPatchet = new AtomicInteger();
        List<Avtale> alleAvtaler = avtaleRepository.findAllByGjeldendeInnhold_AvtaleInngåttNotNull();

        alleAvtaler.forEach(avtale -> {
            if (!skalPatches(avtale)) {
                log.info("Avtale {} skal ikke patches i DVH", avtale.getId());
            } else {
                dvhAvtalePatcher.lagDvhPatchMelding(avtale);
                antallPatchet.getAndIncrement();
                if (antallPatchet.get() % 100 == 0) {
                    log.info("Migrert {} antall avtaler", antallPatchet.get());
                }
            }
        });
        log.info("Migrert {} antall avtaler", antallPatchet.get());
    }

    private boolean skalPatches(Avtale avtale) {
        if (avtale.erAvtaleInngått()) {
            if (!avtale.erGodkjentAvVeileder()) {
                log.warn("Avtale {} er inngått men ikke godkjent av veileder", avtale.getId());
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
