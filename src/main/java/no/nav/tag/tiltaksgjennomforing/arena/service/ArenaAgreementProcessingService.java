package no.nav.tag.tiltaksgjennomforing.arena.service;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.logging.ArenaAgreementLogging;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigration;
import no.nav.tag.tiltaksgjennomforing.arena.models.migration.ArenaAgreementMigrationStatus;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementMigrationRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleInnhold;
import no.nav.tag.tiltaksgjennomforing.avtale.AvtaleRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

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
            TilskuddsperiodeConfig tilskuddsperiodeConfig
    ) {
        this.agreementMigrationRepository = agreementMigrationRepository;
        this.avtaleRepository = avtaleRepository;
        this.eregService = eregService;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.veilarbArenaClient = veilarbArenaClient;
        this.tilskuddsperiodeConfig = tilskuddsperiodeConfig;
    }

    @ArenaAgreementLogging
    @Async("arenaThreadPoolExecutor")
    public void process(ArenaAgreementAggregate agreementAggregate) {
        BiConsumer<ArenaAgreementMigrationStatus, UUID> updateMigrationStatus = updateMigrationStatus(agreementAggregate.getTiltakgjennomforingId());
        updateMigrationStatus.accept(ArenaAgreementMigrationStatus.PROCESSING, null);

        try {
            Pair<Avtale, ArenaAgreementMigrationStatus> result = agreementAggregate.getEksternIdAsUuid()
                    .flatMap(avtaleRepository::findById)
                    .map((existingAvtale) -> {
                        Avtale updatedAvtale = updateAvtale(existingAvtale, agreementAggregate);
                        return new Pair<>(updatedAvtale, ArenaAgreementMigrationStatus.UPDATED);
                    })
                    .orElseGet(() -> {
                        Avtale createdAvtale = createAvtale(agreementAggregate);
                        return new Pair<>(createdAvtale, ArenaAgreementMigrationStatus.CREATED);
                    });

            Avtale avtale = result.getFirst();
            ArenaAgreementMigrationStatus status = result.getSecond();

            avtaleRepository.save(avtale);
            updateMigrationStatus.accept(status, avtale.getId());
        } catch(Exception e) {
            log.error("Feil ved prossesering av avtale fra Arena", e);
            updateMigrationStatus.accept(ArenaAgreementMigrationStatus.FAILED, null);
        }
    }

    private BiConsumer<ArenaAgreementMigrationStatus, UUID> updateMigrationStatus(Integer id) {
        return (status, agreementId) -> {
            agreementMigrationRepository.save(
                ArenaAgreementMigration.builder()
                    .tiltakgjennomforingId(id)
                    .status(status)
                    .agreementId(agreementId)
                    .modified(LocalDateTime.now())
                    .build()
            );
        };
    }

    private Avtale updateAvtale(Avtale avtale, ArenaAgreementAggregate agreementAggregate) {
        if (!avtale.getDeltakerFnr().equals(new Fnr(agreementAggregate.getFnr()))) {
            throw new IllegalArgumentException("Fnr i avtale matcher ikke fnr fra Arena");
        }

        if (!avtale.getBedriftNr().equals(new BedriftNr(agreementAggregate.getVirksomhetsnummer()))) {
            throw new IllegalArgumentException("Virksomhetsnummer i avtale matcher ikke virksomhetsnummer fra Arena");
        }

        EndreAvtaleArena endreAvtale = EndreAvtaleArena.builder()
                .startDato(agreementAggregate.getDatoFra() != null ? agreementAggregate.getDatoFra().toLocalDate() : null)
                .sluttDato(agreementAggregate.getDatoTil() != null ? agreementAggregate.getDatoTil().toLocalDate() : null)
                .antallDagerPerUke(agreementAggregate.getAntallDagerPrUke() != null ? Integer.parseInt(agreementAggregate.getAntallDagerPrUke()) : null)
                .stillingprosent(agreementAggregate.getProsentDeltid())
                .build();

        avtale.endreAvtaleArena(endreAvtale, tilskuddsperiodeConfig.getTiltakstyper());

        log.info("Oppdatert avtale med id: {}", avtale.getId());

        return avtale;
    }

    private Avtale createAvtale(ArenaAgreementAggregate agreementAggregate) {
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

        return avtale;
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
