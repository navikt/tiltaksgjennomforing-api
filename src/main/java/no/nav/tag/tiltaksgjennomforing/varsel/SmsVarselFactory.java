package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;

public class SmsVarselFactory {
    public static final BedriftNr NAV_ORGNR = new BedriftNr("889640782");
    private static final String SELVBETJENINGSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing";
    private static final String FAGSYSTEMSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing";
    private static final String ARBEIDSGIVER_REFUSJON_KLAR_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksrefusjon";

    private final Avtale avtale;
    private final VarslbarHendelse hendelse;

    public SmsVarselFactory(Avtale avtale, VarslbarHendelse hendelse) {
        this.avtale = avtale;
        this.hendelse = hendelse;
    }

    public SmsVarsel deltaker() {
        return SmsVarsel.nyttVarsel(avtale.getDeltakerTlf(), avtale.getDeltakerFnr(), SELVBETJENINGSONE_VARSELTEKST, hendelse.getId());
    }

    public SmsVarsel arbeidsgiver() {
        return SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(), SELVBETJENINGSONE_VARSELTEKST, hendelse.getId());
    }

    public SmsVarsel arbeidsgiverRefusjonKlar() {
        return SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(), ARBEIDSGIVER_REFUSJON_KLAR_VARSELTEKST, hendelse.getId());
    }

    public SmsVarsel veileder() {
        return SmsVarsel.nyttVarsel(avtale.getVeilederTlf(), NAV_ORGNR, FAGSYSTEMSONE_VARSELTEKST, hendelse.getId());
    }
}
