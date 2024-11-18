package no.nav.tag.tiltaksgjennomforing.arena.job;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaEventProcessingJobService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.leader.LeaderPodCheck;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaEventProcessingJob {
    private final ArenaEventProcessingJobService arenaEventRetryService;
    private final FeatureToggleService featureToggleService;
    private final LeaderPodCheck leaderPodCheck;

    public ArenaEventProcessingJob(
        ArenaEventProcessingJobService arenaEventRetryService,
        FeatureToggleService featureToggleService,
        LeaderPodCheck leaderPodCheck
    ) {
        this.arenaEventRetryService = arenaEventRetryService;
        this.featureToggleService = featureToggleService;
        this.leaderPodCheck = leaderPodCheck;
    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!leaderPodCheck.isLeaderPod() || !featureToggleService.isEnabled(FeatureToggle.ARENA_PROSESSERINGS_JOBB)) {
            return;
        }

        List<ArenaEvent> arenaEvents = arenaEventRetryService.getAndUpdateEvents();

        if (!arenaEvents.isEmpty()) {
            arenaEventRetryService.process(arenaEvents);
        }
    }

}
