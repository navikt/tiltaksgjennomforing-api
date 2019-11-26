package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

import java.time.LocalDate;


public class Arbeidsgiver extends Avtalepart<Fnr> {
    public Arbeidsgiver(Fnr identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    static String tekstAvtaleVenterPaaDinGodkjenning = "Deltakeren trenger ikke å godkjenne avtalen før du gjør det. ";

    static String ekstraTekstAvtaleVenterPaaDinGodkjenning = "Veilederen skal godkjenne til slutt.";
    static String tekstTiltaketErAvsluttet = "Hvis du har spørsmål må du kontakte NAV.";

    @Override
    public void godkjennForAvtalepart() {
        avtale.godkjennForArbeidsgiver(getIdentifikator());
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale() {
        AvtaleStatusDetaljer avtaleStatusDetaljer = new AvtaleStatusDetaljer();
        if (!avtale.isAvbrutt()) {
            if (avtale.erAltUtfylt()) {
                if (avtale.erGodkjentAvArbeidsgiver()) {
                    if (avtale.erGodkjentAvVeileder()) {
                        if (avtale.getStartDato().isAfter(LocalDate.now())) {
                            avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErGodkjentAvAllePartner, tekstAvtaleErGodkjentAvAllePartner + avtale.getStartDato().format(formatter), "");
                        } else if (avtale.getSluttDato().isAfter(LocalDate.now())) {
                            avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleGjennomfores, "", "");
                        } else {
                            avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErAvsluttet, tekstTiltaketErAvsluttet, "");
                        }

                    } else if (avtale.erGodkjentAvDeltaker()) {
                        avtaleStatusDetaljer.setInnloggetBrukerStatus(
                                tekstHeaderVentAndreGodkjenning, "", "");
                    } else {
                        avtaleStatusDetaljer.setInnloggetBrukerStatus(
                                tekstHeaderVentAndreGodkjenning, "", "");
                    }
                } else {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderAvtaleVenterPaaDinGodkjenning,
                            Arbeidsgiver.tekstAvtaleVenterPaaDinGodkjenning, ekstraTekstAvtaleVenterPaaDinGodkjenning);
                }
            } else {
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtalePaabegynt, "", "");
            }
        } else {
            avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleAvbrutt, tekstAvtaleAvbrutt, "");
        }
        avtaleStatusDetaljer.setPart1Detaljer((avtale.getDeltakerFornavn() != null && !avtale.getDeltakerFornavn().trim().equals("") ? avtale.getDeltakerFornavn() : "Deltaker") + " " +
                (avtale.getDeltakerEtternavn() != null && !avtale.getDeltakerEtternavn().trim().equals("") ? avtale.getDeltakerEtternavn() + " " : "")
                + (avtale.erGodkjentAvDeltaker() ? "har godkjent" : "har ikke godkjent"), avtale.erGodkjentAvDeltaker());
        avtaleStatusDetaljer.setPart2Detaljer((avtale.getVeilederFornavn() != null && !avtale.getVeilederFornavn().trim().equals("") ? avtale.getVeilederFornavn() : "Veileder") + " "
                + (avtale.getVeilederEtternavn() != null && !avtale.getVeilederEtternavn().trim().equals("") ? avtale.getVeilederEtternavn() + " " : "")
                + (avtale.erGodkjentAvVeileder() ? "har godkjent" : "venter på din godkjenning"), avtale.erGodkjentAvVeileder());
        return avtaleStatusDetaljer;
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker() {
        return avtale.erGodkjentAvArbeidsgiver();
    }

    @Override
    boolean kanOppheveGodkjenninger() {
        return !avtale.erGodkjentAvVeileder();
    }

    @Override
    public Avtalerolle rolle() {
        return Avtalerolle.ARBEIDSGIVER;
    }

    @Override
    public void godkjennForVeilederOgDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn) {
        throw new TilgangskontrollException("Arbeidsgiver kan ikke godkjenne som veileder");
    }

    @Override
    void opphevGodkjenningerSomAvtalepart() {
        avtale.opphevGodkjenningerSomArbeidsgiver();
    }

    @Override
    public void låsOppAvtale() {
        throw new TilgangskontrollException("Arbeidsgiver kan ikke låse opp avtale");
    }
}
