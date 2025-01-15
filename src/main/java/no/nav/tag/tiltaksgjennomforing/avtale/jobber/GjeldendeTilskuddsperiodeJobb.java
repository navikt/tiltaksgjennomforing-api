package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.service.GjeldendeTilskuddsperiodeJobbService;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({Miljø.DEV_FSS, Miljø.PROD_FSS})
class GjeldendeTilskuddsperiodeJobb {
    private final GjeldendeTilskuddsperiodeJobbService gjeldendeTilskuddsperiodeJobbService;
    private final LeaderPodCheck leaderPodCheck;

    public GjeldendeTilskuddsperiodeJobb(GjeldendeTilskuddsperiodeJobbService gjeldendeTilskuddsperiodeJobbService, LeaderPodCheck leaderPodCheck) {
        this.gjeldendeTilskuddsperiodeJobbService = gjeldendeTilskuddsperiodeJobbService;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(cron = "0 5/5 1-4 * * *")
    public void settGjeldendeTilskuddsperiodeJobb() {
        if (leaderPodCheck.isLeaderPod()) {
            log.info("Jobb for å oppdatere gjeldedeTilskuddsperiode-felt startet...");
            gjeldendeTilskuddsperiodeJobbService.settGjeldendeTilskuddsperiodeJobb();
            log.info("Jobb for å oppdatere gjeldedeTilskuddsperiode-felt fullført!");
        }
    }
}
