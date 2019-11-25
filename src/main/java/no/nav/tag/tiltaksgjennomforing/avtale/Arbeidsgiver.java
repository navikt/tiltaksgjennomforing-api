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
            if (avtale.heleAvtalenErFyltUt()) {
                if (avtale.erGodkjentAvArbeidsgiver()) {
                    //avtaleStatusDetaljer.header = tekstHeaderAvtaleErGodkjentAvInnloggetBruker;
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
                    //"Hele avtalen er nå fylt ut og klar for godkjenning av deg. Les hele avtalen først. Hvis du er uenig i innholdet, eller har spørsmål til avtalen, bør du kontakte din veileder via Aktivitetsplanen før du godkjenner.";
                }
            } else {
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtalePaabegynt, "", "");
            }
        } else {
            avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleAvbrutt, tekstAvtaleAvbrutt, "");
        }
        avtaleStatusDetaljer.setPart1Detaljer((avtale.getDeltakerFornavn() != null && !avtale.getDeltakerFornavn().equals("") ? avtale.getDeltakerFornavn() : "Deltaker") + " " +
                (avtale.getDeltakerEtternavn() != null && !avtale.getDeltakerEtternavn().equals("") ? avtale.getDeltakerEtternavn() : ""), avtale.erGodkjentAvDeltaker());
        avtaleStatusDetaljer.setPart2Detaljer((avtale.getVeilederFornavn() != null && !avtale.getVeilederFornavn().equals("") ? avtale.getVeilederFornavn() : "Veileder") + " "
                + (avtale.getVeilederEtternavn() != null && !avtale.getVeilederEtternavn().equals("") ? avtale.getVeilederEtternavn() : ""), avtale.erGodkjentAvVeileder());
        //avtaleStatusDetaljer.info=)
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
}
