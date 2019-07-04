package no.nav.tag.tiltaksgjennomforing.domene.varsel;

import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.BedriftNr;

public class SmsVarselFactory {
    static final BedriftNr NAV_ORGNR = new BedriftNr("889640782");
    private static final String SELVBETJENINGSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing";
    private static final String FAGSYSTEMSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing";

    private final Avtale avtale;

    public SmsVarselFactory(Avtale avtale) {
        this.avtale = avtale;
    }

    public SmsVarsel deltaker() {
        return SmsVarsel.nyttVarsel(avtale.getDeltakerTlf(), avtale.getDeltakerFnr(), SELVBETJENINGSONE_VARSELTEKST);
    }

    public SmsVarsel arbeidsgiver() {
        return SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(), SELVBETJENINGSONE_VARSELTEKST);
    }

    public SmsVarsel veileder() {
        return SmsVarsel.nyttVarsel(avtale.getVeilederTlf(), NAV_ORGNR, FAGSYSTEMSONE_VARSELTEKST);
    }
}
