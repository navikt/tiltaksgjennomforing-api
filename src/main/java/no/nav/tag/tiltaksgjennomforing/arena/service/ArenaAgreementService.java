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
    private final ArenaAgreementMigrationRepository arenaAgreementMigrationRepository;

    public ArenaAgreementService(
        ArenaAgreementProcessingService arenaAgreementProcessingService,
        ArenaAgreementMigrationRepository arenaAgreementMigrationRepository
    ) {
        this.arenaAgreementProcessingService = arenaAgreementProcessingService;
        this.arenaAgreementMigrationRepository = arenaAgreementMigrationRepository;
    }

    @Transactional
    public List<ArenaAgreementAggregate> getArenaAgreementsForProcessing() {
        List<ArenaAgreementAggregate> agreementAggregates = arenaAgreementMigrationRepository.findMigrationAgreementAggregates();

        arenaAgreementMigrationRepository.saveAll(
            agreementAggregates
                .stream()
                .map(aggregate ->
                    ArenaAgreementMigration.builder()
                        .tiltakgjennomforingId(aggregate.getTiltakgjennomforingId())
                        .tiltakdeltakerId(aggregate.getTiltakdeltakerId())
                        .eksternId(aggregate.getEksternIdAsUuid().orElse(null))
                        .status(ArenaAgreementMigrationStatus.PROCESSING)
                        .modified(LocalDateTime.now())
                        .build()
                )
                .toList()
        );

        return agreementAggregates;
    }

    public void processAgreements(List<ArenaAgreementAggregate> agreements) {
        log.info("Prosseserer {} avtaler fra Arena", agreements.size());

        for (ArenaAgreementAggregate agreement : agreements) {
            arenaAgreementProcessingService.process(agreement);
        }
    }

}
