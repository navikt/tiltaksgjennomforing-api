package no.nav.tag.tiltaksgjennomforing.arena.job;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaEventProcessingJobService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaEventProcessingJob {
    private final ArenaEventProcessingJobService arenaEventRetryService;
    private final FeatureToggleService featureToggleService;

    public ArenaEventProcessingJob(
        ArenaEventProcessingJobService arenaEventRetryService,
        FeatureToggleService featureToggleService
    ) {
        this.arenaEventRetryService = arenaEventRetryService;
        this.featureToggleService = featureToggleService;
    }

    @Scheduled(cron = "0 * 1-23 * * *")
    @SchedulerLock(name = "ArenaEventProcessingJob_run", lockAtLeastFor = "PT30S", lockAtMostFor = "PT1M")
    public void run() {
        if (!featureToggleService.isEnabled(FeatureToggle.ARENA_PROSESSERINGS_JOBB)) {
            return;
        }

        List<ArenaEvent> arenaEvents = arenaEventRetryService.getAndUpdateEvents();

        if (!arenaEvents.isEmpty()) {
            arenaEventRetryService.process(arenaEvents);
        }
    }

}
