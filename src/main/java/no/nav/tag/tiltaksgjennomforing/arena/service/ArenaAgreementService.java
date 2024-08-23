package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigration;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigrationStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ArenaAgreementService {

    private final ArenaAgreementProcessingService arenaAgreementProcessingService;
    private final ArenaAgreementMigrationRepository arenaAgreementAggregateRepository;

    public ArenaAgreementService(
            ArenaAgreementProcessingService arenaAgreementProcessingService,
            ArenaAgreementMigrationRepository arenaAgreementAggregateRepository
    ) {
        this.arenaAgreementProcessingService = arenaAgreementProcessingService;
        this.arenaAgreementAggregateRepository = arenaAgreementAggregateRepository;
    }

    @Transactional
    public List<ArenaAgreementAggregate> getArenaAgreementsForProcessing() {
        List<ArenaAgreementAggregate> agreementAggregates = arenaAgreementAggregateRepository.findMigrationAgreementAggregates();

        agreementAggregates.forEach(aggregate -> {
            ArenaAgreementMigration migration = ArenaAgreementMigration.builder()
                    .tiltakgjennomforingId(aggregate.getTiltakgjennomforingId())
                    .status(ArenaAgreementMigrationStatus.PENDING)
                    .modified(LocalDateTime.now())
                    .build();

            arenaAgreementAggregateRepository.save(migration);
        });

        return agreementAggregates;
    }

    public void processAgreements(List<ArenaAgreementAggregate> agreements) {
        log.info("Prosseserer {} avtaler fra Arena", agreements.size());

        for (ArenaAgreementAggregate agreement : agreements) {
            arenaAgreementProcessingService.process(agreement);
        }
    }

}
