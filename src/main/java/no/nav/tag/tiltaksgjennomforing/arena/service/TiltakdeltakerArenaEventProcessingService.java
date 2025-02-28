package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTable;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakdeltaker;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Operation;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEvent;
import no.nav.tag.tiltaksgjennomforing.arena.models.event.ArenaEventStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaEventRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakdeltakerRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakgjennomforingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class TiltakdeltakerArenaEventProcessingService implements IArenaEventProcessingService {
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
        Optional<ArenaEvent> tiltaksgjennomforingEvent = eventRepository.findByArenaIdAndArenaTable(
            tiltakdeltaker.getTiltakgjennomforingId().toString(),
            ArenaTable.TILTAKGJENNOMFORING.getTable()
        );

        boolean isTiltakgjennomforingIgnoredOrDeleted = tiltaksgjennomforingEvent
            .map((tiltak) -> ArenaEventStatus.IGNORED == tiltak.getStatus() || Operation.DELETE == tiltak.getOperation())
            .orElse(false);

        if (isTiltakgjennomforingIgnoredOrDeleted) {
            log.info("Arena-event ignorert fordi tilhørende tiltakgjennomføring er ignorert eller slettet");
            delete(tiltakdeltaker, () -> log.info("Sletter tidligere håndtert deltaker som nå skal ignoreres"));
            return ArenaEventStatus.IGNORED;
        }

        ArenaTiltakskode tiltakskode = tiltaksgjennomforingEvent
            .flatMap((tiltak) -> tiltak.getPayloadFieldAsText("TILTAKSKODE"))
            .map(ArenaTiltakskode::parse)
            .orElse(ArenaTiltakskode.UKJENT);

        if (tiltakskode.isFerdigMigrert()) {
            log.info("Arena-event tilhører et tiltak som er ferdig migrert");
            return ArenaEventStatus.DONE;
        }

        log.info(
            "Arena-event prosesseres med operasjon {}",
            arenaEvent.getOperation().name()
        );

        if (Operation.DELETE == arenaEvent.getOperation()) {
            delete(tiltakdeltaker, () -> log.info("Arena-event har operasjon DELETE og blir slettes"));
            return ArenaEventStatus.DONE;
        }

        boolean tiltakgjennomforingExists = tiltakgjennomforingRepository.existsById(tiltakdeltaker.getTiltakgjennomforingId());
        if (!tiltakgjennomforingExists) {
            log.info("Arena-event settes på vent; tilhørende tiltakgjennomforing er ikke prossesert ennå");
            return ArenaEventStatus.RETRY;
        }

        ordsService.fetchPerson(tiltakdeltaker.getPersonId());
        tiltakdeltakerRepository.save(tiltakdeltaker);

        log.info("Arena-event er ferdig prossesert");
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
                    deleteFnr(arenaTiltakdeltaker.getPersonId());
                }
            );
    }

    private void deleteFnr(Integer personId) {
        if (personId == null) {
            return;
        }

        if (!tiltakdeltakerRepository.findByPersonId(personId).isEmpty()) {
            log.info("Person {} er fortsatt i bruk", personId);
            return;
        }
        ordsService.attemptDeleteFnr(personId);
    }
}
