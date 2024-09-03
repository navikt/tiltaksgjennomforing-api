package no.nav.tag.tiltaksgjennomforing.arena.service;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.logging.ArenaAgreementLogging;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Deltakerstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.Tiltakstatuskode;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigration;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigrationStatus;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaMigrationAction;
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
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
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
    private final ArenaAgreementMigrationRepository agreementMigrationRepository;
    private final TilskuddsperiodeConfig tilskuddsperiodeConfig;
    private final AvtaleRepository avtaleRepository;
    private final EregService eregService;
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;
    private final VeilarbArenaClient veilarbArenaClient;

    public ArenaAgreementProcessingService(
            ArenaAgreementMigrationRepository agreementMigrationRepository,
            AvtaleRepository avtaleRepository,
            EregService eregService,
            PersondataService persondataService,
            Norg2Client norg2Client,
            VeilarbArenaClient veilarbArenaClient,
            TilskuddsperiodeConfig tilskuddsperiodeConfig) {
        this.agreementMigrationRepository = agreementMigrationRepository;
        this.avtaleRepository = avtaleRepository;
        this.eregService = eregService;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.veilarbArenaClient = veilarbArenaClient;
        this.tilskuddsperiodeConfig = tilskuddsperiodeConfig;
    }

    @Transactional
    @ArenaAgreementLogging
    @Async("arenaThreadPoolExecutor")
    public void process(ArenaAgreementAggregate agreementAggregate) {
        Integer tiltaksgjennomforingId = agreementAggregate.getTiltakgjennomforingId();
        updateMigrationStatus(tiltaksgjennomforingId, ArenaAgreementMigrationStatus.PROCESSING, null);

        try {
            Pair<Optional<Avtale>, ArenaAgreementMigrationStatus> result = agreementAggregate.getEksternIdAsUuid()
                    .flatMap(avtaleRepository::findById)
                    .map((existingAvtale) -> updateAvtale(existingAvtale, agreementAggregate))
                    .orElseGet(() -> createAvtale(agreementAggregate));

            Optional<Avtale> avtaleOpt = result.getFirst();
            ArenaAgreementMigrationStatus status = result.getSecond();

            avtaleOpt.ifPresent(avtaleRepository::save);
            updateMigrationStatus(tiltaksgjennomforingId, status, avtaleOpt.map(Avtale::getId).orElse(null));
        } catch(Exception e) {
            log.error("Feil ved prossesering av avtale fra Arena", e);
            updateMigrationStatus(tiltaksgjennomforingId, ArenaAgreementMigrationStatus.FAILED, null);
        }
    }

    private void updateMigrationStatus(
            Integer id,
            ArenaAgreementMigrationStatus status,
            UUID agreementId
    ) {
        agreementMigrationRepository.save(
            ArenaAgreementMigration.builder()
                .tiltakgjennomforingId(id)
                .status(status)
                .avtaleId(agreementId)
                .modified(LocalDateTime.now())
                .build()
        );
    }

    private Pair<Optional<Avtale>, ArenaAgreementMigrationStatus> updateAvtale(Avtale avtale, ArenaAgreementAggregate agreementAggregate) {
        if (!avtale.getDeltakerFnr().equals(new Fnr(agreementAggregate.getFnr()))) {
            throw new IllegalStateException("Fnr i avtale stemmer ikke med fnr fra Arena");
        }

        if (!avtale.getBedriftNr().equals(new BedriftNr(agreementAggregate.getVirksomhetsnummer()))) {
            throw new IllegalStateException("Virksomhetsnummer i avtale stemmer ikke med virksomhetsnummer fra Arena");
        }

        Tiltakstatuskode tiltakstatuskode = agreementAggregate.getTiltakstatuskode();
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();

        ArenaMigrationAction action = ArenaMigrationAction.map(avtale, tiltakstatuskode, deltakerstatuskode);
        switch (action) {
            case CREATE -> {
                log.info(
                    "Avtale med id {} har tiltakstatus {} og deltakerstatus {} i Arena, " +
                    "men er satt som feilregistrert eller annullert med status 'ANNET' hos oss. " +
                    "Opprettet ny avtale.",
                    avtale.getId(),
                    tiltakstatuskode,
                    deltakerstatuskode
                );
                return createAvtale(agreementAggregate);
            }
            case UPDATE, END, TERMINATE -> {
                EndreAvtaleArena endreAvtale = EndreAvtaleArena.builder()
                    .startDato(agreementAggregate.getDatoFra() != null ? agreementAggregate.getDatoFra().toLocalDate() : null)
                    .sluttDato(agreementAggregate.getDatoTil() != null ? agreementAggregate.getDatoTil().toLocalDate() : null)
                    .antallDagerPerUke(agreementAggregate.getAntallDagerPrUke() != null ? Integer.parseInt(agreementAggregate.getAntallDagerPrUke()) : null)
                    .stillingprosent(agreementAggregate.getProsentDeltid())
                    .handling(EndreAvtaleArena.Handling.map(action))
                    .build();

                log.info(
                    "Avtale med id {} har tiltakstatus {} og deltakerstatus {} i Arena. {}.",
                    avtale.getId(),
                    tiltakstatuskode,
                    deltakerstatuskode,
                    switch (action) {
                        case END -> "Avtalen avsluttes/forkortes";
                        case TERMINATE -> "Annullerer avtalen";
                        default -> "Oppdatert avtalen";
                    }
                );

                avtale.endreAvtaleArena(endreAvtale, tilskuddsperiodeConfig.getTiltakstyper());
                return new Pair<>(Optional.of(avtale), ArenaAgreementMigrationStatus.UPDATED);
            }
            default -> throw new IllegalStateException("Ugyldig handling " + action + " for oppdatering av avtale");
        }
    }

    private Pair<Optional<Avtale>, ArenaAgreementMigrationStatus> createAvtale(ArenaAgreementAggregate agreementAggregate) {
        Tiltakstatuskode tiltakstatuskode = agreementAggregate.getTiltakstatuskode();
        Deltakerstatuskode deltakerstatuskode = agreementAggregate.getDeltakerstatuskode();

        ArenaMigrationAction action = ArenaMigrationAction.map(
            tiltakstatuskode,
            deltakerstatuskode
        );

        if (ArenaMigrationAction.IGNORE == action) {
            log.info(
                "Avtale har tiltaksstatus {} og deltakerstatus {} i Arena. Ignorerer avtalen.",
                tiltakstatuskode,
                deltakerstatuskode
            );
            return new Pair<>(Optional.empty(), ArenaAgreementMigrationStatus.IGNORED);
        }

        OpprettAvtale opprettAvtale = OpprettAvtale.builder()
            .bedriftNr(new BedriftNr(agreementAggregate.getVirksomhetsnummer()))
            .deltakerFnr(new Fnr(agreementAggregate.getFnr()))
            .tiltakstype(Tiltakstype.ARBEIDSTRENING)
            .build();

        Avtale avtale = Avtale.opprett(opprettAvtale, Avtaleopphav.ARENA);

        Organisasjon org = getOrgFromEreg(avtale.getBedriftNr());
        avtale.leggTilBedriftNavn(org.getBedriftNavn());

        PdlRespons personalData = getPersondataFromPdl(avtale.getDeltakerFnr());
        avtale.leggTilDeltakerNavn(hentNavnFraPdlRespons(personalData));

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
        Optional.ofNullable(agreementAggregate.getDatoFra() != null ? agreementAggregate.getDatoFra().toLocalDate() : null)
            .ifPresent(avtaleinnhold::setStartDato);
        Optional.ofNullable(agreementAggregate.getDatoTil() != null ? agreementAggregate.getDatoTil().toLocalDate() : null)
            .ifPresent(avtaleinnhold::setSluttDato);
        Optional.ofNullable(agreementAggregate.getAntallDagerPrUke() != null ? Integer.parseInt(agreementAggregate.getAntallDagerPrUke()) : null)
            .ifPresent(avtaleinnhold::setAntallDagerPerUke);
        Optional.ofNullable(agreementAggregate.getProsentDeltid())
            .ifPresent(avtaleinnhold::setStillingprosent);

        avtale.setGodkjentForEtterregistrering(true);
        log.info("Opprettet avtale med id: {}", avtale.getId());
        return new Pair<>(Optional.of(avtale), ArenaAgreementMigrationStatus.CREATED);
    }

    private Organisasjon getOrgFromEreg(BedriftNr bedriftNr) {
        return eregService.hentVirksomhet(bedriftNr);
    }

    private PdlRespons getPersondataFromPdl(Fnr fnr) {
        return persondataService.hentPersondata(fnr);
    }

    private Optional<Norg2GeoResponse> getGeoEnhetFromNorg2(PdlRespons pdlRespons) {
        return PersondataService.hentGeoLokasjonFraPdlRespons(pdlRespons)
                .map(norg2Client::hentGeografiskEnhet);
    }

    private Optional<Oppfølgingsstatus> getOppfolgingsstatusFromVeilarbarena(Fnr fnr) {
        return Optional.ofNullable(veilarbArenaClient.HentOppfølgingsenhetFraCacheEllerArena(fnr.asString()));
    }

    private Optional<String> getOppfolgingsenhetnavnFromNorg2(String oppfolgingsenhet) {
        return Optional.ofNullable(oppfolgingsenhet != null ? norg2Client.hentOppfølgingsEnhet(oppfolgingsenhet) : null)
            .map(Norg2OppfølgingResponse::getNavn);
    }

}
