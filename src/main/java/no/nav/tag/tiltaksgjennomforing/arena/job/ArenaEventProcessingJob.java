package no.nav.tag.tiltaksgjennomforing.arena.job;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaEventProcessingJobService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Profile({ Miljø.DEV_FSS_Q0, Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaEventProcessingJob {
    private final ArenaEventProcessingJobService arenaEventRetryService;

    public ArenaEventProcessingJob(ArenaEventProcessingJobService arenaEventRetryService) {
        this.arenaEventRetryService = arenaEventRetryService;
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void run() {
        List<ArenaEvent> arenaEvents = arenaEventRetryService.getAndUpdateEvents();

        if (!arenaEvents.isEmpty()) {
            arenaEventRetryService.process(arenaEvents);
        }
    }

}
