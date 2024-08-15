package no.nav.tag.tiltaksgjennomforing.arena.job;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaEventRetryService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({ Miljø.DEV_FSS, Miljø.PROD_FSS })
public class ArenaEventRetryJob {
    private static final int ONE_MIN_IN_MS = 60 * 1000;

    private final ArenaEventRetryService arenaEventRetryService;

    public ArenaEventRetryJob(ArenaEventRetryService arenaEventRetryService) {
        this.arenaEventRetryService = arenaEventRetryService;
    }

    @Scheduled(fixedDelay = ONE_MIN_IN_MS)
    public void run() {
        List<ArenaEvent> arenaEvents = arenaEventRetryService.getAndUpdateRetryEvents();

        if (!arenaEvents.isEmpty()) {
            arenaEventRetryService.retry(arenaEvents);
        }
    }

}
