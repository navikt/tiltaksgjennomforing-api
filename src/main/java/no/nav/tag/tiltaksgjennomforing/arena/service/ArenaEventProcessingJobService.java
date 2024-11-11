package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaEventRepository;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        failEventsThatHaveReachedMaxRetryCount(events);

        return events
            .stream()
            .filter(this::isReadyForRetry)
            .map(arenaEvent ->
                arenaEvent.toBuilder()
                    .status(ArenaEventStatus.PENDING)
                    .retryCount(arenaEvent.getRetryCount() + 1)
                    .build()
            )
            .peek(arenaEventRepository::save)
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
        return events.isEmpty() ? arenaEventRepository.findRetryEventsForProcessing() : events;
    }

    private void failEventsThatHaveReachedMaxRetryCount(List<ArenaEvent> arenaEvents) {
        arenaEvents
            .stream()
            .filter(this::isMaxRetry)
            .map(arenaEvent ->
                arenaEvent.toBuilder()
                    .status(ArenaEventStatus.FAILED)
                    .build()
            )
            .peek(arenaEvent ->
                log.warn(
                    "Arena-event {} har blitt forsøkt max antall ganger. Avbryter.",
                    arenaEvent.getLogId()
                )
            )
            .forEach(arenaEventRepository::save);
    }

    private boolean isMaxRetry(ArenaEvent arenaEvent) {
        return arenaEvent.getRetryCount() > MAX_RETRY_COUNT;
    }

    private boolean isReadyForRetry(ArenaEvent arenaEvent) {
        LocalDateTime created = arenaEvent.getCreated();

        return switch (arenaEvent.getRetryCount()) {
            case 0 -> Now.localDateTime().minusMinutes(5).isAfter(created);
            case 1 -> Now.localDateTime().minusMinutes(10).isAfter(created);
            case 2 -> Now.localDateTime().minusMinutes(15).isAfter(created);
            case 3 -> Now.localDateTime().minusHours(30).isAfter(created);
            case 4 -> Now.localDateTime().minusHours(1).isAfter(created);
            default -> false;
        };
    }

}
