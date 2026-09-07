package no.nav.tag.tiltaksgjennomforing.avtale.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class AvtalestatusJobbService {
    private static final Limit LIMIT = Limit.of(500);
    private final AvtalestatusService avtalestatusService;

    public AvtalestatusJobbService(AvtalestatusService avtalestatusService) {
        this.avtalestatusService = avtalestatusService;
    }

    public void oppdaterAvtalerSomKreverEndringAvStatus() {
        UUID sisteId = new UUID(0L, 0L);
        boolean harFlere = true;
        int antallOppdatert = 0;

        while (harFlere) {
            OppdaterAvtalestatusRespons respons = avtalestatusService.oppdaterBatch(sisteId, LIMIT);
            sisteId = respons.sisteId();
            harFlere = respons.harFlere();
            antallOppdatert += respons.antallOppdatert();
        }

        log.debug("Oppdaterte status på {} avtale(r).", antallOppdatert);
    }
}
