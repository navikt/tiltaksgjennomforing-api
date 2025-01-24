package no.nav.tag.tiltaksgjennomforing.arena.job;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaGosysService;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaGosysJob {
    private final ArenaGosysService arenaCleanUpService;
    private final LeaderPodCheck leaderPodCheck;

    public ArenaGosysJob(
            ArenaGosysService arenaCleanUpService,
            LeaderPodCheck leaderPodCheck
    ) {
        this.arenaCleanUpService = arenaCleanUpService;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(cron = "0 0 14 * * *")
    public void run() {
        if (!leaderPodCheck.isLeaderPod()) {
            return;
        }
        arenaCleanUpService.cleanUp();
    }

}
