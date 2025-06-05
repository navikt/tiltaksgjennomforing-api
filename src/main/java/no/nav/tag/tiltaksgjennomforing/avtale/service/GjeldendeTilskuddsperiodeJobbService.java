package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        var avtaler = avtaleRepository.finnAvtaleMedAktiveTilskuddsperioder(
            Set.of(
                Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
                Tiltakstype.VARIG_LONNSTILSKUDD,
                Tiltakstype.SOMMERJOBB,
                Tiltakstype.VTAO
            ),
            Set.of(Status.GJENNOMFØRES, Status.KLAR_FOR_OPPSTART)
        );
        if (avtaler.isEmpty()) {
            log.info("Ingen avtaler å behandle");
            return;
        }
        log.info("Fant {} avtaler å behandle...", avtaler.size());
        avtaler.forEach(avtale -> {
            var nyGjeldende = avtale.finnGjeldendeTilskuddsperiode();
            avtale.setGjeldendeTilskuddsperiode(nyGjeldende);
        });
        avtaleRepository.saveAll(avtaler);
    }
}
