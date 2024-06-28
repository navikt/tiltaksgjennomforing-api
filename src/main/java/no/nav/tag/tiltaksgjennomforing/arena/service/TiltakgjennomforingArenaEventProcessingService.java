package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakgjennomforing;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakgjennomforingRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakssakRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TiltakgjennomforingArenaEventProcessingService implements ArenaEventProcessingService {

    private final ObjectMapper objectMapper;
    private final ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository;
    private final ArenaTiltakssakRepository tiltakssakRepository;
    private final ArenaOrdsService ordsService;

    public TiltakgjennomforingArenaEventProcessingService(
        ObjectMapper objectMapper,
        ArenaOrdsService ordsService,
        ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository,
        ArenaTiltakssakRepository tiltakssakRepository
    ) {
        this.objectMapper = objectMapper;
        this.ordsService = ordsService;
        this.tiltakgjennomforingRepository = tiltakgjennomforingRepository;
        this.tiltakssakRepository = tiltakssakRepository;
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
            delete(tiltakgjennomforing, () -> log.info("{} har operasjon DELETE og slettet", arenaEvent.getLogId()));
            ordsService.attemptDeleteArbeidsgiver(tiltakgjennomforing.getArbgivIdArrangor());
            return ArenaEventStatus.DONE;
        }

        boolean tiltakssakExists = tiltakssakRepository.existsById(tiltakgjennomforing.getSakId());
        if (!tiltakssakExists) {
            log.info("{} settes på vent; tilhørende tiltakssak er ikke prossesert ennå", arenaEvent.getLogId());
            return ArenaEventStatus.RETRY;
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
