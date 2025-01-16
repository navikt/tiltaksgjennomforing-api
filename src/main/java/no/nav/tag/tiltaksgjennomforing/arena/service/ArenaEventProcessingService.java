package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.logging.ArenaEventLogging;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaKafkaMessage;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class ArenaEventProcessingService {
    private final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final ArenaEventRepository arenaEventRepository;
    private final TiltakgjennomforingArenaEventProcessingService tiltakgjennomforingArenaEventService;
    private final TiltakdeltakerArenaEventProcessingService tiltakdeltakerArenaEventService;

    public ArenaEventProcessingService(
        ObjectMapper objectMapper,
        ArenaEventRepository arenaEventRepository,
        TiltakgjennomforingArenaEventProcessingService tiltakgjennomforingArenaEventService,
        TiltakdeltakerArenaEventProcessingService tiltakdeltakerArenaEventService
    ) {
        this.objectMapper = objectMapper;
        this.arenaEventRepository = arenaEventRepository;
        this.tiltakgjennomforingArenaEventService = tiltakgjennomforingArenaEventService;
        this.tiltakdeltakerArenaEventService = tiltakdeltakerArenaEventService;
    }

    @Async("arenaThreadPoolExecutor")
    public void create(String key, String value) {
        try {
            ArenaKafkaMessage message = this.objectMapper.readValue(sanitize(value), ArenaKafkaMessage.class);
            create(key, message);
        } catch (JsonProcessingException e) {
            log.error("Feil ved prosessering av Arena-event", e);
        }
    }

    private void create(String key, ArenaKafkaMessage message) {
        String operation = message.opType();
        String table = message.table();

        Lock lock = locks.computeIfAbsent(key + table, k -> new ReentrantLock());
        lock.lock();

        try {
            JsonNode payload = Operation.parse(operation) == Operation.DELETE
                ? message.before()
                : message.after();

            Optional<ArenaEvent> existingArenaEventOpt = arenaEventRepository.findByArenaIdAndArenaTable(key, table);

            boolean isAlreadyProcessed = existingArenaEventOpt
                .map(e -> message.opTimestamp().isBefore(e.getOperationTime()))
                .orElse(false);

            if (isAlreadyProcessed) {
                log.info(
                    "Ignorerer arena-event {} som allerede er prossesert med et OP-tidspunkt fremover i tid.",
                    existingArenaEventOpt.get().getLogId()
                );
                return;
            }

            boolean isEqual = existingArenaEventOpt
                .map(e -> message.opTimestamp().isEqual(e.getOperationTime()) && e.getPayload().equals(payload))
                .orElse(false);

            if (isEqual) {
                return;
            }

            ArenaEvent arenaEvent = existingArenaEventOpt
                .map(exisitingArenaEvent ->
                    exisitingArenaEvent.toBuilder()
                        .operation(operation)
                        .operationTime(message.opTimestamp())
                        .payload(payload)
                        .retryCount(0)
                        .status(ArenaEventStatus.CREATED)
                        .build()
                )
                .orElse(
                    ArenaEvent.create(
                        key,
                        table,
                        operation,
                        message.opTimestamp(),
                        payload,
                        ArenaEventStatus.CREATED
                    )
                );

            log.info(
                "Oppretter arena-event {} med id {} og operasjon {}",
                arenaEvent.getLogId(),
                arenaEvent.getId(),
                arenaEvent.getOperation().name()
            );

            arenaEventRepository.save(arenaEvent);
        } catch (Exception e) {
            Optional<ArenaEvent> arenaEventOpt = arenaEventRepository.findByArenaIdAndArenaTable(key, table);

            ArenaEvent arenaEvent = arenaEventOpt.map(exisitingArenaEvent ->
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

            log.error(
                "Kunne ikke opprette arena-event {} med id {}.",
                arenaEvent.getLogId(),
                arenaEvent.getId(),
                e
            );

            saveExceptionally(arenaEvent);
        } finally {
            lock.unlock();
            locks.remove(key + table);
        }
    }

    @Transactional
    @ArenaEventLogging
    @Async("arenaThreadPoolExecutor")
    public void process(ArenaEvent arenaEvent) {
        log.info("Starter prosessering av arena-event");

        try {
            var result = switch (arenaEvent.getArenaTable()) {
                case TILTAKGJENNOMFORING -> tiltakgjennomforingArenaEventService.process(arenaEvent);
                case TILTAKDELTAKER -> tiltakdeltakerArenaEventService.process(arenaEvent);
            };

            ArenaEvent completedEvent = arenaEvent.toBuilder()
                .status(result)
                .build();

            if (completedEvent.getStatus() == ArenaEventStatus.RETRY) {
                String retries = Integer.toString(completedEvent.getRetryCount());
                log.info(
                    "Arena-event satt på vent. Antall gjentatte forsøk: {}. Forsøker på nytt.",
                    retries
                );
            }

            arenaEventRepository.save(completedEvent);
        } catch (DataIntegrityViolationException e) {
            ArenaEvent update = arenaEvent.toBuilder()
                .status(ArenaEventStatus.RETRY)
                .build();

            log.info(
                "Feil ved opprettelse av Arena-event. Antall gjentatte forsøk: {}. Forsøker på nytt.",
                arenaEvent.getRetryCount()
            );

            saveExceptionally(update);
        } catch (Exception e) {
            log.error("Feil ved prosessering av Arena-event", e);

            ArenaEvent update = arenaEvent.toBuilder()
                .status(ArenaEventStatus.FAILED)
                .build();

            saveExceptionally(update);
        }
    }

    private void saveExceptionally(ArenaEvent arenaEvent) {
        try {
            arenaEventRepository.save(arenaEvent);
        } catch (Exception e) {
            log.error("Feil ved lagring av arena-event", e);
        }
    }

    private String sanitize(String value) {
        return value.replace("\u0000", "").replace("\\u0000", "");
    }
}
