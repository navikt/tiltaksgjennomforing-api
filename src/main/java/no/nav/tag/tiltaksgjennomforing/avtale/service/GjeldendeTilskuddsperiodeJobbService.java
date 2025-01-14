package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class GjeldendeTilskuddsperiodeJobbService {
    private final AvtaleRepository avtaleRepository;

    public GjeldendeTilskuddsperiodeJobbService(AvtaleRepository avtaleRepository) {
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public void settGjeldendeTilskuddsperiodeJobb() {
        log.info("Starter jobb for å oppdatere gjeldedeTilskuddsperiode-felt");
        var avtaler = avtaleRepository.finnAvtaleHvorGjeldendeTilskuddsperiodeKanSettes(
                Set.of(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.SOMMERJOBB, Tiltakstype.VTAO),
                Limit.of(200)
        );
        if (avtaler.isEmpty()) {
            log.info("Ingen avtaler å behandle");
            return;
        }
        log.info("Fant {} avtaler å behandle...", avtaler.size());
        avtaler.forEach(avtale -> {
            var nyGjeldende = avtale.finnGjeldendeTilskuddsperiode();
            if (nyGjeldende == null) {
                log.warn("Fant ikke en gjeldende tilskuddsperiode! Har ikke avtalen aktive tilskuddsperioder? (avtale-id: {})", avtale.getId());
            }
            avtale.setGjeldendeTilskuddsperiode(nyGjeldende);
        });
        avtaleRepository.saveAll(avtaler);
    }
}
