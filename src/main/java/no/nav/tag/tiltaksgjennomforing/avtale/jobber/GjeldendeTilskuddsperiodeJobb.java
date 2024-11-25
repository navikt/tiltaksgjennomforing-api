package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({Miljø.DEV_FSS, Miljø.PROD_FSS})
class GjeldendeTilskuddsperiodeJobb {
    private final GjeldendeTilskuddsperiodeJobbWorker gjeldendeTilskuddsperiodeJobbWorker;

    public GjeldendeTilskuddsperiodeJobb(GjeldendeTilskuddsperiodeJobbWorker gjeldendeTilskuddsperiodeJobbWorker) {
        this.gjeldendeTilskuddsperiodeJobbWorker = gjeldendeTilskuddsperiodeJobbWorker;
    }

    @Scheduled(cron = "0 0/15 * * * *")
    public void settGjeldendeTilskuddsperiodeJobb() {
        gjeldendeTilskuddsperiodeJobbWorker.settGjeldendeTilskuddsperiodeJobb();
    }
}

@Slf4j
@Component
class GjeldendeTilskuddsperiodeJobbWorker {
    private final AvtaleRepository avtaleRepository;

    public GjeldendeTilskuddsperiodeJobbWorker(AvtaleRepository avtaleRepository) {
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
