package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.persondata.PersondataService.hentGeoLokasjonFraPdlRespons;
import static no.nav.tag.tiltaksgjennomforing.persondata.PersondataService.hentNavnFraPdlRespons;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.*;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.cache.annotation.Cacheable;

@Slf4j
public class Veileder extends Avtalepart<NavIdent> {
    static String ekstraTekstAvtleErGodkjentAvAllePartner =
            "Du må fullføre registreringen i Arena. Avtalen journalføres automatisk i Gosys.";
    private final TilgangskontrollService tilgangskontrollService;

    private final PersondataService persondataService;
    private final SlettemerkeProperties slettemerkeProperties;
    private final TilskuddsperiodeConfig tilskuddsperiodeConfig;
    private final boolean harAdGruppeForBeslutter;
    private final Norg2Client norg2Client;
    private final Set<NavEnhet> navEnheter;
    private final VeilarbArenaClient veilarbArenaClient;

    public Veileder(NavIdent identifikator,
                    TilgangskontrollService tilgangskontrollService,
                    PersondataService persondataService,
                    Norg2Client norg2Client,
                    Set<NavEnhet> navEnheter,
                    SlettemerkeProperties slettemerkeProperties,
                    TilskuddsperiodeConfig tilskuddsperiodeConfig,
                    boolean harAdGruppeForBeslutter,
                    VeilarbArenaClient veilarbArenaClient) {

        super(identifikator);
        this.tilgangskontrollService = tilgangskontrollService;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.navEnheter = navEnheter;
        this.slettemerkeProperties = slettemerkeProperties;
        this.harAdGruppeForBeslutter = harAdGruppeForBeslutter;
        this.veilarbArenaClient = veilarbArenaClient;
        this.tilskuddsperiodeConfig = tilskuddsperiodeConfig;
    }

    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(getIdentifikator(), avtale.getDeltakerFnr());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        if (queryParametre.getVeilederNavIdent() != null) {
            return avtaleRepository.findAllByVeilederNavIdent(queryParametre.getVeilederNavIdent());

        } else if (queryParametre.getDeltakerFnr() != null) {
            return avtaleRepository.findAllByDeltakerFnr(queryParametre.getDeltakerFnr());

        } else if (queryParametre.getBedriftNr() != null) {
            return avtaleRepository.findAllByBedriftNrIn(Set.of(queryParametre.getBedriftNr()));

        } else if (
                queryParametre.getNavEnhet() != null &&
                        queryParametre.getErUfordelt() != null &&
                        queryParametre.getErUfordelt()
        ) {
            return avtaleRepository.findAllUfordelteByEnhet(queryParametre.getNavEnhet());

        } else if (queryParametre.getNavEnhet() != null) {
            return avtaleRepository.findAllFordelteOrUfordeltByEnhet(queryParametre.getNavEnhet());

        } else if (queryParametre.getAvtaleNr() != null) {
            return avtaleRepository.findAllByAvtaleNr(queryParametre.getAvtaleNr());
        } else {
            return avtaleRepository.findAllByVeilederNavIdent(getIdentifikator());
        }
    }

    public void annullerAvtale(Instant sistEndret, String annullerGrunn, Avtale avtale) {
        avtale.sjekkSistEndret(sistEndret);
        avtale.annuller(this, annullerGrunn);
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    void godkjennForAvtalepart(Avtale avtale) {
        if (persondataService.erKode6(avtale.getDeltakerFnr())) {
            throw new KanIkkeGodkjenneAvtalePåKode6Exception();
        }
        if (avtale.getTiltakstype() != Tiltakstype.SOMMERJOBB) {
            veilarbArenaClient.sjekkOppfølingStatus(avtale);
        }
        avtale.godkjennForVeileder(
                getIdentifikator(),
                tilskuddsperiodeConfig.getPilotvirksomheter(),
                tilskuddsperiodeConfig.getPilotenheter()
        );
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker(Avtale avtale) {
        return avtale.erGodkjentAvVeileder();
    }

    @Override
    boolean kanOppheveGodkjenninger(Avtale avtale) {
        return true;
    }

    public void godkjennForVeilederOgDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn, Avtale avtale) {
        sjekkTilgang(avtale);
        if (persondataService.erKode6(avtale.getDeltakerFnr())) {
            throw new KanIkkeGodkjenneAvtalePåKode6Exception();
        }
        if (avtale.getTiltakstype() != Tiltakstype.SOMMERJOBB) {
            veilarbArenaClient.sjekkOppfølingStatus(avtale);
        }
        avtale.godkjennForVeilederOgDeltaker(
                getIdentifikator(),
                paVegneAvGrunn,
                tilskuddsperiodeConfig.getPilotvirksomheter(),
                tilskuddsperiodeConfig.getPilotenheter()
        );
    }

    public void godkjennForVeilederOgArbeidsgiver(
            GodkjentPaVegneAvArbeidsgiverGrunn paVegneAvArbeidsgiverGrunn,
            Avtale avtale
    ) {
        sjekkTilgang(avtale);
        if (persondataService.erKode6(avtale.getDeltakerFnr())) {
            throw new KanIkkeGodkjenneAvtalePåKode6Exception();
        }
        if (avtale.getTiltakstype() != Tiltakstype.SOMMERJOBB) {
            veilarbArenaClient.sjekkOppfølingStatus(avtale);
        }
        avtale.godkjennForVeilederOgArbeidsgiver(
                getIdentifikator(),
                paVegneAvArbeidsgiverGrunn,
                tilskuddsperiodeConfig.getPilotvirksomheter(),
                tilskuddsperiodeConfig.getPilotenheter()
        );
    }

    public void godkjennForVeilederOgDeltakerOgArbeidsgiver(
            GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvDeltakerOgArbeidsgiverGrunn,
            Avtale avtale
    ) {
        sjekkTilgang(avtale);
        if (persondataService.erKode6(avtale.getDeltakerFnr())) {
            throw new KanIkkeGodkjenneAvtalePåKode6Exception();
        }
        if (avtale.getTiltakstype() != Tiltakstype.SOMMERJOBB) {
            veilarbArenaClient.sjekkOppfølingStatus(avtale);
        }
        avtale.godkjennForVeilederOgDeltakerOgArbeidsgiver(
                getIdentifikator(),
                paVegneAvDeltakerOgArbeidsgiverGrunn,
                tilskuddsperiodeConfig.getPilotvirksomheter(),
                tilskuddsperiodeConfig.getPilotenheter()
        );
    }

    @Override
    void opphevGodkjenningerSomAvtalepart(Avtale avtale) {
        avtale.opphevGodkjenningerSomVeileder();
    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.VEILEDER;
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetVeileder(getIdentifikator(), navEnheter, harAdGruppeForBeslutter);
    }

    public void delAvtaleMedAvtalepart(Avtalerolle avtalerolle, Avtale avtale) {
        avtale.delMedAvtalepart(avtalerolle);
    }

    public void overtaAvtale(Avtale avtale) {
        sjekkTilgang(avtale);
        if (this.getIdentifikator().equals(avtale.getVeilederNavIdent())) {
            throw new ErAlleredeVeilederException();
        }
        avtale.overtaAvtale(this.getIdentifikator());
    }

    @Override
    public void endreAvtale(
            Instant sistEndret,
            EndreAvtale endreAvtale,
            Avtale avtale,
            EnumSet<Tiltakstype> tiltakstyperMedTilskuddsperioder,
            List<BedriftNr> pilotvirksomheter,
            List<String> pilotEnheter
    ) {
        sjekkTilgangOgEndreAvtale(
                sistEndret,
                endreAvtale,
                avtale,
                tiltakstyperMedTilskuddsperioder,
                pilotvirksomheter,
                pilotEnheter
        );
        PdlRespons pdlRespons = hentPersondataFraPdl(avtale.getDeltakerFnr().asString());
        hentGeoOgOppfølgingsenhet(avtale, pdlRespons);
    }

    private void hentGeoOgOppfølgingsenhet(Avtale avtale, PdlRespons pdlRespons) {
        this.leggTilGeoEnhet(avtale, pdlRespons);
        this.leggTilOppfølgingEnhet(avtale);
        this.leggTilOppfølgingEnhetsnavnFraNorg2(avtale);
    }

    private void leggTilGeoEnhet(Avtale avtale, PdlRespons pdlRespons) {
        Norg2GeoResponse norg2GeoResponse = hentGeoLokasjonFraPdlRespons(pdlRespons)
                .map(enhet -> hentGeoEnhetFraNorg2(enhet, avtale.getDeltakerFnr().asString())).orElse(null);
        if (norg2GeoResponse != null) {
            avtale.setEnhetGeografisk(norg2GeoResponse.getEnhetNr());
            avtale.setEnhetsnavnGeografisk(norg2GeoResponse.getNavn());
        }
    }

    private void leggTilOppfølgingEnhet(Avtale avtale) {
        String oppfølgingsEnhet = this.hentOppfølgingsenhet(avtale.getDeltakerFnr().asString());
        setEnhetOppfolging(avtale, oppfølgingsEnhet);
    }

    private void leggTilOppfølgingEnhetsnavnFraNorg2(Avtale avtale) {
        final Norg2OppfølgingResponse response = hentOppfølgingsEnhetsnavn(
                avtale.getEnhetOppfolging(),
                avtale.getDeltakerFnr().asString()
        );
        if (response != null) {
            avtale.setEnhetsnavnOppfolging(response.getNavn());
        }
    }

    @Cacheable(value = "oppfolgingenhet", key = "deltakerFnr")
    public String hentOppfølgingsenhet(String deltakerFnr) {
        return veilarbArenaClient.hentOppfølgingsEnhet(deltakerFnr);
    }

    @Cacheable(value = "oppfolgingnavn", key = "deltakerFnr")
    public Norg2OppfølgingResponse hentOppfølgingsEnhetsnavn(String enhetOppfolging, String deltakerFnr) {
        return norg2Client.hentOppfølgingsEnhetsnavn(enhetOppfolging);
    }

    @Cacheable(value = "geoenhet", key = "deltakerFnr")
    public Norg2GeoResponse hentGeoEnhetFraNorg2(String geoTilknytning, String deltakerFnr) {
        return norg2Client.hentGeografiskEnhet(geoTilknytning);
    }

    @Cacheable(value = "pdlResponse", key = "deltakerFnr")
    public PdlRespons hentPersondataFraPdl(String deltakerFnr) {
        return hentPersondata(new Fnr(deltakerFnr));
    }

    private PdlRespons hentPersondata(Fnr deltakerFnr) {
        final PdlRespons persondata = persondataService.hentPersondata(deltakerFnr);
        boolean erKode6 = persondataService.erKode6(persondata);

        if (erKode6) {
            throw new KanIkkeOppretteAvtalePåKode6Exception();
        }
        return persondata;
    }

    public void sjekkTilgangskontroll(NavIdent identifikator, Fnr deltakerFnr) {
        if(!tilgangskontrollService.harSkrivetilgangTilKandidat(identifikator, deltakerFnr)) {
            throw new IkkeTilgangTilDeltakerException();
        }
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        sjekkTilgangskontroll(getIdentifikator(), opprettAvtale.getDeltakerFnr());
        final PdlRespons persondata = this.hentPersondata(opprettAvtale.getDeltakerFnr());
        Avtale avtale = Avtale.veilederOppretterAvtale(opprettAvtale, getIdentifikator());
        avtale.leggTilDeltakerNavn(hentNavnFraPdlRespons(persondata));
        leggTilGeografiskOgOppfølgingsenhet(avtale, persondata, norg2Client, veilarbArenaClient);
        hentOppfølgingFraArenaclient(avtale, veilarbArenaClient);
        return avtale;
    }

    public Avtale opprettMentorAvtale(OpprettMentorAvtale opprettMentorAvtale) {
        sjekkTilgangskontroll(getIdentifikator(), opprettMentorAvtale.getDeltakerFnr());
        final PdlRespons persondata = hentPersondata(opprettMentorAvtale.getDeltakerFnr());
        Avtale avtale = Avtale.veilederOppretterAvtale(opprettMentorAvtale, getIdentifikator());
        avtale.leggTilDeltakerNavn(hentNavnFraPdlRespons(persondata));
        leggTilGeografiskOgOppfølgingsenhet(avtale, persondata, norg2Client, veilarbArenaClient);
        hentOppfølgingFraArenaclient(avtale, veilarbArenaClient);
        return avtale;
    }

    public void slettemerk(Avtale avtale) {
        this.sjekkTilgang(avtale);
        List<NavIdent> identer = slettemerkeProperties.getIdent();
        if (!identer.contains(this.getIdentifikator())) {
            throw new IkkeAdminTilgangException();
        }
        avtale.slettemerk(this.getIdentifikator());
    }

    public void endreMål(EndreMål endreMål, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreMål(endreMål, getIdentifikator());
    }

    public void endreInkluderingstilskudd(EndreInkluderingstilskudd endreInkluderingstilskudd, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreInkluderingstilskudd(endreInkluderingstilskudd, getIdentifikator());
    }

    public void forkortAvtale(Avtale avtale, LocalDate sluttDato, String grunn, String annetGrunn) {
        sjekkTilgang(avtale);
        avtale.forkortAvtale(sluttDato, grunn, annetGrunn, getIdentifikator());
    }

    public void forlengAvtale(LocalDate sluttDato, Avtale avtale) {
        sjekkTilgang(avtale);
        sjekkOgHentOppfølgingStatus(avtale, veilarbArenaClient);
        avtale.forlengAvtale(sluttDato, getIdentifikator());
    }

    public void endreStillingbeskrivelse(EndreStillingsbeskrivelse endreStillingsbeskrivelse, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreStillingsbeskrivelse(endreStillingsbeskrivelse, getIdentifikator());
    }

    public void endreOppfølgingOgTilrettelegging(
            EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging,
            Avtale avtale
    ) {
        sjekkTilgang(avtale);
        avtale.endreOppfølgingOgTilrettelegging(endreOppfølgingOgTilrettelegging, getIdentifikator());
    }

    public void endreOmMentor(EndreOmMentor endreOmMentor, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreOmMentor(endreOmMentor, getIdentifikator());
    }

    public void endreKontaktinfo(EndreKontaktInformasjon endreKontaktInformasjon, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreKontaktInformasjon(endreKontaktInformasjon, getIdentifikator());
    }

    public void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreTilskuddsberegning(endreTilskuddsberegning, getIdentifikator());
    }

    public void sendTilbakeTilBeslutter(Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.sendTilbakeTilBeslutter();
    }

    protected void oppdatereKostnadssted(Avtale avtale, Norg2Client norg2Client, String enhet) {
        final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhetsnavn(enhet);
        if (response == null) {
            throw new FeilkodeException(Feilkode.ENHET_FINNES_IKKE);
        }
        NyttKostnadssted nyttKostnadssted = new NyttKostnadssted(enhet, response.getNavn());
        TreeSet<TilskuddPeriode> tilskuddPerioder = avtale.finnTilskuddsperioderIkkeLukketForEndring();
        if (tilskuddPerioder == null) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_IKKE_SATT);
        }
        avtale.oppdatereKostnadsstedForTilskuddsperioder(nyttKostnadssted);
    }

    private LocalDate settStartDato(LocalDate startdato) {
        return startdato != null ? startdato : LocalDate.now();
    }

    private String settAvtaleId(UUID avtaleId) {
        return avtaleId != null ? avtaleId.toString() : null;
    }

    protected List<AlleredeRegistrertAvtale> hentAvtaleDeltakerAlleredeErRegistrertPaa(
            Fnr deltakerFnr,
            Tiltakstype tiltakstype,
            UUID avtaleId,
            LocalDate startDato,
            LocalDate sluttDato,
            AvtaleRepository avtaleRepository
    ) {
        if(avtaleId != null && startDato != null && sluttDato != null) {
            return AlleredeRegistrertAvtale.filtrerAvtaleDeltakerAlleredeErRegistrertPaa(
                    avtaleRepository.finnAvtalerSomOverlapperForDeltakerVedGodkjenningAvAvtale(
                            deltakerFnr.asString(),
                            avtaleId.toString(),
                            Date.valueOf(settStartDato(startDato)),
                            Date.valueOf(sluttDato)
                    ),
                    tiltakstype
            );
        }
        return AlleredeRegistrertAvtale.filtrerAvtaleDeltakerAlleredeErRegistrertPaa(
                avtaleRepository.finnAvtalerSomOverlapperForDeltakerVedOpprettelseAvAvtale(
                        deltakerFnr.asString(),
                        Date.valueOf(settStartDato(startDato))

                ),
                tiltakstype
        );
    }



    public boolean sjekkOmPilot(SjekkOmPilotRequest sjekkOmPilotRequest) {
        boolean erPilot = false;
        String enhetOppfolging =
                veilarbArenaClient.hentOppfølgingsEnhet(sjekkOmPilotRequest.getDeltakerFnr().asString());
        BedriftNr bedriftNr = sjekkOmPilotRequest.getBedriftNr();
        Tiltakstype tiltakstype = sjekkOmPilotRequest.getTiltakstype();
        List<BedriftNr> pilotvirksomheter = tilskuddsperiodeConfig.getPilotvirksomheter();
        List<String> pilotEnheter = tilskuddsperiodeConfig.getPilotenheter();

        if(
                pilotvirksomheter.contains(bedriftNr) &&
                        (tiltakstype == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD ||
                                tiltakstype == Tiltakstype.VARIG_LONNSTILSKUDD)
        ) {
            erPilot = true;
        }
        if(
                enhetOppfolging != null &&
                        pilotEnheter.contains(enhetOppfolging) &&
                        (tiltakstype == Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD ||
                                tiltakstype == Tiltakstype.VARIG_LONNSTILSKUDD)
        ) {
            erPilot = true;
        }
        return erPilot;
    }


}
