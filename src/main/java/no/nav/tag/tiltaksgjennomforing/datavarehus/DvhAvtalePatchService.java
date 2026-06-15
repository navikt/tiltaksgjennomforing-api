package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DvhAvtalePatchService {
    private final AvtaleRepository avtaleRepository;
    private final DvhMeldingEntitetRepository dvhRepository;

    private Slice<Avtale> hentAvtaler(Tiltakstype tiltakstype, Pageable page) {
        return tiltakstype == null ?
            avtaleRepository.findAllByGjeldendeInnhold_AvtaleInngåttNotNull(page) :
            avtaleRepository.findAllByTiltakstype(tiltakstype, page);
    }

    @Transactional
    public DvhAvtalePatcherRespons patch(Tiltakstype tiltakstype, Pageable pageable) {
        AtomicInteger antallOppdatert = new AtomicInteger();
        Slice<Avtale> slice = hentAvtaler(tiltakstype, pageable);
        List<Avtale> avtaler = slice.getContent();

        if (avtaler.isEmpty()) {
            log.info("Ingen flere avtaler å patche");
            return new DvhAvtalePatcherRespons(slice, antallOppdatert.get());
        }

        log.info("Patcher {} avtaler...", avtaler.size());
        avtaler.forEach(avtale -> {
            if (skalPatches(avtale)) {
                lagDvhPatchMelding(avtale);
                antallOppdatert.getAndIncrement();
            } else {
                log.info("Avtale {} skal ikke patches i DVH", avtale.getId());
            }
        });

        return new DvhAvtalePatcherRespons(slice, antallOppdatert.get());
    }

    private void lagDvhPatchMelding(Avtale avtale) {
        var melding = AvroTiltakHendelseFabrikk.konstruer(avtale, DvhHendelseType.PATCHING, "system");
        DvhMeldingEntitet entitet = new DvhMeldingEntitet(avtale, melding);
        dvhRepository.save(entitet);
    }

    private boolean skalPatches(Avtale avtale) {
        return avtale.erAvtaleInngått() || avtale.harArenaOpphavEllerHistoriskEndretAvArena();
    }
}
