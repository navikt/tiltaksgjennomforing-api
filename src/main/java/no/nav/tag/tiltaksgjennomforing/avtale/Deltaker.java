package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

public class Deltaker extends Avtalepart<Fnr> {

    public Deltaker(Fnr identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    static String tekstHeaderAvtalePaabegynt = "Avtale påbegynt";
    static String tekstAvtalePaabegynt = "Du kan først godkjenne avtalen når arbeidsgiveren og veilederen har fylt ut avtalen.";
    static String tekstAvtaleVenterPaaDinGodkjenning = "Les hele avtalen først. Du kan ikke endre teksten i avtalen. ";
    static String ekstraTekstAvtaleVenterPaaDinGodkjenning = "Hvis du er uenig i innholdet, eller har spørsmål til avtalen, må du kontakte din veilederen din via Aktivitetsplanen før du godkjenner.";

    @Override
    public void godkjennForAvtalepart() {
        avtale.godkjennForDeltaker(getIdentifikator());
    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale() {
        AvtaleStatusDetaljer avtaleStatusDetaljer = new AvtaleStatusDetaljer();
        avtaleStatusDetaljer.setGodkjentAvInnloggetBruker(erGodkjentAvInnloggetBruker());
        if (avtale.heleAvtalenErFyltUt()) {
            if (avtale.erGodkjentAvDeltaker()) {
                if (avtale.erGodkjentAvVeileder()) {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderAvtaleErGodkjentAvInnloggetBruker, tekstAvtaleErGodkjentAvAllePartner, ekstraTekstAvtaleErGodkjentAvAllePartner);
                } else if (avtale.erGodkjentAvArbeidsgiver()) {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderAvtaleErGodkjentAvInnloggetBruker, tekstAvtaleVenterPaaVeilederGodkjenning, ekstraTekstAvtaleVenterPaaVeilederGodkjenning);
                } else {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderAvtaleErGodkjentAvInnloggetBruker, tekstAvtaleVenterPaaAndrepartnerGodkjenning, ekstraTekstAvtaleVenterPaaAndrePartnerGodkjenning);
                }
            } else {
                avtaleStatusDetaljer.setInnloggetBrukerStatus(
                        tekstHeaderAvtaleVenterPaaDinGodkjenning, tekstAvtaleVenterPaaDinGodkjenning, ekstraTekstAvtaleVenterPaaDinGodkjenning);
            }
        } else {
            avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtalePaabegynt, tekstAvtalePaabegynt, "");
        }
        avtaleStatusDetaljer.setPart1Detaljer(avtale.getBedriftNavn() + " /v" + avtale.getArbeidsgiverFornavn(), avtale.erGodkjentAvArbeidsgiver());
        avtaleStatusDetaljer.setPart2Detaljer(avtale.getVeilederFornavn() + " " + avtale.getVeilederEtternavn(), avtale.erGodkjentAvVeileder());
        //avtaleStatusDetaljer.info",)
        return avtaleStatusDetaljer;
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker() {
        return avtale.erGodkjentAvDeltaker();
    }


    @Override
    boolean kanOppheveGodkjenninger() {
        return false;
    }

    @Override
    public Avtalerolle rolle() {
        return Avtalerolle.DELTAKER;
    }

    @Override
    public void godkjennForVeilederOgDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn) {
        throw new TilgangskontrollException("Deltaker kan ikke godkjenne som veileder");
    }

    @Override
    void opphevGodkjenningerSomAvtalepart() {
        throw new TilgangskontrollException("Deltaker kan ikke oppheve godkjenninger");
    }
}
