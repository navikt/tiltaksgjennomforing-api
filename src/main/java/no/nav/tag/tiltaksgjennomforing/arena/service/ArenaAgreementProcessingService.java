package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.client.acl.AktivitetArenaAclClient;
import no.nav.tag.tiltaksgjennomforing.arena.client.hendelse.HendelseAktivitetsplanClient;
import no.nav.tag.tiltaksgjennomforing.arena.logging.ArenaAgreementLogging;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaTiltakskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigration;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigrationStatus;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaMigrationAction;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaMigrationProcessResult;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtaleArena;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ArenaAgreementProcessingService {
    private final ArenaAgreementMigrationRepository arenaAgreementMigrationRepository;
    private final AvtaleRepository avtaleRepository;
    private final EregService eregService;
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;
    private final VeilarboppfolgingService veilarboppfolgingService;
    private final AktivitetArenaAclClient aktivitetArenaAclClient;
    private final HendelseAktivitetsplanClient hendelseAktivitetsplanClient;
    private final FeatureToggleService featureToggleService;

    public ArenaAgreementProcessingService(
            ArenaAgreementMigrationRepository arenaAgreementMigrationRepository,
            AvtaleRepository avtaleRepository,
            EregService eregService,
            PersondataService persondataService,
            Norg2Client norg2Client,
            VeilarboppfolgingService veilarboppfolgingService,
            AktivitetArenaAclClient aktivitetArenaAclClient,
            HendelseAktivitetsplanClient hendelseAktivitetsplanClient,
            FeatureToggleService featureToggleService
    ) {
        this.arenaAgreementMigrationRepository = arenaAgreementMigrationRepository;
        this.avtaleRepository = avtaleRepository;
        this.eregService = eregService;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.veilarboppfolgingService = veilarboppfolgingService;
        this.aktivitetArenaAclClient = aktivitetArenaAclClient;
        this.hendelseAktivitetsplanClient = hendelseAktivitetsplanClient;
        this.featureToggleService = featureToggleService;
    }

    @Transactional
    @ArenaAgreementLogging
    @Async("arenaThreadPoolExecutor")
    public void process(UUID migrationId, ArenaAgreementAggregate agreementAggregate) {
        UUID eksternId = agreementAggregate.getEksternIdAsUuid().orElse(null);
        Integer tiltaksgjennomforingId = agreementAggregate.getTiltakgjennomforingId();
        Integer tiltakdeltakerId = agreementAggregate.getTiltakdeltakerId();

        try {
            ArenaMigrationProcessResult result = agreementAggregate.getEksternIdAsUuid()
                    .flatMap(avtaleRepository::findById)
                    .map((existingAvtale) -> updateAvtale(existingAvtale, agreementAggregate))
                    .orElseGet(() -> createAvtale(agreementAggregate));

            switch (result) {
                case ArenaMigrationProcessResult.Completed completed -> {
                    if (agreementAggregate.getTiltakdeltakerId() != null) {
                        transferAktivitetsplankort(completed.avtale(), agreementAggregate.getTiltakdeltakerId());
                    }
                    Avtale nyAvtale = avtaleRepository.save(completed.avtale());
                    log.info(
                        "Lagrer avtale med id {}. Status for avtalen etter migrering {}",
                        nyAvtale.getId(),
                        completed.avtale().getStatus()
                    );
                    saveMigrationStatus(
                        migrationId,
                        tiltaksgjennomforingId,
                        tiltakdeltakerId,
                        ArenaAgreementMigrationStatus.COMPLETED,
                        completed.action(),
                        eksternId,
                        nyAvtale.getId(),
                        agreementAggregate.getTiltakskode(),
                        null
                    );
                }
                case ArenaMigrationProcessResult.Ignored ignored ->
                    saveMigrationStatus(
                        migrationId,
                        tiltaksgjennomforingId,
                        tiltakdeltakerId,
                        ArenaAgreementMigrationStatus.COMPLETED,
                        ArenaMigrationAction.IGNORER,
                        eksternId,
                        null,
                        agreementAggregate.getTiltakskode(),
                        null
                    );
                case ArenaMigrationProcessResult.Failed failed ->
                    saveMigrationStatus(
                        migrationId,
                        tiltaksgjennomforingId,
                        tiltakdeltakerId,
                        ArenaAgreementMigrationStatus.FAILED,
                        null,
                        eksternId,
                        null,
                        agreementAggregate.getTiltakskode(),
                        failed.error()
                    );
            }
        } catch(Exception e) {
            log.error("Feil ved prossesering av avtale fra Arena", e);
            saveMigrationStatus(
                migrationId,
                tiltaksgjennomforingId,
                tiltakdeltakerId,
                ArenaAgreementMigrationStatus.FAILED,
                null,
                eksternId,
                null,
                agreementAggregate.getTiltakskode(),
                e.getMessage()
            );
        }
    }

    private void saveMigrationStatus(
            UUID id,
            Integer tiltakgjennomforingId,
            Integer tiltakdeltakerId,
            ArenaAgreementMigrationStatus status,
            ArenaMigrationAction action,
            UUID eksternId,
            UUID agreementId,
            ArenaTiltakskode tiltakskode,
            String error
    ) {
        arenaAgreementMigrationRepository.save(
            ArenaAgreementMigration.builder()
                .id(id)
                .tiltakgjennomforingId(tiltakgjennomforingId)
                .tiltakdeltakerId(tiltakdeltakerId)
                .status(status)
                .action(action)
                .avtaleId(agreementId)
                .eksternId(eksternId)
                .modified(LocalDateTime.now())
                .tiltakstype(tiltakskode)
                .error(error)
                .build()
        );
    }

    private ArenaMigrationProcessResult updateAvtale(Avtale avtale, ArenaAgreementAggregate agreementAggregate) {
        ArenaMigrationAction action = ArenaMigrationAction.map(avtale, agreementAggregate);
        switch (action) {
            case IGNORER -> {
                if (agreementAggregate.isDublett()) {
                    log.info(
                        "Avtale med id {} er dublett i Arena. Ignorerer avtalen.",
                        avtale.getId()
                    );
                } else {
                    log.info(
                        "Avtale med id {} og status {} har tiltakstatus {}, deltakerstatus {}. Ignorerer avtalen.",
                        avtale.getId(),
                        avtale.getStatus(),
                        agreementAggregate.getTiltakstatuskode(),
                        agreementAggregate.getDeltakerstatuskode()
                    );
                }
                return new ArenaMigrationProcessResult.Ignored();
            }
            case OPPRETT -> {
                Optional<String> validationAction = validate(avtale, agreementAggregate);
                if (validationAction.isPresent()) {
                    return new ArenaMigrationProcessResult.Failed(validationAction.get());
                }

                log.info(
                    "Avtale med id {} og status {} har tiltakstatus {} og deltakerstatus {} i Arena, " +
                    "men er satt som feilregistrert eller annullert med status 'ANNET' hos oss. " +
                    "Opprettet ny avtale.",
                    avtale.getId(),
                    avtale.getStatus(),
                    agreementAggregate.getTiltakstatuskode(),
                    agreementAggregate.getDeltakerstatuskode()
                );
                return createAvtale(agreementAggregate);
            }
            case GJENOPPRETT, OPPDATER, AVSLUTT, ANNULLER -> {
                Optional<String> validationAction = validate(avtale, agreementAggregate);
                if (validationAction.isPresent()) {
                    return new ArenaMigrationProcessResult.Failed(validationAction.get());
                }

                EndreAvtaleArena.EndreAvtaleArenaBuilder endreAvtaleBuilder = EndreAvtaleArena.builder()
                    .startdato(agreementAggregate.findStartdato().orElse(null))
                    .antallDagerPerUke(agreementAggregate.getAntallDagerPrUke().orElse(null))
                    .stillingprosent(agreementAggregate.getProsentDeltid().orElse(null))
                    .handling(EndreAvtaleArena.Handling.map(action));

                if (!agreementAggregate.isSluttdatoBeforeStartdato() && !agreementAggregate.isDeltakerForGammelPaaSluttDato()) {
                    endreAvtaleBuilder.sluttdato(agreementAggregate.findSluttdato().orElse(null));
                }

                log.info(
                    "Avtale med id {} og status {} har tiltakstatus {} og deltakerstatus {} i Arena. {}.",
                    avtale.getId(),
                    avtale.getStatus(),
                    agreementAggregate.getTiltakstatuskode(),
                    agreementAggregate.getDeltakerstatuskode(),
                    switch (action) {
                        case AVSLUTT -> "Avtalen avsluttes/forkortes";
                        case ANNULLER -> "Annullerer avtalen";
                        case GJENOPPRETT -> "Gjenoppretter avtalen";
                        default -> "Oppdaterer avtalen";
                    }
                );

                avtale.endreAvtaleArena(endreAvtaleBuilder.build());
                return new ArenaMigrationProcessResult.Completed(action, avtale);
            }
            default -> throw new IllegalStateException("Ugyldig handling " + action + " for oppdatering av avtale");
        }
    }

    private ArenaMigrationProcessResult createAvtale(ArenaAgreementAggregate agreementAggregate) {
        ArenaMigrationAction action = ArenaMigrationAction.map(agreementAggregate);

        if (ArenaMigrationAction.IGNORER == action) {
            if (agreementAggregate.isDublett()) {
                log.info(
                    "Avtalen er dublett i Arena. Ignorerer avtalen."
                );
            } else {
                log.info(
                    "Avtale har tiltaksstatus {}, deltakerstatus {}, sluttdato {} i Arena. Ignorerer avtalen.",
                    agreementAggregate.getTiltakstatuskode(),
                    agreementAggregate.getDeltakerstatuskode(),
                    agreementAggregate.findSluttdato().orElse(null)
                );
            }
            return new ArenaMigrationProcessResult.Ignored();
        }

        Optional<Fnr> fnrOpt = agreementAggregate.getFnr();
        if (fnrOpt.isEmpty()) {
            log.info("Avtale mangler fnr og kan derfor ikke opprettes.");
            return new ArenaMigrationProcessResult.Failed("MANGLER_FNR");
        }

        Optional<BedriftNr> bedriftNrOpt = agreementAggregate.getVirksomhetsnummer();
        if (bedriftNrOpt.isEmpty()) {
            log.info("Avtale mangler virksomhetsnummer og kan derfor ikke opprettes.");
            return new ArenaMigrationProcessResult.Failed("MANGLER_VIRKSOMHETSNUMMER");
        }

        Fnr deltakerFnr = fnrOpt.get();
        BedriftNr bedriftNr = bedriftNrOpt.get();
        OpprettAvtale opprettAvtale = OpprettAvtale.builder()
            .bedriftNr(bedriftNr)
            .deltakerFnr(deltakerFnr)
            .tiltakstype(agreementAggregate.getTiltakskode().getTiltakstype())
            .build();

        Avtale avtale = Avtale.opprett(opprettAvtale, Avtaleopphav.ARENA);

        getVirksomhetFromEreg(avtale.getBedriftNr()).ifPresent(
            (org) -> avtale.leggTilBedriftNavn(org.getBedriftNavn())
        );
        avtale.leggTilDeltakerNavn(persondataService.hentNavn(deltakerFnr));

        getGeoEnhetFromNorg2(deltakerFnr).ifPresent((norg2GeoResponse) -> {
            avtale.setEnhetGeografisk(norg2GeoResponse.getEnhetNr());
            avtale.setEnhetsnavnGeografisk(norg2GeoResponse.getNavn());
        });

        getOppfolgingsstatusFromVeilarboppfolging(
            avtale.getDeltakerFnr(),
            agreementAggregate.getTiltakskode().getTiltakstype()
        ).ifPresent((status) -> {
            avtale.setEnhetOppfolging(status.getOppfolgingsenhet());
            avtale.setKvalifiseringsgruppe(status.getKvalifiseringsgruppe());
            avtale.setFormidlingsgruppe(status.getFormidlingsgruppe());
        });

        getOppfolgingsenhetnavnFromNorg2(avtale.getEnhetOppfolging()).ifPresent(avtale::setEnhetsnavnOppfolging);

        AvtaleInnhold avtaleinnhold = avtale.getGjeldendeInnhold();
        Optional.ofNullable(agreementAggregate.getRegDato()).ifPresent(avtale::setOpprettetTidspunkt);
        agreementAggregate.getAntallDagerPrUke().ifPresent(avtaleinnhold::setAntallDagerPerUke);
        agreementAggregate.getProsentDeltid().ifPresent(avtaleinnhold::setStillingprosent);
        agreementAggregate.findStartdato().ifPresent(avtaleinnhold::setStartDato);
        if (!agreementAggregate.isSluttdatoBeforeStartdato() && !agreementAggregate.isDeltakerForGammelPaaSluttDato()) {
            agreementAggregate.findSluttdato().ifPresent(avtaleinnhold::setSluttDato);
        } else {
            log.info(
                "Setter ikke sluttdato på avtalen. {}",
                agreementAggregate.isSluttdatoBeforeStartdato() ?
                    "Sluttdato er før startdato" :
                    "Deltaker er for gammel på sluttdato"
            );
        }

        avtale.setGodkjentForEtterregistrering(true);
        var migreringsdato = agreementAggregate.getTiltakskode().getMigreringsdatoForTilskudd();
        avtale.nyeTilskuddsperioderEtterMigreringFraArena(migreringsdato);
        log.info("Opprettet avtale med id: {}", avtale.getId());
        return new ArenaMigrationProcessResult.Completed(action, avtale);
    }

    private Optional<Norg2GeoResponse> getGeoEnhetFromNorg2(Fnr fnr) {
        return persondataService.hentGeografiskTilknytning(fnr).map(norg2Client::hentGeografiskEnhet);
    }

    private Optional<Organisasjon> getVirksomhetFromEreg(BedriftNr bedriftNr) {
        if (featureToggleService.isEnabled(FeatureToggle.ARENA_EREG_SJEKK)) {
            return Optional.ofNullable(eregService.hentVirksomhet(bedriftNr));
        }

        try {
            return Optional.ofNullable(eregService.hentVirksomhet(bedriftNr));
        } catch (Exception e) {
            log.info(
                "ARENA_EREG_SJEKK toggle er skrudd av. Ignorer derfor feil fra E-Reg med melding: " +
                e.getMessage()
            );
            return Optional.empty();
        }
    }

    private Optional<Oppfølgingsstatus> getOppfolgingsstatusFromVeilarboppfolging(Fnr fnr, Tiltakstype tiltakstype) {
        if (featureToggleService.isEnabled(FeatureToggle.ARENA_OPPFOLGING_SJEKK)) {
            return Optional.ofNullable(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(fnr, tiltakstype));
        }

        try {
            return Optional.ofNullable(veilarboppfolgingService.hentOppfolgingsstatus(fnr.asString()));
        } catch (Exception e) {
            log.info(
                "ARENA_OPPFOLGING_SJEKK toggle er skrudd av. Ignorer derfor feil på oppfølging med melding: " +
                e.getMessage()
            );
            return Optional.empty();
        }
    }

    private Optional<String> getOppfolgingsenhetnavnFromNorg2(String oppfolgingsenhet) {
        return Optional.ofNullable(oppfolgingsenhet != null ? norg2Client.hentOppfølgingsEnhet(oppfolgingsenhet) : null)
            .map(Norg2OppfølgingResponse::getNavn);
    }

    private void transferAktivitetsplankort(Avtale avtale, Integer deltakerId) {
        UUID aktivitetsplanId = aktivitetArenaAclClient.getAktivitetsId(deltakerId);
        hendelseAktivitetsplanClient.putAktivitetsplanId(avtale.getId(), aktivitetsplanId);
    }

    private Optional<String> validate(Avtale avtale, ArenaAgreementAggregate agreementAggregate) {
        if (
            agreementAggregate.getFnr()
                .map(fnr -> !avtale.getDeltakerFnr().equals(fnr))
                .orElse(false)
        ) {
            return Optional.of("FNR_STEMMER_IKKE");
        }

        if (
            agreementAggregate.getVirksomhetsnummer()
                .map(bedriftNr -> !avtale.getBedriftNr().equals(bedriftNr))
                .orElse(false)
        ) {
            return Optional.of("VIRKSOMHETSNUMMER_STEMMER_IKKE");
        }

        if (featureToggleService.isEnabled(FeatureToggle.KODE_6_SPERRE)) {
            boolean isKode6 = agreementAggregate.getFnr()
                .map(fnr -> persondataService.hentDiskresjonskode(fnr).erKode6())
                .orElse(false);

            if (isKode6) {
                return Optional.of("KODE_6");
            }
        }

        return Optional.empty();
    }
}
