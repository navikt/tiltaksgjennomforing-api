package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.service.PabegynteAvtalerRyddeService;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class PabegynteAvtalerRyddeJobb {
    private final LeaderPodCheck leaderPodCheck;
    private final PabegynteAvtalerRyddeService pabegynteAvtalerRyddeService;

    public PabegynteAvtalerRyddeJobb(
        LeaderPodCheck leaderPodCheck,
        PabegynteAvtalerRyddeService pabegynteAvtalerRyddeService
    ) {
        this.leaderPodCheck = leaderPodCheck;
        this.pabegynteAvtalerRyddeService = pabegynteAvtalerRyddeService;
    }

    @Scheduled(cron = "0 10 0 * * *")
    public void run() {
        if (leaderPodCheck.isLeaderPod()) {
            pabegynteAvtalerRyddeService.ryddAvtalerSomErPabegyntEllerManglerGodkjenning();
        }
    }
}
