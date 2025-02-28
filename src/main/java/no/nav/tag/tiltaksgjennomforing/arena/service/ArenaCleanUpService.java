package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AnnullertGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ArenaCleanUpService {

    private final ArenaAgreementMigrationRepository arenaAgreementMigrationRepository;
    private final AvtaleRepository avtaleRepository;

    public ArenaCleanUpService(
        ArenaAgreementMigrationRepository arenaAgreementMigrationRepository,
        AvtaleRepository avtaleRepository
    ) {
        this.arenaAgreementMigrationRepository = arenaAgreementMigrationRepository;
        this.avtaleRepository = avtaleRepository;
    }

    @Transactional
    public void cleanUp() {
        List<Avtale> avtaleList = arenaAgreementMigrationRepository.findAgreementsForCleanUp(
            ArenaTiltakskode.GJELDENDE_MIGRERING.getTiltakstype(),
            Limit.of(1000)
        );
        log.info("Rydder opp {} avtaler som ikke ble migrert fra Arena", avtaleList.size());

        for (Avtale avtale : avtaleList) {
            log.info(
                "Annullerer avtale med id {}, tiltakstype: {} og status: {} ",
                avtale.getId(),
                avtale.getTiltakstype(),
                avtale.getStatus()
            );
            avtale.annuller(AnnullertGrunn.FINNES_IKKE_I_ARENA, Identifikator.ARENA);
        }

        avtaleRepository.saveAll(avtaleList);
    }

}
