package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ArenaEventRetryService {
    private final static int MAX_RETRY_COUNT = 4;

    private final ArenaEventRepository arenaEventRepository;
    private final ArenaProcessingService arenaProcessingService;

    public ArenaEventRetryService(
        ArenaEventRepository arenaEventRepository,
        ArenaProcessingService arenaProcessingService
    ) {
        this.arenaEventRepository = arenaEventRepository;
        this.arenaProcessingService = arenaProcessingService;
    }

    @Transactional
    public List<ArenaEvent> getAndUpdateRetryEvents() {
        List<ArenaEvent> retryEvents = arenaEventRepository.findRetryEvents();
        failEventsThatHaveReachedMaxRetryCount(retryEvents);

        return retryEvents
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

    public void retry(List<ArenaEvent> arenaEvents) {
        log.info("Kjører retry på {} eventer", arenaEvents.size());

        for (ArenaEvent arenaEvent : arenaEvents) {
            arenaProcessingService.process(arenaEvent);
        }
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
            case 0 -> LocalDateTime.now().minusMinutes(5).isAfter(created);
            case 1 -> LocalDateTime.now().minusMinutes(10).isAfter(created);
            case 2 -> LocalDateTime.now().minusMinutes(15).isAfter(created);
            case 3 -> LocalDateTime.now().minusHours(30).isAfter(created);
            case 4 -> LocalDateTime.now().minusHours(1).isAfter(created);
            default -> false;
        };
    }

}
