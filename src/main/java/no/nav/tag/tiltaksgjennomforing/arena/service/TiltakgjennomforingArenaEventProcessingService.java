package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakgjennomforing;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakgjennomforingRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TiltakgjennomforingArenaEventProcessingService implements ArenaEventProcessingService {

    private final ObjectMapper objectMapper;
    private final ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository;
    private final ArenaOrdsService ordsService;

    public TiltakgjennomforingArenaEventProcessingService(
        ObjectMapper objectMapper,
        ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository,
        ArenaOrdsService ordsService
    ) {
        this.objectMapper = objectMapper;
        this.tiltakgjennomforingRepository = tiltakgjennomforingRepository;
        this.ordsService = ordsService;
    }

    public ArenaEventStatus process(ArenaEvent arenaEvent) throws JsonProcessingException {
        ArenaTiltakgjennomforing tiltakgjennomforing = this.objectMapper.treeToValue(arenaEvent.getPayload(), ArenaTiltakgjennomforing.class);

        if (!tiltakgjennomforing.isArbeidstrening()) {
            log.info("{} ignorert fordi den ikke er arbeidstrening", arenaEvent.getLogId());
            delete(
                tiltakgjennomforing,
                () -> log.info("Sletter tidligere håndtert tiltak {} som nå skal ignoreres", arenaEvent.getLogId())
            );
            return ArenaEventStatus.IGNORED;
        }

        if (tiltakgjennomforing.getDatoFra() == null) {
            log.info("{} ignorert; fra-dato er null", arenaEvent.getLogId());
            delete(
                tiltakgjennomforing,
                () -> log.info("Sletter tidligere håndtert tiltak {} som nå skal ignoreres", arenaEvent.getLogId())
            );
            return ArenaEventStatus.IGNORED;
        }

        if (tiltakgjennomforing.getArbgivIdArrangor() == null) {
            log.info("{} ignorert; ArbGivIdArrangor er null", arenaEvent.getLogId());
            delete(
                tiltakgjennomforing,
                () -> log.info("Sletter tidligere håndtert tiltak {} som nå skal ignoreres", arenaEvent.getLogId())
            );
            return ArenaEventStatus.IGNORED;
        }

        log.info(
            "{} prosesseres med operasjon {}",
            arenaEvent.getLogId(),
            arenaEvent.getOperation().name()
        );

        if (Operation.DELETE == arenaEvent.getOperation()) {
            ordsService.attemptDeleteArbeidsgiver(tiltakgjennomforing.getArbgivIdArrangor());
            delete(tiltakgjennomforing, () -> log.info("{} har operasjon DELETE og slettet", arenaEvent.getLogId()));
            return ArenaEventStatus.DONE;
        }

        ordsService.fetchArbeidsgiver(tiltakgjennomforing.getArbgivIdArrangor());
        tiltakgjennomforingRepository.save(tiltakgjennomforing);

        log.info("{} er ferdig prossesert", arenaEvent.getLogId());
        return ArenaEventStatus.DONE;
    }

    private void delete(
        ArenaTiltakgjennomforing arenaTiltakgjennomforing,
        Runnable onBeforeDelete
    ) {
        tiltakgjennomforingRepository
            .findById(arenaTiltakgjennomforing.getTiltakgjennomforingId())
            .ifPresent(
                (existingTiltaksak) -> {
                    onBeforeDelete.run();
                    tiltakgjennomforingRepository.delete(existingTiltaksak);
                }
            );
    }
}
