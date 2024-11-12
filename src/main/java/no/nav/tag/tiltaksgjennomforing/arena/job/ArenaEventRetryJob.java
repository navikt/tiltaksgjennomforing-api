package no.nav.tag.tiltaksgjennomforing.arena.job;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaEventRetryService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaEventRetryJob {
    private final ArenaEventRetryService arenaEventRetryService;
    private final FeatureToggleService featureToggleService;

    public ArenaEventRetryJob(
        ArenaEventRetryService arenaEventRetryService,
        FeatureToggleService featureToggleService
    ) {
        this.arenaEventRetryService = arenaEventRetryService;
        this.featureToggleService = featureToggleService;
    }

    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    public void run() {
        if (!featureToggleService.isEnabled(FeatureToggle.ARENA_KAFKA)) {
            return;
        }

        List<ArenaEvent> arenaEvents = arenaEventRetryService.getAndUpdateRetryEvents();

        if (!arenaEvents.isEmpty()) {
            arenaEventRetryService.retry(arenaEvents);
        }
    }

}
