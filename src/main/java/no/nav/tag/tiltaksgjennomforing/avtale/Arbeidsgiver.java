package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

public class Arbeidsgiver extends Avtalepart<Fnr> {
    public Arbeidsgiver(Fnr identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    static String tekstAvtaleErIkkkeFyltUt = "Som arbeidsgiver kan du fylle ut avtalen i samarbeid med Nav/veileder. Avtalen kan godkjennes etter at den er fylt ut.\n mer tekst";

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
        if (avtale.heleAvtalenErFyltUt()) {
            if (avtale.erGodkjentAvArbeidsgiver()) {
                avtaleStatusDetaljer.header = tekstHeaderAvtaleErGodkjentAvInnloggetBruker;
                if (avtale.erGodkjentAvVeileder()) {
                    avtaleStatusDetaljer.infoDel1 = tekstAvtaleErGodkjentAvAllePartner;
                    avtaleStatusDetaljer.infoDel2 = ekstraTekstAvtaleErGodkjentAvAllePartner;
                } else if (avtale.erGodkjentAvDeltaker()) {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderAvtaleErGodkjentAvInnloggetBruker, tekstAvtaleVenterPaaVeilederGodkjenning, ekstraTekstAvtaleVenterPaaVeilederGodkjenning);
                } else {
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderAvtaleErGodkjentAvInnloggetBruker, tekstAvtaleVenterPaaAndrepartnerGodkjenning, ekstraTekstAvtaleVenterPaaAndrePartnerGodkjenning);
                }
            } else {
                avtaleStatusDetaljer.setInnloggetBrukerStatus(
                        tekstHeaderAvtaleVenterPaaDinGodkjenning, tekstAvtaleVenterPaaDinGodkjenning, ekstraTekstAvtaleVenterPaaDinGodkjenning);
                //"Hele avtalen er nå fylt ut og klar for godkjenning av deg. Les hele avtalen først. Hvis du er uenig i innholdet, eller har spørsmål til avtalen, bør du kontakte din veileder via Aktivitetsplanen før du godkjenner.";
            }
        } else {
            avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErIkkkeFyltUt, tekstAvtaleErIkkkeFyltUt, "");
        }
        avtaleStatusDetaljer.setPart1Detaljer(avtale.getDeltakerFornavn() + " " + avtale.getDeltakerEtternavn(), avtale.erGodkjentAvDeltaker());
        avtaleStatusDetaljer.setPart2Detaljer(avtale.getVeilederFornavn() + " " + avtale.getVeilederEtternavn(), avtale.erGodkjentAvVeileder());
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
