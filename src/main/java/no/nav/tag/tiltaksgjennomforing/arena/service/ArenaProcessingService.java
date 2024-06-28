package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaKafkaMessage;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ArenaProcessingService {
    private final static int MAX_RETRY_COUNT = 3;

    private final ObjectMapper objectMapper;
    private final ArenaEventRepository arenaEventRepository;
    private final TiltakgjennomforingArenaEventProcessingService tiltakgjennomforingArenaEventService;
    private final TiltaksakArenaEventProcessingService tiltaksakArenaEventService;
    private final TiltakdeltakerArenaEventProcessingService tiltakdeltakerArenaEventService;

    public ArenaProcessingService(
        ObjectMapper objectMapper,
        ArenaEventRepository arenaEventRepository,
        TiltakgjennomforingArenaEventProcessingService tiltakgjennomforingArenaEventService,
        TiltaksakArenaEventProcessingService tiltaksakArenaEventService,
        TiltakdeltakerArenaEventProcessingService tiltakdeltakerArenaEventService
    ) {
        this.objectMapper = objectMapper;
        this.arenaEventRepository = arenaEventRepository;
        this.tiltakgjennomforingArenaEventService = tiltakgjennomforingArenaEventService;
        this.tiltaksakArenaEventService = tiltaksakArenaEventService;
        this.tiltakdeltakerArenaEventService = tiltakdeltakerArenaEventService;
    }

    @Async("arenaThreadPoolExecutor")
    public void process(String key, String value) {
        try {
            ArenaKafkaMessage message = this.objectMapper.readValue(value, ArenaKafkaMessage.class);
            process(key, message);
        } catch (JsonProcessingException e) {
            log.error("Feil ved prosessering av Arena-event", e);
        }
    }

    @Async("arenaThreadPoolExecutor")
    public void process(ArenaEvent arenaEvent) {
        ArenaEvent processingEvent = arenaEvent.toBuilder()
            .status(ArenaEventStatus.PROCESSING)
            .build();

        arenaEventRepository.save(processingEvent);
        run(processingEvent);
    }

    private void process(String key, ArenaKafkaMessage message) {
        String operation = message.opType();
        String table = message.table();

        try {
            JsonNode payload = Operation.parse(operation) == Operation.DELETE
                ? message.before()
                : message.after();

            ArenaEvent arenaEvent = arenaEventRepository.findByArenaIdAndArenaTable(key, table)
                .map(exisitingArenaEvent ->
                    exisitingArenaEvent.toBuilder()
                        .operation(operation)
                        .payload(payload)
                        .status(ArenaEventStatus.PENDING)
                        .retryCount(0)
                        .build()
                )
                .orElse(
                    ArenaEvent.create(
                        key,
                        table,
                        operation,
                        message.opTimestamp(),
                        payload
                    )
                );

            arenaEventRepository.save(arenaEvent);
            process(arenaEvent);
        } catch (Exception e) {
            log.error("Kunne ikke opprette arena-event. Feil operasjon: {}.", operation, e);

            ArenaEvent arenaEvent = arenaEventRepository.findByArenaIdAndArenaTable(key, table)
                .map(exisitingArenaEvent ->
                    exisitingArenaEvent.toBuilder()
                        .status(ArenaEventStatus.FAILED)
                        .build()
                )
                .orElse(
                    ArenaEvent.create(
                        key,
                        table,
                        operation,
                        message.opTimestamp(),
                        message.after(),
                        ArenaEventStatus.FAILED
                    )
                );

            arenaEventRepository.save(arenaEvent);
        }
    }

    private void run(ArenaEvent arenaEvent) {
        try {
            var result = switch (arenaEvent.getArenaTable()) {
                case TILTAKGJENNOMFORING -> tiltakgjennomforingArenaEventService.process(arenaEvent);
                case TILTAKSAK -> tiltaksakArenaEventService.process(arenaEvent);
                case TILTAKDELTAKER -> tiltakdeltakerArenaEventService.process(arenaEvent);
            };

            ArenaEvent completedEvent = arenaEvent.toBuilder()
                .status(result)
                .build();

            arenaEventRepository.save(completedEvent);
        } catch (DataIntegrityViolationException e) {
            ArenaEventStatus status = arenaEvent.getRetryCount() < MAX_RETRY_COUNT
                ? ArenaEventStatus.RETRY
                : ArenaEventStatus.FAILED;

            ArenaEvent update = arenaEvent.toBuilder()
                .status(status)
                .build();

            if (status == ArenaEventStatus.RETRY) {
                String retries = Integer.toString(arenaEvent.getRetryCount());
                log.info(
                    "Feil ved opprettelse av Arena-event: {}. Antall forsøk: {}. Forsøker på nytt.",
                    arenaEvent.getLogId(),
                    retries
                );
            } else {
                log.error("Arena-event {} har blitt forsøkt max antall ganger. Avbryter.", arenaEvent.getLogId());
            }

            arenaEventRepository.save(update);
        } catch (Exception e) {
            log.error("Feil ved prosessering av Arena-event: {}", arenaEvent.getLogId(), e);

            ArenaEvent update = arenaEvent.toBuilder()
                .status(ArenaEventStatus.FAILED)
                .build();

            arenaEventRepository.save(update);
        }
    }
}
