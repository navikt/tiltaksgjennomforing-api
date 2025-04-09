package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class DvhAvtalePatchService {

    private final AvtaleRepository avtaleRepository;
    private final DvhMeldingEntitetRepository dvhRepository;

    @Async
    public void lagDvhPatchMeldingForAlleAvtaler() {
        AtomicInteger antallPatchet = new AtomicInteger();
        List<Avtale> alleAvtaler = avtaleRepository.findAllByGjeldendeInnhold_AvtaleInngåttNotNull();

        alleAvtaler.forEach(avtale -> {
            if(skalPatches(avtale)) {
                lagDvhPatchMelding(avtale);
                antallPatchet.getAndIncrement();
                if(antallPatchet.get() % 100 == 0) {
                    log.info("Migrert {} antall avtaler", antallPatchet.get());
                }
            }
            log.info("Avtale {} skal ikke patches i DVH", avtale.getId());
        });
        log.info("Migrert {} antall avtaler", antallPatchet.get());
    }

    @Async
    public void lagDvhPatchMeldingerForTiltakstype(Tiltakstype tiltakstype) {
        AtomicInteger antallPatchet = new AtomicInteger();
        List<Avtale> avtaler = avtaleRepository.findAllByTiltakstypeAndGjeldendeInnhold_AvtaleInngåttNotNull(tiltakstype);
        avtaler.forEach(avtale -> {
            if(skalPatches(avtale)) {
                lagDvhPatchMelding(avtale);
                antallPatchet.getAndIncrement();
                if(antallPatchet.get() % 100 == 0) {
                    log.info("Migrert {} antall avtaler med tiltakstype {}", antallPatchet.get(), tiltakstype);
                }
            }
            log.info("Avtale {} skal ikke patches i DVH", avtale.getId());
        });
        log.info("Migrert {} antall avtaler med tiltakstype {}}", antallPatchet.get(), tiltakstype);
    }

    @Transactional
    public void lagDvhPatchMelding(Avtale avtale) {
        var melding = AvroTiltakHendelseFabrikk.konstruer(avtale, DvhHendelseType.PATCHING, "system");
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(avtale, melding);
        dvhRepository.save(entitet);
    }

    private boolean skalPatches(Avtale avtale) {
        if(avtale.erAvtaleInngått()) {
            if(!avtale.erGodkjentAvVeileder()) {
                log.warn("Avtale {} er inngått men ikke godkjent av veileder", avtale.getId());
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
}
