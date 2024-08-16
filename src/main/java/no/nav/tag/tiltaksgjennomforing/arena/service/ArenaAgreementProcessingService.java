package no.nav.tag.tiltaksgjennomforing.arena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.arena.logging.ArenaAgreementLogging;
import no.nav.tag.tiltaksgjennomforing.arena.models.arena.ArenaAgreementAggregate;
import no.nav.tag.tiltaksgjennomforing.arena.repository.ArenaAgreementAggregateRepository;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.enhet.*;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static no.nav.tag.tiltaksgjennomforing.persondata.PersondataService.hentNavnFraPdlRespons;

@Slf4j
@Service
public class ArenaAgreementProcessingService {

    private final ArenaAgreementAggregateRepository arenaAgreementAggregateRepository;
    private final AvtaleRepository avtaleRepository;
    private final EregService eregService;
    private final PersondataService persondataService;
    private final Norg2Client norg2Client;
    private final VeilarbArenaClient veilarbArenaClient;

    public ArenaAgreementProcessingService(
            ArenaAgreementAggregateRepository arenaAgreementAggregateRepository,
            AvtaleRepository avtaleRepository,
            EregService eregService,
            PersondataService persondataService,
            Norg2Client norg2Client,
            VeilarbArenaClient veilarbArenaClient
    ) {
        this.arenaAgreementAggregateRepository = arenaAgreementAggregateRepository;
        this.avtaleRepository = avtaleRepository;
        this.eregService = eregService;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.veilarbArenaClient = veilarbArenaClient;
    }

    @ArenaAgreementLogging
    @Async("arenaThreadPoolExecutor")
    public void process(ArenaAgreementAggregate agreementAggregate) {
        Optional<Avtale> exisitingAvtaleOpt = agreementAggregate.getEksternIdAsUuid()
                .flatMap(avtaleRepository::findById);

        Avtale avtale = exisitingAvtaleOpt.orElseGet(() -> createAvtale(agreementAggregate));
        if (exisitingAvtaleOpt.isPresent()) {
            // Oppdater avtale
            return;
        }

        Organisasjon org = getOrgFromEreg(avtale.getBedriftNr());
        avtale.leggTilBedriftNavn(org.getBedriftNavn());

        PdlRespons personalData = getPersondataFromPdl(avtale.getDeltakerFnr());
        avtale.leggTilDeltakerNavn(hentNavnFraPdlRespons(personalData));

        getGeoEnhetFromNorg2(personalData).ifPresent((norg2GeoResponse) -> {
            avtale.setEnhetGeografisk(norg2GeoResponse.getEnhetNr());
            avtale.setEnhetsnavnGeografisk(norg2GeoResponse.getNavn());
        });

        getOppfolgingsstatusFromVeilarbarena(avtale.getDeltakerFnr()).ifPresent((oppfølgingsstatus) -> {
            avtale.setEnhetOppfolging(oppfølgingsstatus.getOppfolgingsenhet());
            avtale.setKvalifiseringsgruppe(oppfølgingsstatus.getKvalifiseringsgruppe());
            avtale.setFormidlingsgruppe(oppfølgingsstatus.getFormidlingsgruppe());
        });

        getOppfolgingsenhetFromNorg2(avtale.getEnhetOppfolging(), avtale.getEnhetGeografisk())
                .ifPresent(avtale::setEnhetOppfolging);

        avtaleRepository.save(avtale);

        // 1. Sjekk om avtalen skal opprettes eller oppdaterees
        // 2. Hent enhet (kun for nye avtaler)
        // 3. Oppdater eller opprett avtale
        // 4. Hent ID fra aktivitetsplan ACL og lagre ID i egen tabell?
        // 5. Meld fra til GoSys (kun for nye avtaler)
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

    private Optional<String> getOppfolgingsenhetFromNorg2(String oppfolgingsenhet, String geografiskEnhet) {
        if (oppfolgingsenhet == null) {
            return Optional.empty();
        }
        if (oppfolgingsenhet.equals(geografiskEnhet)) {
            return Optional.of(geografiskEnhet);
        }

        return Optional.ofNullable(norg2Client.hentOppfølgingsEnhet(oppfolgingsenhet))
                .map(Norg2OppfølgingResponse::getNavn);
    }

    private Avtale createAvtale(ArenaAgreementAggregate agreementAggregate) {
        OpprettAvtale opprettAvtale = OpprettAvtale.builder()
                .bedriftNr(new BedriftNr(agreementAggregate.getVirksomhetsnummer()))
                .deltakerFnr(new Fnr(agreementAggregate.getFnr()))
                .tiltakstype(Tiltakstype.ARBEIDSTRENING)
                .build();

        return Avtale.opprett(opprettAvtale, Avtaleopphav.ARENA);
    }

}
