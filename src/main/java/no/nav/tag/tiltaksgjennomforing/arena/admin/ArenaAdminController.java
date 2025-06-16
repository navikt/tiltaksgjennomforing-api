package no.nav.tag.tiltaksgjennomforing.arena.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltaksgjennomforingIdDeltakerIdOgFnr;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementErrorCount;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigrationCount;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigrationStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaTiltakgjennomforingRepository;
import no.nav.tag.tiltaksgjennomforing.arena.service.ArenaCleanUpService;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ProtectedWithClaims(issuer = "azure-access-token", claimMap = { "groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b" })
@RestController
@RequestMapping("/utvikler-admin/arena")
@Slf4j
@RequiredArgsConstructor
public class ArenaAdminController {
    private final ArenaAgreementMigrationRepository agreementMigrationRepository;
    private final ArenaTiltakgjennomforingRepository tiltakgjennomforingRepository;
    private final EregService eregService;
    private final VeilarboppfolgingService veilarboppfolgingService;
    private final ArenaCleanUpService arenaCleanUpService;

    @GetMapping("/tiltak/{arenaTiltakskode}/sjekk-ereg")
    public Map<String, ?> sjekkEreg(
        @PathVariable ArenaTiltakskode arenaTiltakskode,
        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
        @RequestParam(value = "size", required = false, defaultValue = "1000") Integer size
    ) {
        Pageable pageable = PageRequest.of(Math.abs(page), Math.abs(size));

        Map<BedriftNr, Optional<String>> enheter = tiltakgjennomforingRepository
            .findVirksomhetsnummerByTiltakskode(arenaTiltakskode, pageable)
            .stream()
            .collect(Collectors.toMap(
                BedriftNr::new,
                bedriftNr -> {
                    try {
                        eregService.hentVirksomhet(new BedriftNr(bedriftNr));
                        return Optional.empty();
                    } catch (Exception e) {
                        if (e instanceof FeilkodeException) {
                            return Optional.of(((FeilkodeException) e).getFeilkode().name());
                        }
                        return Optional.of(e.getMessage());
                    }
                },
                (first, second) -> first.isPresent() ? first : second
            ));

        return Map.of(
            "totalt", enheter.size(),
            "gjennomfort", enheter.values().stream().filter(Optional::isEmpty).count(),
            "failet", enheter.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()))
        );
    }

    @GetMapping("/tiltak/{arenaTiltakskode}/sjekk-oppfolging")
    public Map<String, ?> sjekkOppfolgingsstatus(
        @PathVariable ArenaTiltakskode arenaTiltakskode,
        @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
        @RequestParam(value = "size", required = false, defaultValue = "1000") Integer size
    ) {
        Pageable pageable = PageRequest.of(Math.abs(page), Math.abs(size));

        Map<Integer, Optional<String>> enheter = tiltakgjennomforingRepository
            .findFnrByTiltakskode(arenaTiltakskode, pageable)
            .stream()
            .collect(Collectors.toMap(
                ArenaTiltaksgjennomforingIdDeltakerIdOgFnr::getDeltakerId,
                arenaTiltaksgjennomforingIdDeltakerIdOgFnr -> {
                    try {
                        veilarboppfolgingService.hentOgSjekkOppfolgingstatus(
                            Fnr.av(arenaTiltaksgjennomforingIdDeltakerIdOgFnr.getFnr()),
                            arenaTiltakskode.getTiltakstype()
                        );
                        return Optional.empty();
                    } catch (Exception e) {
                        if (e instanceof FeilkodeException) {
                            return Optional.of(((FeilkodeException) e).getFeilkode().name());
                        }
                        return Optional.of(e.getMessage());
                    }
                },
                (first, second) -> first.isPresent() ? first : second
            ));

        return Map.of(
            "totalt", enheter.size(),
            "gjennomfort", enheter.values().stream().filter(Optional::isEmpty).count(),
            "failet", enheter.entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()))
        );
    }

    @GetMapping("/tiltak/{arenaTiltakskode}/statistikk")
    public Map<?, ?> hentStatistikk(@PathVariable ArenaTiltakskode arenaTiltakskode) {
        long count = agreementMigrationRepository.countMigrationAgreementAggregates(arenaTiltakskode);

        List<ArenaAgreementMigrationCount> arenaAgreementMigration =
            agreementMigrationRepository.getStatistics(arenaTiltakskode);
        List<ArenaAgreementErrorCount> arenaAgreementError =
            arenaAgreementMigration.stream().anyMatch(aam -> ArenaAgreementMigrationStatus.FAILED == aam.getStatus()) ?
                agreementMigrationRepository.findMigrationErrors(arenaTiltakskode) :
                Collections.emptyList();

        Map<?, ?> statistikk = arenaAgreementMigration.stream().collect(Collectors.toMap(
            ArenaAgreementMigrationCount::getStatus,
            (aam) -> switch (aam.getStatus()) {
                case FAILED -> arenaAgreementError.stream().collect(Collectors.toMap(
                    ArenaAgreementErrorCount::getError,
                    ArenaAgreementErrorCount::getCount
                ));
                case PROCESSING -> aam.getCount();
                case COMPLETED -> arenaAgreementMigration.stream()
                    .filter(aam2 -> aam2.getStatus() == ArenaAgreementMigrationStatus.COMPLETED)
                    .collect(Collectors.toMap(
                        ArenaAgreementMigrationCount::getAction,
                        ArenaAgreementMigrationCount::getCount
                    ));
            },
            (first, second) -> first
        ));

        return Map.of(
            "status", count == 0 ? "Ferdig migrert" : "Migrering pågår - " + count + " gjenstår",
            "statistikk", statistikk
        );
    }

    @Transactional
    @PostMapping("/tiltak/{arenaTiltakskode}/reset")
    public void reset(
        @PathVariable ArenaTiltakskode arenaTiltakskode
    ) {
        agreementMigrationRepository.reset(arenaTiltakskode);
    }

    @PostMapping("/tiltak/{arenaTiltakskode}/clean-up")
    public void cleanUp(
        @PathVariable ArenaTiltakskode arenaTiltakskode,
        @RequestParam("dry-run") Boolean dryRun
    ) {
        arenaCleanUpService.cleanUp(arenaTiltakskode, Boolean.TRUE.equals(dryRun));
    }
}
