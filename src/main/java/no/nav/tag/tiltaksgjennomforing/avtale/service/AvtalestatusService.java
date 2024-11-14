package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AvtalestatusService {

    private final AvtaleRepository avtaleRepository;

    public AvtalestatusService(AvtaleRepository avtaleRepository) {
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public void oppdaterAvtalerSomKreverEndringAvStatus() {
        avtaleRepository.findAvtalerForEndringAvStatus().forEach(avtale -> {
            Status status = Status.fra(avtale);
            if (avtale.getStatus().equals(status)) {
                return;
            }

            log.info(
                "Avtale med id {} oppdateres med ny status {}. Tidligere status var {}.",
                avtale.getId(),
                status,
                avtale.getStatus()
            );
            avtale.setStatus(status);
            avtaleRepository.save(avtale);
        });
    }
}
