package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementAggregateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ArenaAgreementService {

    private final ArenaAgreementProcessingService arenaAgreementProcessingService;
    private final ArenaAgreementAggregateRepository arenaAgreementAggregateRepository;

    public ArenaAgreementService(
            ArenaAgreementProcessingService arenaAgreementProcessingService,
            ArenaAgreementAggregateRepository arenaAgreementAggregateRepository
    ) {
        this.arenaAgreementProcessingService = arenaAgreementProcessingService;
        this.arenaAgreementAggregateRepository = arenaAgreementAggregateRepository;
    }

    @Transactional
    public List<ArenaAgreementAggregate> getArenaAgreementsForProcessing() {
        return arenaAgreementAggregateRepository.findAgreements();
    }

    public void processAgreements(List<ArenaAgreementAggregate> agreements) {
        log.info("Oppretter {} avtaler fra Arena", agreements.size());

        for (ArenaAgreementAggregate agreement : agreements) {
            arenaAgreementProcessingService.process(agreement);
        }
    }

}
