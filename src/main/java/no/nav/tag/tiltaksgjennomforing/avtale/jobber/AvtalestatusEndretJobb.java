package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.service.AvtalestatusJobbService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class AvtalestatusEndretJobb {

    private final AvtalestatusJobbService avtalestatusJobbService;

    public AvtalestatusEndretJobb(
        AvtalestatusJobbService avtalestatusJobbService
    ) {
        this.avtalestatusJobbService = avtalestatusJobbService;
    }

    @Scheduled(cron = "0 5 0 * * *")
    @SchedulerLock(name = "AvtalestatusEndretJobb_run", lockAtMostFor = "PT2H")
    public void run() {
        log.info("Jobb for å endre avtalestatus startet...");
        avtalestatusJobbService.oppdaterAvtalerSomKreverEndringAvStatus();
        log.info("Jobb for å endre avtalestatus fullført!");
    }

}
