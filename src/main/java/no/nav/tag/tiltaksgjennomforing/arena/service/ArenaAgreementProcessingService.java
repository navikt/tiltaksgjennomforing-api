package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.logging.ArenaAgreementLogging;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementAggregateRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ArenaAgreementProcessingService {

    private final ArenaAgreementAggregateRepository arenaAgreementAggregateRepository;

    public ArenaAgreementProcessingService(
            ArenaAgreementAggregateRepository arenaAgreementAggregateRepository
    ) {
        this.arenaAgreementAggregateRepository = arenaAgreementAggregateRepository;
    }

    @ArenaAgreementLogging
    @Async("arenaThreadPoolExecutor")
    public void process(ArenaAgreementAggregate agreementAggregate) {

    }

}
