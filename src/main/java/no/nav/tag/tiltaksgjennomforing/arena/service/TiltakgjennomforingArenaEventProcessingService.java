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
public class TiltakgjennomforingArenaEventProcessingService implements IArenaEventProcessingService {

    private final ObjectMapper objectMapper;
    private final ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository;
    private final ArenaOrdsService ordsService;

    public TiltakgjennomforingArenaEventProcessingService(
        ArenaOrdsService ordsService,
        ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository,
        ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.ordsService = ordsService;
        this.tiltakgjennomforingRepository = tiltakgjennomforingRepository;
    }

    public ArenaEventStatus process(ArenaEvent arenaEvent) throws JsonProcessingException {
        ArenaTiltakgjennomforing tiltakgjennomforing = this.objectMapper.treeToValue(arenaEvent.getPayload(), ArenaTiltakgjennomforing.class);

        if (!tiltakgjennomforing.isArbeidstrening()) {
            log.info("Arena-event ignorert fordi den ikke er arbeidstrening");
            delete(tiltakgjennomforing, () -> log.info("Sletter tidligere håndtert tiltak som nå skal ignoreres"));
            return ArenaEventStatus.IGNORED;
        }

        log.info(
            "Arena-event prosesseres med operasjon {}",
            arenaEvent.getOperation().name()
        );

        if (Operation.DELETE == arenaEvent.getOperation()) {
            delete(tiltakgjennomforing, () -> log.info("Arena-event har operasjon DELETE og slettet"));
            return ArenaEventStatus.DONE;
        }

        ordsService.fetchArbeidsgiver(tiltakgjennomforing.getArbgivIdArrangor());
        tiltakgjennomforingRepository.save(tiltakgjennomforing);

        log.info("Arena-event er ferdig prossesert");
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
                    deleteArbeidsgiver(arenaTiltakgjennomforing.getArbgivIdArrangor());
                }
            );
    }

    private void deleteArbeidsgiver(Integer arbeidsgiverId) {
        if (arbeidsgiverId == null) {
            return;
        }

        if (!tiltakgjennomforingRepository.findByArbgivIdArrangor(arbeidsgiverId).isEmpty()) {
            log.info("Arbeidsgiver {} er fortsatt i bruk", arbeidsgiverId);
            return;
        }
        ordsService.attemptDeleteArbeidsgiver(arbeidsgiverId);
    }
}
