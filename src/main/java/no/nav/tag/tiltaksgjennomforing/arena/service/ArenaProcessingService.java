package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaKafkaMessage;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTable;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaEventRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ArenaProcessingService {

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

    public void process(String key, String value) {
        try {
            ArenaKafkaMessage message = this.objectMapper.readValue(value, ArenaKafkaMessage.class);
            process(key, message);
        } catch (JsonProcessingException e) {
            log.error("Feil ved prosessering av Arena-event", e);
        }
    }

    private void process(String key, ArenaKafkaMessage message) {
        String operation = message.opType();
        String table = message.table();

        try {
            JsonNode payload = Operation.parse(operation) == Operation.DELETE
                ? message.before()
                : message.after();

            Optional<ArenaEvent> arenaEventOpt = arenaEventRepository.findByArenaIdAndArenaTable(key, table);
            ArenaEvent arenaEvent = arenaEventOpt.orElse(ArenaEvent.create(key, table, operation, payload));

            if (arenaEventOpt.isPresent()) {
                arenaEvent = arenaEvent.toBuilder()
                    .operation(operation)
                    .payload(payload)
                    .status(ArenaEventStatus.PENDING)
                    .retryCount(0)
                    .build();
            }

            arenaEventRepository.save(arenaEvent);
            process(arenaEvent);
        } catch (Exception e) {
            log.error("Kunne ikke opprette arena-event. Feil operasjon: {}.", operation, e);
            ArenaEvent arenaEvent = ArenaEvent.create(key, table, operation, message.after(), ArenaEventStatus.FAILED);
            arenaEventRepository.save(arenaEvent);
        }
    }

    private void process(ArenaEvent arenaEvent) {
        try {
            ArenaTable arenaTable = ArenaTable.parse(arenaEvent.getArenaTable());

            switch (arenaTable) {
                case TILTAKGJENNOMFORING: {
                    tiltakgjennomforingArenaEventService.process(arenaEvent);
                    break;
                }
                case TILTAKSAK: {
                    tiltaksakArenaEventService.process(arenaEvent);
                    break;
                }
                case TILTAKDELTAKER: {
                    tiltakdeltakerArenaEventService.process(arenaEvent);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Feil ved prosessering av Arena-event", e);

            ArenaEvent update = arenaEvent.toBuilder()
                .status(ArenaEventStatus.FAILED)
                .build();

            arenaEventRepository.save(update);
        }
    }
}
