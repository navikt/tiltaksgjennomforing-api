package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.persondata.PersondataService.hentNavnFraPdlRespons;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.ErAlleredeVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeAdminTilgangException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeGodkjenneAvtalePåKode6Exception;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppretteAvtalePåKode6Exception;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.springframework.web.bind.annotation.RequestParam;


public class Veileder extends Avtalepart<NavIdent> {
    static String ekstraTekstAvtleErGodkjentAvAllePartner = "Du må fullføre registreringen i Arena. Avtalen journalføres automatisk i Gosys.";
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

        } else if (queryParametre.getNavEnhet() != null && queryParametre.getErUfordelt() != null && queryParametre.getErUfordelt()) {

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
        avtale.godkjennForVeileder(getIdentifikator(), tilskuddsperiodeConfig.getPilotvirksomheter());
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
        avtale.godkjennForVeilederOgDeltaker(getIdentifikator(), paVegneAvGrunn, tilskuddsperiodeConfig.getPilotvirksomheter());
    }

    public void godkjennForVeilederOgArbeidsgiver(GodkjentPaVegneAvArbeidsgiverGrunn paVegneAvArbeidsgiverGrunn, Avtale avtale) {
        sjekkTilgang(avtale);
        if (persondataService.erKode6(avtale.getDeltakerFnr())) {
            throw new KanIkkeGodkjenneAvtalePåKode6Exception();
        }
        if (avtale.getTiltakstype() != Tiltakstype.SOMMERJOBB) {
            veilarbArenaClient.sjekkOppfølingStatus(avtale);
        }
        avtale.godkjennForVeilederOgArbeidsgiver(getIdentifikator(), paVegneAvArbeidsgiverGrunn, tilskuddsperiodeConfig.getPilotvirksomheter());
    }

    public void godkjennForVeilederOgDeltakerOgArbeidsgiver(GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvDeltakerOgArbeidsgiverGrunn, Avtale avtale) {
        sjekkTilgang(avtale);
        if (persondataService.erKode6(avtale.getDeltakerFnr())) {
            throw new KanIkkeGodkjenneAvtalePåKode6Exception();
        }
        if (avtale.getTiltakstype() != Tiltakstype.SOMMERJOBB) {
            veilarbArenaClient.sjekkOppfølingStatus(avtale);
        }
        avtale.godkjennForVeilederOgDeltakerOgArbeidsgiver(getIdentifikator(), paVegneAvDeltakerOgArbeidsgiverGrunn, tilskuddsperiodeConfig.getPilotvirksomheter());
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

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        boolean harAbacTilgang = tilgangskontrollService.harSkrivetilgangTilKandidat(getIdentifikator(), opprettAvtale.getDeltakerFnr());
        if (!harAbacTilgang) {
            throw new IkkeTilgangTilDeltakerException();
        }

        final PdlRespons persondata = persondataService.hentPersondata(opprettAvtale.getDeltakerFnr());
        boolean erKode6 = persondataService.erKode6(persondata);

        if (erKode6) {
            throw new KanIkkeOppretteAvtalePåKode6Exception();
        }

        Avtale avtale = Avtale.veilederOppretterAvtale(opprettAvtale, getIdentifikator());
        avtale.leggTilDeltakerNavn(hentNavnFraPdlRespons(persondata));
        leggTilGeografiskEnhet(avtale, persondata, norg2Client);
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

    public void endreOppfølgingOgTilrettelegging(EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging, Avtale avtale) {
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
        if (response != null) {
            NyttKostnadssted nyttKostnadssted = new NyttKostnadssted(enhet, response.getNavn());
            TreeSet<TilskuddPeriode> tilskuddPerioder = avtale.finnTilskuddsperioderIkkeLukketForEndring();
            if (tilskuddPerioder == null) {
                throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_IKKE_SATT);
            }
            avtale.oppdatereKostnadsstedForTilskuddsperioder(nyttKostnadssted);
        } else {
            throw new FeilkodeException(Feilkode.ENHET_FINNES_IKKE);
        }
    }

    protected void hentAvtaleDeltakerAlleredeErRegistrertPaa(
            Fnr deltakerFnr,
            Tiltakstype tiltakstype,
            UUID avtaleId,
            boolean alleredeOpprettetAvtale
    ) {

    }

    public Avtale opprettMentorAvtale(OpprettMentorAvtale opprettMentorAvtale) {
        if (!tilgangskontrollService.harSkrivetilgangTilKandidat(getIdentifikator(), opprettMentorAvtale.getDeltakerFnr())) {
            throw new IkkeTilgangTilDeltakerException();
        }

        final PdlRespons persondata = persondataService.hentPersondata(opprettMentorAvtale.getDeltakerFnr());
        boolean erKode6 = persondataService.erKode6(persondata);

        if (erKode6) {
            throw new KanIkkeOppretteAvtalePåKode6Exception();
        }

        Avtale avtale = Avtale.veilederOppretterAvtale(opprettMentorAvtale, getIdentifikator());
        avtale.leggTilDeltakerNavn(hentNavnFraPdlRespons(persondata));
        leggTilGeografiskEnhet(avtale, persondata, norg2Client);
        return avtale;
    }
}
