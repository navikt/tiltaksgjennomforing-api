package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.AnnullertGrunn;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;
import no.nav.tag.tiltaksgjennomforing.avtale.Status;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Async
    @Transactional
    public void cleanUp(ArenaTiltakskode tiltakskode, boolean dryRun) {
        List<Avtale> avtaleList = arenaAgreementMigrationRepository.findAgreementsForCleanUp(tiltakskode.getTiltakstype());

        Map<Status, List<Avtale>> avtalerMedStatus = avtaleList.stream()
            .collect(Collectors.groupingBy(Avtale::getStatus));

        List<Avtale> gjennomfores = Optional.ofNullable(avtalerMedStatus.get(Status.GJENNOMFØRES)).orElse(List.of());

        log.info(
            "{}Rydder opp {} avtaler som ikke ble migrert fra Arena",
            dryRun ? "[DRY-RUN]: " : "",
            gjennomfores.size()
        );

        for (Avtale avtale : avtalerMedStatus.get(Status.GJENNOMFØRES)) {
            log.info(
                "{}Annullerer avtale med id {}, tiltakstype: {} og status: {} ",
                dryRun ? "[DRY-RUN]: " : "",
                avtale.getId(),
                avtale.getTiltakstype(),
                avtale.getStatus()
            );
            avtale.annuller(AnnullertGrunn.FINNES_IKKE_I_ARENA, Identifikator.ARENA);
        }

        List<Avtale> pabegyntEllerManglerGodkjenning = Stream.concat(
            Optional.ofNullable(avtalerMedStatus.get(Status.PÅBEGYNT)).orElse(List.of()).stream(),
            Optional.ofNullable(avtalerMedStatus.get(Status.MANGLER_GODKJENNING)).orElse(List.of()).stream()
        ).toList();

        log.info(
            "{}Fikser opp innhold i {} avtaler som er påbegynt eller mangler godkjenning som ikke kommer fra Arena",
            dryRun ? "[DRY-RUN]: " : "",
            pabegyntEllerManglerGodkjenning.size()
        );

        for (Avtale avtale : pabegyntEllerManglerGodkjenning) {
            if (avtale.getTiltakstype() != Tiltakstype.MENTOR) {
                continue;
            }
            log.info(
                "{}Fikser opp innhold i avtale med id {}, tiltakstype: {} og status: {} ",
                dryRun ? "[DRY-RUN]: " : "",
                avtale.getId(),
                avtale.getTiltakstype(),
                avtale.getStatus()
            );

            avtale.getGjeldendeInnhold().setMentorTimelonn(null);
            avtale.getGjeldendeInnhold().setMentorAntallTimer(null);
            avtale.endreStatus(Status.fra(avtale));
        }

        if (!dryRun) {
            avtaleRepository.saveAll(avtaleList);
        }
    }

    private void fiksInnholdIAvtale(Avtale avtale) {
        if (avtale.getTiltakstype().isMentor()) {
            avtale.getGjeldendeInnhold().setMentorTimelonn(null);
            avtale.getGjeldendeInnhold().setMentorAntallTimer(null);
        }
    }

}
