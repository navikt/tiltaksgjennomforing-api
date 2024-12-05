package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ArenaAgreementService {

    private final ArenaAgreementProcessingService arenaAgreementProcessingService;
    private final ArenaAgreementMigrationRepository arenaAgreementMigrationRepository;

    public ArenaAgreementService(
            ArenaAgreementProcessingService arenaAgreementProcessingService,
            ArenaAgreementMigrationRepository arenaAgreementMigrationRepository
    ) {
        this.arenaAgreementProcessingService = arenaAgreementProcessingService;
        this.arenaAgreementMigrationRepository = arenaAgreementMigrationRepository;
    }

    public List<ArenaAgreementAggregate> getArenaAgreementsForProcessing() {
        return arenaAgreementMigrationRepository.findMigrationAgreementAggregates();
    }

    public void processAgreements(List<ArenaAgreementAggregate> agreements) {
        log.info("Prosseserer {} avtaler fra Arena", agreements.size());

        for (ArenaAgreementAggregate agreement : agreements) {
            arenaAgreementProcessingService.process(agreement);
        }
    }

}
