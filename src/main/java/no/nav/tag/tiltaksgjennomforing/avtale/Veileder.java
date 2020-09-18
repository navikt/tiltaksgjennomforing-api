package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.*;

import java.time.Instant;

public class Veileder extends Avtalepart<NavIdent> {
    String venteListeForVeileder;
    static String tekstAvtaleVenterPaaDinGodkjenning = "Før du godkjenner avtalen må du sjekke at alt er i orden og innholdet er riktig.";
    static String ekstraTekstAvtleErGodkjentAvAllePartner = "Du må fullføre registreringen i Arena. Avtalen journalføres automatisk i Gosys.";
    static String tekstAvtaleAvbrutt = "Du eller en annen veileder har avbrutt tiltaket.";

    public Veileder(NavIdent identifikator, Avtale avtale) {
        super(identifikator, avtale);
        this.venteListeForVeileder = "Du må vente for " + (!avtale.erGodkjentAvArbeidsgiver() ? "arbeidsgiver" : "");
        this.venteListeForVeileder = this.venteListeForVeileder +
                ((!avtale.erGodkjentAvDeltaker() ? " og deltaker" : "") + " godkjenner avtale");
    }

    @Override
    public void godkjennForAvtalepart() {
        avtale.godkjennForVeileder(getIdentifikator());
    }

    public void avbrytAvtale(Instant sistEndret, AvbruttInfo avbruttInfo) {
        avtale.sjekkSistEndret(sistEndret);
        avbruttInfo.grunnErOppgitt();
        avtale.avbryt(this, avbruttInfo);
    }

    public void gjenopprettAvtale() {
        avtale.gjenopprett(this);
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale() {
        AvtaleStatusDetaljer avtaleStatusDetaljer = new AvtaleStatusDetaljer();
        avtaleStatusDetaljer.setGodkjentAvInnloggetBruker(erGodkjentAvInnloggetBruker());


        switch (avtale.statusSomEnum()) {
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
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErGodkjentAvAllePartner, tekstAvtaleErGodkjentAvAllePartner + avtale.getStartDato().format(formatter)+".", Veileder.ekstraTekstAvtleErGodkjentAvAllePartner);
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
    public boolean erGodkjentAvInnloggetBruker() {
        return avtale.erGodkjentAvVeileder();
    }


    @Override
    public void sjekkOmAvtaleKanGodkjennes() {
        if (!avtale.erGodkjentAvArbeidsgiver() || !avtale.erGodkjentAvDeltaker()) {
            throw new VeilederSkalGodkjenneSistException();
        }
    }

    @Override
    boolean kanOppheveGodkjenninger() {
        return true;
    }

    @Override
    public Avtalerolle rolle() {
        return Avtalerolle.VEILEDER;
    }

    @Override
    public void godkjennForVeilederOgDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn) {
        if (avtale.erGodkjentAvDeltaker()) {
            throw new DeltakerHarGodkjentException();
        }
        if (!avtale.erGodkjentAvArbeidsgiver()) {
            throw new ArbeidsgiverSkalGodkjenneFørVeilederException();
        }
        paVegneAvGrunn.valgtMinstEnGrunn();
        avtale.godkjennForVeilederOgDeltaker(getIdentifikator(), paVegneAvGrunn);
    }

    @Override
    void opphevGodkjenningerSomAvtalepart() {
        avtale.opphevGodkjenningerSomVeileder();
    }

    @Override
    public void låsOppAvtale() {
        avtale.låsOppAvtale();
    }

    public void delAvtaleMedAvtalepart(Avtalerolle avtalerolle) {
        avtale.delMedAvtalepart(avtalerolle);
    }

    public void overtaAvtale(Avtale avtale) {
        if(this.getIdentifikator().equals(avtale.getVeilederNavIdent())){
            throw new ErAlleredeVeilederException();
        }
        avtale.overtaAvtale(this.getIdentifikator());
    }

}
