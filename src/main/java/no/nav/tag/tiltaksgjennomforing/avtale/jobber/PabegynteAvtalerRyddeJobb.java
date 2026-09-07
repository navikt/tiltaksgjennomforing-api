package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.service.PabegynteAvtalerRyddeJobbService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class PabegynteAvtalerRyddeJobb {
    private final PabegynteAvtalerRyddeJobbService pabegynteAvtalerRyddeJobbService;

    public PabegynteAvtalerRyddeJobb(
        PabegynteAvtalerRyddeJobbService pabegynteAvtalerRyddeJobbService
    ) {
        this.pabegynteAvtalerRyddeJobbService = pabegynteAvtalerRyddeJobbService;
    }

    @Scheduled(cron = "0 10 0 * * *")
    @SchedulerLock(name = "PabegynteAvtalerRyddeJobb_run", lockAtMostFor = "PT2H")
    public void run() {
        pabegynteAvtalerRyddeJobbService.ryddAvtalerSomErPabegyntEllerManglerGodkjenning();
    }
}
