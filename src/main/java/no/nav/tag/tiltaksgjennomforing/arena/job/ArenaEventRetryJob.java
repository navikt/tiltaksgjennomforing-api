package no.nav.tag.tiltaksgjennomforing.arena.job;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaEventRetryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ArenaEventRetryJob {
    private final ArenaEventRetryService arenaEventRetryService;

    public ArenaEventRetryJob(ArenaEventRetryService arenaEventRetryService) {
        this.arenaEventRetryService = arenaEventRetryService;
    }

    @Scheduled(fixedDelay = 60000)
    public void updateArenaEventStatus() {
        log.info("ArenaEventRetryJob startet");
        List<ArenaEvent> arenaEvents = arenaEventRetryService.getRetryEvents();

        if (!arenaEvents.isEmpty()) {
            arenaEventRetryService.retry(arenaEvents);
        }
    }

}
