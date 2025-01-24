package no.nav.tag.tiltaksgjennomforing.arena.service;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.client.acl.AktivitetArenaAclClient;
import no.nav.tag.tiltaksgjennomforing.arena.client.hendelse.HendelseAktivitetsplanClient;
import no.nav.tag.tiltaksgjennomforing.arena.logging.ArenaAgreementLogging;
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
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddsperiodeConfig;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.persondata.PersondataService.hentNavnFraPdlRespons;

@Slf4j
@Service
public class ArenaAgreementProcessingService {
    private final ArenaAgreementMigrationRepository arenaAgreementMigrationRepository;
    private final TilskuddsperiodeConfig tilskuddsperiodeConfig;
    private final AvtaleRepository avtaleRepository;
    private final EregService eregService;
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;
    private final VeilarboppfolgingService veilarboppfolgingService;
    private final AktivitetArenaAclClient aktivitetArenaAclClient;
    private final HendelseAktivitetsplanClient hendelseAktivitetsplanClient;

    public ArenaAgreementProcessingService(
            ArenaAgreementMigrationRepository arenaAgreementMigrationRepository,
            AvtaleRepository avtaleRepository,
            EregService eregService,
            PersondataService persondataService,
            Norg2Client norg2Client,
            VeilarboppfolgingService veilarboppfolgingService,
            TilskuddsperiodeConfig tilskuddsperiodeConfig,
            AktivitetArenaAclClient aktivitetArenaAclClient,
            HendelseAktivitetsplanClient hendelseAktivitetsplanClient) {
        this.arenaAgreementMigrationRepository = arenaAgreementMigrationRepository;
        this.avtaleRepository = avtaleRepository;
        this.eregService = eregService;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.veilarboppfolgingService = veilarboppfolgingService;
        this.tilskuddsperiodeConfig = tilskuddsperiodeConfig;
        this.aktivitetArenaAclClient = aktivitetArenaAclClient;
        this.hendelseAktivitetsplanClient = hendelseAktivitetsplanClient;
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
                        nyAvtale.getId()
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
                        null
                    );
                case ArenaMigrationProcessResult.Failed failed ->
                    saveMigrationStatus(
                        migrationId,
                        tiltaksgjennomforingId,
                        tiltakdeltakerId,
                        ArenaAgreementMigrationStatus.FAILED,
                        failed.action(),
                        eksternId,
                        null
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
                null
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
            UUID agreementId
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
                Optional<ArenaMigrationAction> validationAction = validate(avtale, agreementAggregate);
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
                Optional<ArenaMigrationAction> validationAction = validate(avtale, agreementAggregate);
                if (validationAction.isPresent()) {
                    return new ArenaMigrationProcessResult.Failed(validationAction.get());
                }

                EndreAvtaleArena endreAvtale = EndreAvtaleArena.builder()
                    .startdato(agreementAggregate.findStartdato().orElse(null))
                    .sluttdato(agreementAggregate.findSluttdato().orElse(null))
                    .antallDagerPerUke(Strings.isNullOrEmpty(agreementAggregate.getAntallDagerPrUke()) ? null : Double.parseDouble(agreementAggregate.getAntallDagerPrUke()))
                    .stillingprosent(Strings.isNullOrEmpty(agreementAggregate.getProsentDeltid()) ? null : Double.parseDouble(agreementAggregate.getProsentDeltid()))
                    .handling(EndreAvtaleArena.Handling.map(action))
                    .build();

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

                avtale.endreAvtaleArena(endreAvtale, tilskuddsperiodeConfig.getTiltakstyper());
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

        if (agreementAggregate.getFnr() == null) {
            log.info("Avtale mangler fnr og kan derfor ikke opprettes.");
            return new ArenaMigrationProcessResult.Failed(ArenaMigrationAction.MANGLER_FNR);
        }

        if (agreementAggregate.getVirksomhetsnummer() == null) {
            log.info("Avtale mangler virksomhetsnummer og kan derfor ikke opprettes.");
            return new ArenaMigrationProcessResult.Failed(ArenaMigrationAction.MANGLER_VIRKSOMHETSNUMMER);
        }

        Fnr deltakerFnr = new Fnr(agreementAggregate.getFnr());
        PdlRespons personalData = persondataService.hentPersondata(deltakerFnr);
        if (persondataService.erKode6(personalData)) {
            log.info("Ikke tilgang til deltaker.");
            return new ArenaMigrationProcessResult.Failed(ArenaMigrationAction.KODE_6);
        }

        OpprettAvtale opprettAvtale = OpprettAvtale.builder()
            .bedriftNr(new BedriftNr(agreementAggregate.getVirksomhetsnummer()))
            .deltakerFnr(deltakerFnr)
            .tiltakstype(Tiltakstype.ARBEIDSTRENING)
            .build();

        Avtale avtale = Avtale.opprett(opprettAvtale, Avtaleopphav.ARENA);

        if ("100004368".equals(avtale.getBedriftNr().asString())) {
            avtale.leggTilBedriftNavn("FÖRSÄKRINGSKASSAN");
            try {
                avtale.leggTilDeltakerNavn(hentNavnFraPdlRespons(personalData));
            } catch (Exception e) {
                log.error("Feil ved henting av navn fra PDL", e);
            }
        } else {
            Organisasjon org = eregService.hentVirksomhet(avtale.getBedriftNr());
            avtale.leggTilBedriftNavn(org.getBedriftNavn());
            avtale.leggTilDeltakerNavn(hentNavnFraPdlRespons(personalData));
        }

        getGeoEnhetFromNorg2(personalData).ifPresent((norg2GeoResponse) -> {
            avtale.setEnhetGeografisk(norg2GeoResponse.getEnhetNr());
            avtale.setEnhetsnavnGeografisk(norg2GeoResponse.getNavn());
        });

        getOppfolgingsstatusFromVeilarbarena(avtale.getDeltakerFnr()).ifPresent((status) -> {
            avtale.setEnhetOppfolging(status.getOppfolgingsenhet());
            avtale.setKvalifiseringsgruppe(status.getKvalifiseringsgruppe());
            avtale.setFormidlingsgruppe(status.getFormidlingsgruppe());
        });

        getOppfolgingsenhetnavnFromNorg2(avtale.getEnhetOppfolging()).ifPresent(avtale::setEnhetsnavnOppfolging);

        AvtaleInnhold avtaleinnhold = avtale.getGjeldendeInnhold();

        Optional.ofNullable(agreementAggregate.getRegDato())
            .ifPresent(avtale::setOpprettetTidspunkt);
        agreementAggregate.findStartdato()
            .ifPresent(avtaleinnhold::setStartDato);
        agreementAggregate.findSluttdato()
            .ifPresent(avtaleinnhold::setSluttDato);
        Optional.ofNullable(Strings.isNullOrEmpty(agreementAggregate.getAntallDagerPrUke()) ? null : Double.parseDouble(agreementAggregate.getAntallDagerPrUke()))
            .ifPresent(avtaleinnhold::setAntallDagerPerUke);
        Optional.ofNullable(Strings.isNullOrEmpty(agreementAggregate.getProsentDeltid()) ? null : Double.parseDouble(agreementAggregate.getProsentDeltid()))
            .ifPresent(avtaleinnhold::setStillingprosent);

        avtale.setGodkjentForEtterregistrering(true);
        log.info("Opprettet avtale med id: {}", avtale.getId());
        return new ArenaMigrationProcessResult.Completed(action, avtale);
    }

    private Optional<Norg2GeoResponse> getGeoEnhetFromNorg2(PdlRespons pdlRespons) {
        return PersondataService.hentGeoLokasjonFraPdlRespons(pdlRespons).map(norg2Client::hentGeografiskEnhet);
    }

    private Optional<Oppfølgingsstatus> getOppfolgingsstatusFromVeilarbarena(Fnr fnr) {
        return Optional.ofNullable(veilarboppfolgingService.hentOppfolgingsstatus(fnr.asString()));
    }

    private Optional<String> getOppfolgingsenhetnavnFromNorg2(String oppfolgingsenhet) {
        return Optional.ofNullable(oppfolgingsenhet != null ? norg2Client.hentOppfølgingsEnhet(oppfolgingsenhet) : null)
            .map(Norg2OppfølgingResponse::getNavn);
    }

    private void transferAktivitetsplankort(Avtale avtale, Integer deltakerId) {
        UUID aktivitetsplanId = aktivitetArenaAclClient.getAktivitetsId(deltakerId);
        hendelseAktivitetsplanClient.putAktivietsplanId(avtale.getId(), aktivitetsplanId);
    }

    private Optional<ArenaMigrationAction> validate(Avtale avtale, ArenaAgreementAggregate agreementAggregate) {
        if (
            agreementAggregate.getFnr() != null &&
                !avtale.getDeltakerFnr().asString().equals(agreementAggregate.getFnr())
        ) {
            return Optional.of(ArenaMigrationAction.FNR_STEMMER_IKKE);
        }

        if (
            agreementAggregate.getVirksomhetsnummer() != null &&
                !avtale.getBedriftNr().asString().equals(agreementAggregate.getVirksomhetsnummer())
        ) {
            return Optional.of(ArenaMigrationAction.VIRKSOMHETSNUMMER_STEMMER_IKKE);
        }

        if (agreementAggregate.getFnr() != null) {
            PdlRespons personalData = persondataService.hentPersondata(new Fnr(agreementAggregate.getFnr()));
            if (persondataService.erKode6(personalData)) {
                return Optional.of(ArenaMigrationAction.KODE_6);
            }
        }

        return Optional.empty();
    }
}
