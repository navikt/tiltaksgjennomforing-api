package no.nav.tag.tiltaksgjennomforing.arena.job;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaCleanUpService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaCleanUpJob {
    private final ArenaCleanUpService arenaCleanUpService;
    private final FeatureToggleService featureToggleService;
    private final LeaderPodCheck leaderPodCheck;

    public ArenaCleanUpJob(
            ArenaCleanUpService arenaCleanUpService,
            FeatureToggleService featureToggleService,
            LeaderPodCheck leaderPodCheck
    ) {
        this.arenaCleanUpService = arenaCleanUpService;
        this.featureToggleService = featureToggleService;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void run() {
        if (!leaderPodCheck.isLeaderPod() || !featureToggleService.isEnabled(FeatureToggle.ARENA_CLEAN_UP_JOB)) {
            return;
        }
        arenaCleanUpService.cleanUp();
    }

}
