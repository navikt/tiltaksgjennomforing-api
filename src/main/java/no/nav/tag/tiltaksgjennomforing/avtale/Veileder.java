package no.nav.tag.tiltaksgjennomforing.avtale;

import static java.util.Collections.emptyList;
import static no.nav.tag.tiltaksgjennomforing.persondata.PersondataService.hentNavnFraPdlRespons;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.exceptions.ErAlleredeVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeAdminTilgangException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeGodkjenneAvtalePåKode6Exception;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppretteAvtalePåKode6Eller7Exception;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;

public class Veileder extends Avtalepart<NavIdent> {
    static String tekstAvtaleVenterPaaDinGodkjenning = "Før du godkjenner avtalen må du sjekke at alt er i orden og innholdet er riktig.";
    static String ekstraTekstAvtleErGodkjentAvAllePartner = "Du må fullføre registreringen i Arena. Avtalen journalføres automatisk i Gosys.";
    static String tekstAvtaleAvbrutt = "Du eller en annen veileder har avbrutt tiltaket.";
    private final TilgangskontrollService tilgangskontrollService;

    private final PersondataService persondataService;
    private final SlettemerkeProperties slettemerkeProperties;
    private final Norg2Client norg2Client;
    private Set<String> navEnheter;

    public Veileder(NavIdent identifikator, TilgangskontrollService tilgangskontrollService, PersondataService persondataService,
                    Norg2Client norg2Client, Set<String> navEnheter, SlettemerkeProperties slettemerkeProperties) {
        super(identifikator);
        this.tilgangskontrollService = tilgangskontrollService;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.navEnheter = navEnheter;
        this.slettemerkeProperties = slettemerkeProperties;
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
        } else if (queryParametre.getNavEnhet() != null) {
            return avtaleRepository.findAllUfordelteByEnhet(queryParametre.getNavEnhet());
        } else {
            return emptyList();
        }
    }

    public void avbrytAvtale(Instant sistEndret, AvbruttInfo avbruttInfo, Avtale avtale) {
        avtale.sjekkSistEndret(sistEndret);
        avbruttInfo.grunnErOppgitt();
        avtale.avbryt(this, avbruttInfo);
    }

    public void annullerAvtale(Instant sistEndret, String annullerGrunn, Avtale avtale) {
        avtale.sjekkSistEndret(sistEndret);
        avtale.annuller(this, annullerGrunn);
    }

    public void gjenopprettAvtale(Avtale avtale) {
        avtale.gjenopprett(this);
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale(Avtale avtale) {
        AvtaleStatusDetaljer avtaleStatusDetaljer = new AvtaleStatusDetaljer();
        avtaleStatusDetaljer.setGodkjentAvInnloggetBruker(erGodkjentAvInnloggetBruker(avtale));

        switch (avtale.statusSomEnum()) {
            case ANNULLERT:
                avtaleStatusDetaljer.setInnloggetBrukerStatus("Tiltaket er annullert", "Du eller en annen veileder har annullert tiltaket.", "");
                break;
            case AVBRUTT:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleAvbrutt, tekstAvtaleAvbrutt, "");
                break;
            case PÅBEGYNT:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtalePaabegynt, "", "");
                break;
            case MANGLER_GODKJENNING:
                if (avtale.erGodkjentAvArbeidsgiver() && avtale.erGodkjentAvDeltaker()) {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus
                            (tekstHeaderAvtaleVenterPaaDinGodkjenning, tekstAvtaleVenterPaaDinGodkjenning, "");
                } else {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus
                            (tekstHeaderVentAndreGodkjenning, "", "");
                }
                break;
            case KLAR_FOR_OPPSTART:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErGodkjentAvAllePartner, tekstAvtaleErGodkjentAvAllePartner + avtale.getStartDato().format(formatter) + ".", Veileder.ekstraTekstAvtleErGodkjentAvAllePartner);
                break;
            case GJENNOMFØRES:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleGjennomfores, "", "");
                break;
            case AVSLUTTET:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErAvsluttet, "", "");
                break;
        }

        avtaleStatusDetaljer.setPart1Detaljer((avtale.getBedriftNavn() != null && !avtale.getBedriftNavn().trim().equals("") ? avtale.getBedriftNavn() : "Arbeidsgiver")
                + (avtale.erGodkjentAvArbeidsgiver() ? " har godkjent" : " har ikke godkjent"), avtale.erGodkjentAvArbeidsgiver());
        avtaleStatusDetaljer.setPart2Detaljer((avtale.getDeltakerFornavn() != null && !avtale.getDeltakerFornavn().trim().equals("") ? avtale.getDeltakerFornavn() : "Deltaker") + " " +
                (avtale.getDeltakerEtternavn() != null && !avtale.getDeltakerEtternavn().trim().equals("") ? avtale.getDeltakerEtternavn() + " " : "")
                + (avtale.erGodkjentAvDeltaker() ? "har godkjent" : "har ikke godkjent"), avtale.erGodkjentAvDeltaker());
        return avtaleStatusDetaljer;
    }

    @Override
    void godkjennForAvtalepart(Avtale avtale) {
        if (persondataService.erKode6(avtale.getDeltakerFnr())) {
            throw new KanIkkeGodkjenneAvtalePåKode6Exception();
        }
        avtale.godkjennForVeileder(getIdentifikator());
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
        avtale.godkjennForVeilederOgDeltaker(getIdentifikator(), paVegneAvGrunn);
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
    public void låsOppAvtale(Avtale avtale) {
        avtale.låsOppAvtale();
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetVeileder(getIdentifikator(), navEnheter);
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
        boolean erKode6Eller7 = persondataService.erKode6Eller7(persondata);

        if (erKode6Eller7) {
            throw new KanIkkeOppretteAvtalePåKode6Eller7Exception();
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

    public void forkortAvtale(Instant sistEndret, Avtale avtale, LocalDate sluttDato, String grunn, String annetGrunn) {
        sjekkTilgang(avtale);
        avtale.forkortAvtale(sluttDato, grunn, annetGrunn, getIdentifikator());
    }

    public void forlengAvtale(Instant sistEndret, LocalDate sluttDato, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.forlengAvtale(sluttDato);
    }


    public void endreStillingsinfo(EndreStillingsbeskrivelse endreStillingsbeskrivelse, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreStillingsbeskrivelse(endreStillingsbeskrivelse);
    }

    public void endreOppfølgingOgTilrettelegginginfo(EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreOppfølgingOgTilrettelegging(endreOppfølgingOgTilrettelegging);
    }

    public void endreKontaktinfo(EndreKontaktInformasjon endreKontaktInformasjon, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreKontaktInformasjon(endreKontaktInformasjon);
    }

    public void endreTilskuddsberegning(Instant sistEndret, EndreTilskuddsberegning endreTilskuddsberegning, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.endreTilskuddsberegning(endreTilskuddsberegning);
    }

    public void sendTilbakeTilBeslutter(Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.sendTilbakeTilBeslutter();
    }
}
