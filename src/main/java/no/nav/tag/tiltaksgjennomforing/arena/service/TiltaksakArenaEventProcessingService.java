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
public class TiltaksakArenaEventProcessingService implements ArenaEventProcessingService {
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
            log.info(
                "{} ignorert fordi den ikke er en tiltakssak (SAKSKODE != TILT)",
                arenaEvent.getLogId()
            );
            delete(
                tiltaksak,
                () -> log.info("Sletter tidligere håndtert sak {} som nå skal ignoreres", arenaEvent.getLogId())
            );
            return ArenaEventStatus.IGNORED;
        }

        if (!tiltaksak.hasEnhent()) {
            log.info(
                "{} ignorert fordi den ikke har en tilhørende enhet (AETATENHET_ANSVARLIG = null)",
                arenaEvent.getLogId()
            );
            delete(
                tiltaksak,
                () -> log.info("Sletter tidligere håndtert sak {} som nå skal ignoreres", arenaEvent.getLogId())
            );
            return ArenaEventStatus.IGNORED;
        }

        log.info(
            "{} prosesseres med operasjon {}",
            arenaEvent.getLogId(),
            arenaEvent.getOperation().name()
        );

        if (Operation.DELETE == arenaEvent.getOperation()) {
            delete(tiltaksak, () -> log.info("{} har operasjon DELETE og slettet", arenaEvent.getLogId()));
            return ArenaEventStatus.DONE;
        }

        tiltaksakRepository.save(tiltaksak);

        log.info("{} er ferdig prossesert", arenaEvent.getLogId());
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
