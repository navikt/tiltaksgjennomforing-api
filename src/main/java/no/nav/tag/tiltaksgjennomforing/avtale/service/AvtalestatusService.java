package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AvtalestatusService {
    private final AvtaleRepository avtaleRepository;

    public AvtalestatusService(AvtaleRepository avtaleRepository) {
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public OppdaterAvtalestatusRespons oppdaterBatch(UUID fraId, Limit limit) {
        List<UUID> ider = avtaleRepository.finnAvtaleIderForEndringAvStatus(fraId, limit);
        boolean harFlere = ider.size() == limit.max();

        if (ider.isEmpty()) {
            return new OppdaterAvtalestatusRespons(fraId, false, 0);
        }

        UUID sisteId = ider.getLast();
        int antallOppdatert = 0;

        for (var avtale : avtaleRepository.findAllById(ider)) {
            Status status = Status.fra(avtale);
            if (avtale.getStatus().equals(status)) {
                continue;
            }

            log.info(
                "Avtale med id {} har endret status fra {} til {}. Avtalen blir oppdatert.",
                avtale.getId(),
                avtale.getStatus(),
                status
            );

            avtale.oppdaterStatus();
            avtaleRepository.save(avtale);
            antallOppdatert++;
        }

        return new OppdaterAvtalestatusRespons(sisteId, harFlere, antallOppdatert);
    }

}
