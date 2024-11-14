package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AvtalestatusService {

    private final AvtaleRepository avtaleRepository;

    public AvtalestatusService(AvtaleRepository avtaleRepository) {
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public void oppdaterAvtalerSomManglerStatus() {
        List<Avtale> avtaler = avtaleRepository.findAvtalerSomIkkeHarStatus();

        if (avtaler.isEmpty()) {
            log.info("Avtalejobben er ferdig og alle avtaler har status - Jobben kan slettes");
            return;
        }

        log.info("Avtalejobben oppdaterer {} avtaler som mangler status", avtaler.size());

        avtaler.forEach(avtale -> {
            avtale.setStatus(Status.fra(avtale));
            avtaleRepository.save(avtale);
        });
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
