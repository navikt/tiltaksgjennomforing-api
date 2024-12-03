package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class GjeldendeTilskuddsperiodeJobbService {
    private final AvtaleRepository avtaleRepository;

    public GjeldendeTilskuddsperiodeJobbService(AvtaleRepository avtaleRepository, LeaderPodCheck leaderPodCheck) {
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public void settGjeldendeTilskuddsperiodeJobb() {
        log.info("Starter jobb for å oppdatere gjeldedeTilskuddsperiode-felt");
        var avtaler = avtaleRepository.findAllByGjeldendeTilskuddsperiodeIsNullAndTiltakstypeIsNot(
                Tiltakstype.ARBEIDSTRENING,
                Limit.of(100)
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
            // TODO: Utfør!
            //avtale.setGjeldendeTilskuddsperiode(nyGjeldende);
        });
        //avtaleRepository.saveAll(avtaler);
        log.info("Dry-run over!");
    }
}
