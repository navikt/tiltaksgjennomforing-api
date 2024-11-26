package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ArenaEventProcessingJobService {
    private final static int MAX_RETRY_COUNT = 4;

    private final ArenaEventRepository arenaEventRepository;
    private final ArenaEventProcessingService arenaEventProcessingService;

    public ArenaEventProcessingJobService(
        ArenaEventRepository arenaEventRepository,
        ArenaEventProcessingService arenaEventProcessingService
    ) {
        this.arenaEventRepository = arenaEventRepository;
        this.arenaEventProcessingService = arenaEventProcessingService;
    }

    @Transactional
    public List<ArenaEvent> getAndUpdateEvents() {
        List<ArenaEvent> events = findEventsForProcessing();

        return events
            .stream()
            .map(arenaEvent ->
                isMaxRetry(arenaEvent)
                    ? arenaEvent.toBuilder().status(ArenaEventStatus.FAILED).build()
                    : arenaEvent.toBuilder().retryCount(arenaEvent.getRetryCount() + 1).build()
            )
            .peek(arenaEventRepository::save)
            .filter(arenaEvent -> arenaEvent.getStatus() != ArenaEventStatus.FAILED)
            .toList();
    }

    public void process(List<ArenaEvent> arenaEvents) {
        log.info("Kjører retry på {} eventer", arenaEvents.size());

        for (ArenaEvent arenaEvent : arenaEvents) {
            arenaEventProcessingService.process(arenaEvent);
        }
    }

    private List<ArenaEvent> findEventsForProcessing() {
        List<ArenaEvent> events = arenaEventRepository.findNewEventsForProcessing();

        if (events.isEmpty() || events.size() <= 100) {
            events.addAll(arenaEventRepository.findRetryEventsForProcessing());
        }

        return events;
    }

    private boolean isMaxRetry(ArenaEvent arenaEvent) {
        if (arenaEvent.getRetryCount() > MAX_RETRY_COUNT) {
            log.warn(
                "Arena-event {} har blitt forsøkt max antall ganger. Avbryter.",
                arenaEvent.getLogId()
            );
            return true;
        } else {
            return false;
        }
    }

}
