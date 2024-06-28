package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTable;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakdeltaker;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaEventRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakdeltakerRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakgjennomforingRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TiltakdeltakerArenaEventProcessingService implements ArenaEventProcessingService {
    private final ObjectMapper objectMapper;
    private final ArenaTiltakdeltakerRepository tiltakdeltakerRepository;
    private final ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository;
    private final ArenaEventRepository eventRepository;
    private final ArenaOrdsService ordsService;

    public TiltakdeltakerArenaEventProcessingService(
        ArenaEventRepository eventRepository,
        ObjectMapper objectMapper,
        ArenaOrdsService ordsService,
        ArenaTiltakdeltakerRepository tiltakdeltakerRepository,
        ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository
    ) {
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.ordsService = ordsService;
        this.tiltakdeltakerRepository = tiltakdeltakerRepository;
        this.tiltakgjennomforingRepository = tiltakgjennomforingRepository;
    }

    public ArenaEventStatus process(ArenaEvent arenaEvent) throws JsonProcessingException {
        ArenaTiltakdeltaker tiltakdeltaker = this.objectMapper.treeToValue(arenaEvent.getPayload(), ArenaTiltakdeltaker.class);

        boolean isTiltakgjennomforingIgnored = eventRepository.findByArenaIdAndArenaTable(
            tiltakdeltaker.getTiltakgjennomforingId().toString(),
            ArenaTable.TILTAKGJENNOMFORING.getTable()
        )
            .map((tiltak) -> ArenaEventStatus.IGNORED == tiltak.getStatus())
            .orElse(false);

        if (isTiltakgjennomforingIgnored) {
            log.info(
                "{} ignorert fordi tilhørende tiltakgjennomføring er ignorert",
                arenaEvent.getLogId()
            );

            delete(
                tiltakdeltaker,
                () -> log.info("Sletter tidligere håndtert deltaker {} som nå skal ignoreres", arenaEvent.getLogId())
            );
            ordsService.attemptDeleteFnr(tiltakdeltaker.getPersonId()) ;

            return ArenaEventStatus.IGNORED;
        }

        log.info(
            "{} prosesseres med operasjon {}",
            arenaEvent.getLogId(),
            arenaEvent.getOperation().name()
        );

        if (Operation.DELETE == arenaEvent.getOperation()) {
            delete(tiltakdeltaker, () -> log.info("{} har operasjon DELETE og slettet", arenaEvent.getLogId()));
            ordsService.attemptDeleteFnr(tiltakdeltaker.getPersonId()) ;
            return ArenaEventStatus.DONE;
        }

        boolean tiltakgjennomforingExists = tiltakgjennomforingRepository.existsById(tiltakdeltaker.getTiltakgjennomforingId());
        if (!tiltakgjennomforingExists) {
            log.info("{} settes på vent; tilhørende tiltakgjennomforing er ikke prossesert ennå", arenaEvent.getLogId());
            return ArenaEventStatus.RETRY;
        }

        ordsService.fetchPerson(tiltakdeltaker.getPersonId());
        tiltakdeltakerRepository.save(tiltakdeltaker);

        log.info("{} er ferdig prossesert", arenaEvent.getLogId());
        return ArenaEventStatus.DONE;
    }

    private void delete(
        ArenaTiltakdeltaker arenaTiltakdeltaker,
        Runnable onBeforeDelete
    ) {
        tiltakdeltakerRepository
            .findById(arenaTiltakdeltaker.getTiltakdeltakerId())
            .ifPresent(
                (existingTiltaksak) -> {
                    onBeforeDelete.run();
                    tiltakdeltakerRepository.delete(existingTiltaksak);
                }
            );
    }
}
