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
    public List<ArenaEvent> getRetryEvents() {
        return arenaEventRepository.findRetryEvents()
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

    private boolean isReadyForRetry(ArenaEvent arenaEvent) {
        LocalDateTime created = arenaEvent.getCreated();

        return switch (arenaEvent.getRetryCount()) {
            case 0 -> LocalDateTime.now().minusMinutes(1).isBefore(created);
            case 1 -> LocalDateTime.now().minusMinutes(5).isBefore(created);
            case 2 -> LocalDateTime.now().minusMinutes(15).isBefore(created);
            case 3 -> LocalDateTime.now().minusMinutes(30).isBefore(created);
            default -> false;
        };
    }

}
