package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;

public class Veileder extends Avtalepart<NavIdent> {
    static String tekstAvtaleErIkkkeFyltUt = "Som veileder kan du fylle ut avtalen i samarbeid med arbeidsgiver. Avtalen kan godkjennes etter at den er fylt ut.";
    String venteListeForVeileder;
    static String tekstForklarerVenting = "Som veileder, er det du som godkjenner sist. Det er på grun av at etter dign godkjenning er vel avtalen låst og klar for oppstart, " +
            "Dette bør skjer før dagen for tiltak starter.....";
    static String tekstGodkjenneForDeltaker = "Som veileder, kan du også godkjenne på vegne av deltaker hvis det er behov for det... Dette kan sje etter at arbeidsgiver har godkjent avtalen";

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

    public void avbrytAvtale(Integer versjon) {
        avtale.sjekkVersjon(versjon);
        avtale.avbryt(this);
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale() {
        AvtaleStatusDetaljer avtaleStatusDetaljer = new AvtaleStatusDetaljer();
        avtaleStatusDetaljer.setGodkjentAvInnloggetBruker(erGodkjentAvInnloggetBruker());
        if (avtale.heleAvtalenErFyltUt()) {
            if (avtale.erGodkjentAvVeileder()) {
                avtaleStatusDetaljer.setInnloggetBrukerStatus
                        (tekstHeaderAvtaleErGodkjentAvInnloggetBruker, tekstAvtaleErGodkjentAvAllePartner, ekstraTekstAvtaleErGodkjentAvAllePartner);
            } else if (avtale.erGodkjentAvArbeidsgiver() && avtale.erGodkjentAvDeltaker()) {
                avtaleStatusDetaljer.setInnloggetBrukerStatus
                        (tekstHeaderAvtaleVenterPaaDinGodkjenning, tekstAvtaleVenterPaaDinGodkjenning, ekstraTekstAvtaleVenterPaaDinGodkjenning);
            } else {
                avtaleStatusDetaljer.setInnloggetBrukerStatus
                        (this.venteListeForVeileder, tekstForklarerVenting, !avtale.erGodkjentAvDeltaker() ? tekstGodkjenneForDeltaker : "");
            }
        } else {
            avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErIkkkeFyltUt, tekstAvtaleErIkkkeFyltUt, "");
        }
        avtaleStatusDetaljer.setPart1Detaljer(avtale.getBedriftNavn() + " /v " + avtale.getArbeidsgiverFornavn(), avtale.erGodkjentAvArbeidsgiver());
        avtaleStatusDetaljer.setPart2Detaljer(avtale.getDeltakerFornavn() + " " + avtale.getDeltakerEtternavn(), avtale.erGodkjentAvDeltaker());
        //avtaleStatusDetaljer.info=)
        return avtaleStatusDetaljer;
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker() {
        return avtale.erGodkjentAvVeileder();
    }


    @Override
    public void sjekkOmAvtaleKanGodkjennes() {
        if (!avtale.erGodkjentAvArbeidsgiver() || !avtale.erGodkjentAvDeltaker()) {
            throw new TiltaksgjennomforingException("Veileder må godkjenne avtalen etter deltaker og arbeidsgiver.");
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
            throw new TiltaksgjennomforingException("Deltaker har allerde godkjent avtalen");
        }
        if (!avtale.erGodkjentAvArbeidsgiver()) {
            throw new TiltaksgjennomforingException("Arbeidsgiver må godkjenne avtalen før veileder kan godkjenne");
        }
        paVegneAvGrunn.valgtMinstEnGrunn();
        avtale.godkjennForVeilederOgDeltaker(getIdentifikator(), paVegneAvGrunn);
    }

    @Override
    void opphevGodkjenningerSomAvtalepart() {
        avtale.opphevGodkjenningerSomVeileder();
    }
}
