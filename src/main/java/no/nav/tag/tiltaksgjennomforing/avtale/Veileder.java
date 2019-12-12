package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;

import java.time.Instant;
import java.time.LocalDate;

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

    public void avbrytAvtale(Instant sistEndret) {
        avtale.sjekkSistEndret(sistEndret);
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
        if (!avtale.isAvbrutt()) {
            if (avtale.erAltUtfylt()) {
                if (avtale.erGodkjentAvVeileder()) {
                    if (avtale.getStartDato().isAfter(LocalDate.now())) {
                        avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErGodkjentAvAllePartner, tekstAvtaleErGodkjentAvAllePartner + avtale.getStartDato().format(formatter), Veileder.ekstraTekstAvtleErGodkjentAvAllePartner);
                    } else if (avtale.getSluttDato().isAfter(LocalDate.now())) {
                        avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleGjennomfores, "", "");
                    } else {
                        avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErAvsluttet, "", "");
                    }
                } else if (avtale.erGodkjentAvArbeidsgiver() && avtale.erGodkjentAvDeltaker()) {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus
                            (tekstHeaderAvtaleVenterPaaDinGodkjenning, tekstAvtaleVenterPaaDinGodkjenning, "");
                } else {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus
                            (tekstHeaderVentAndreGodkjenning, "", "");
                }
            } else {
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtalePaabegynt, "", "");
            }
        } else {
            avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleAvbrutt, tekstAvtaleAvbrutt, "");
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

    @Override
    public void låsOppAvtale() {
        avtale.låsOppAvtale();
    }
}
