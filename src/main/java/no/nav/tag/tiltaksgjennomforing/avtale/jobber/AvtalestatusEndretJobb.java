package no.nav.tag.tiltaksgjennomforing.avtale.jobber;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.service.AvtalestatusService;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({ Miljø.DEV_GCP_LABS, Miljø.DEV_FSS, Miljø.PROD_FSS })
public class AvtalestatusEndretJobb {

    private final AvtalestatusService avtalestatusService;
    private final LeaderPodCheck leaderPodCheck;

    public AvtalestatusEndretJobb(
        AvtalestatusService avtalestatusService,
        LeaderPodCheck leaderPodCheck
    ) {
        this.avtalestatusService = avtalestatusService;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void run() {
        if (leaderPodCheck.isLeaderPod()) {
            avtalestatusService.oppdaterAvtalerSomKreverEndringAvStatus();
        }
    }

}
