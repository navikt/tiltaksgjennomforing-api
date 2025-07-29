package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigration;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigrationStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Map<UUID, ArenaAgreementAggregate> getArenaAgreementsForProcessing() {
        Map<UUID, ArenaAgreementAggregate> agreementAggregates = arenaAgreementMigrationRepository
                .findMigrationAgreementAggregates(ArenaTiltakskode.GJELDENDE_MIGRERING)
                .stream()
                .collect(Collectors.toMap(aggregate -> UUID.randomUUID(), aggregate -> aggregate));

        arenaAgreementMigrationRepository.saveAll(
            agreementAggregates
                .entrySet()
                .stream()
                .map(entry ->
                    ArenaAgreementMigration.builder()
                        .id(entry.getKey())
                        .tiltakgjennomforingId(entry.getValue().getTiltakgjennomforingId())
                        .tiltakdeltakerId(entry.getValue().getTiltakdeltakerId())
                        .eksternId(entry.getValue().getEksternIdAsUuid().orElse(null))
                        .status(ArenaAgreementMigrationStatus.PROCESSING)
                        .modified(Now.instant())
                        .tiltakstype(entry.getValue().getTiltakskode())
                        .build()
                )
                .toList()
        );

        return agreementAggregates;
    }

    public void processAgreements(Map<UUID, ArenaAgreementAggregate> agreements) {
        log.info("Prosseserer {} avtaler fra Arena", agreements.size());

        for (Map.Entry<UUID, ArenaAgreementAggregate> entry : agreements.entrySet()) {
            arenaAgreementProcessingService.process(entry.getKey(), entry.getValue());
        }
    }

}
