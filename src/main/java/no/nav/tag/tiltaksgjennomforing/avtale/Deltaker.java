package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

import java.time.LocalDate;

public class Deltaker extends Avtalepart<Fnr> {

    public Deltaker(Fnr identifikator, Avtale avtale) {
        super(identifikator, avtale);
    }

    static String tekstHeaderAvtalePaabegynt = "Avtale påbegynt";
    static String tekstAvtalePaabegynt = "Du kan først godkjenne avtalen når arbeidsgiveren og veilederen har fylt ut avtalen.";
    static String tekstAvtaleVenterPaaDinGodkjenning = "Les hele avtalen først. Du kan ikke endre teksten i avtalen. ";
    static String ekstraTekstAvtaleVenterPaaDinGodkjenning = "Hvis du er uenig i innholdet, eller har spørsmål til avtalen, må du kontakte din veilederen din via Aktivitetsplanen før du godkjenner.";
    static String tekstTiltaketErAvsluttet = "Hvis du har spørsmål må du kontakte veilederen din.";

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
        switch (Status.valueOf(avtale.statusSomEnum().name())) {
            case AVBRUTT:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleAvbrutt, tekstAvtaleAvbrutt, "");
                break;
            case PÅBEGYNT:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtalePaabegynt, tekstAvtalePaabegynt, "");
                break;
            case MANGLER_GODKJENNING:
                if (avtale.erGodkjentAvDeltaker())
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderVentAndreGodkjenning, "", "");
                else
                    avtaleStatusDetaljer.setInnloggetBrukerStatus(
                            tekstHeaderAvtaleVenterPaaDinGodkjenning, tekstAvtaleVenterPaaDinGodkjenning, ekstraTekstAvtaleVenterPaaDinGodkjenning);
                break;
            case KLAR_FOR_OPPSTART:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(
                        tekstHeaderAvtaleErGodkjentAvAllePartner, tekstAvtaleErGodkjentAvAllePartner + avtale.getStartDato().format(formatter), "");
                break;
            case GJENNOMFØRES:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleGjennomfores, " ", "");
                break;
            case AVSLUTTET:
                avtaleStatusDetaljer.setInnloggetBrukerStatus(tekstHeaderAvtaleErAvsluttet, tekstTiltaketErAvsluttet, "");
                break;
        }

        avtaleStatusDetaljer.setPart1Detaljer((avtale.getBedriftNavn() != null && !avtale.getBedriftNavn().trim().equals("") ? avtale.getBedriftNavn() : "Arbeidsgiver")
                + (avtale.erGodkjentAvArbeidsgiver() ? " har godkjent" : " har ikke godkjent"), avtale.erGodkjentAvArbeidsgiver());
        avtaleStatusDetaljer.setPart2Detaljer((avtale.getVeilederFornavn() != null && !avtale.getVeilederFornavn().trim().equals("") ? avtale.getVeilederFornavn() : "Veileder") + " "
                + (avtale.getVeilederEtternavn() != null && !avtale.getVeilederEtternavn().trim().equals("") ? avtale.getVeilederEtternavn() + " " : "")
                + (avtale.erGodkjentAvVeileder() ? "har godkjent" : (avtale.erGodkjentAvDeltaker() ? "har ikke godkjent" : "venter på din godkjenning")), avtale.erGodkjentAvVeileder());
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

    @Override
    public void låsOppAvtale() {
        throw new TilgangskontrollException("Deltaker kan ikke låse opp avtale");
    }
}
