package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakssak;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakssakRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TiltaksakArenaEventProcessingService implements IArenaEventProcessingService {
    private final ObjectMapper objectMapper;
    private final ArenaTiltakssakRepository tiltaksakRepository;

    public TiltaksakArenaEventProcessingService(
        ObjectMapper objectMapper,
        ArenaTiltakssakRepository tiltaksakRepository
    ) {
        this.objectMapper = objectMapper;
        this.tiltaksakRepository = tiltaksakRepository;
    }

    public ArenaEventStatus process(ArenaEvent arenaEvent) throws JsonProcessingException {
        ArenaTiltakssak tiltaksak = this.objectMapper.treeToValue(arenaEvent.getPayload(), ArenaTiltakssak.class);

        if (!tiltaksak.isTiltaksgjennomforing()) {
            log.info("Arena-event ignorert fordi den ikke er en tiltakssak (SAKSKODE != TILT)");
            delete(
                tiltaksak,
                () -> log.info("Sletter tidligere håndtert sak som nå skal ignoreres")
            );
            return ArenaEventStatus.IGNORED;
        }

        log.info(
            "Arena-event prosesseres med operasjon {}",
            arenaEvent.getOperation().name()
        );

        if (Operation.DELETE == arenaEvent.getOperation()) {
            delete(tiltaksak, () -> log.info("Arena-event har operasjon DELETE og blir slettet"));
            return ArenaEventStatus.DONE;
        }

        tiltaksakRepository.save(tiltaksak);

        log.info("Arena-event er ferdig prossesert");
        return ArenaEventStatus.DONE;
    }

    private void delete(
        ArenaTiltakssak arenaTiltakssak,
        Runnable onBeforeDelete
    ) {
        tiltaksakRepository
            .findById(arenaTiltakssak.getSakId())
            .ifPresent(
                (existingTiltaksak) -> {
                    onBeforeDelete.run();
                    tiltaksakRepository.delete(existingTiltaksak);
                }
            );
    }
}
