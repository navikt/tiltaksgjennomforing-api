package no.nav.tag.tiltaksgjennomforing.varsel;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;

public class SmsVarselFactory {
    public static final BedriftNr NAV_ORGNR = new BedriftNr("889640782");
    private static final String SELVBETJENINGSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing";
    private static final String FAGSYSTEMSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing";

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
        return SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(), refusjonTekst(avtale.getTiltakstype()), hendelse.getId());
    }

    public SmsVarsel veileder() {
        return SmsVarsel.nyttVarsel(avtale.getVeilederTlf(), NAV_ORGNR, FAGSYSTEMSONE_VARSELTEKST, hendelse.getId());
    }

    private String refusjonTekst(Tiltakstype tiltakstype) {
        switch (tiltakstype) {
            case SOMMERJOBB:
                return "Dere kan nå søke om refusjon for tilskudd til sommerjobb. Frist for å søke er to måneder etter tiltaket er avsluttet. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.";
            case MIDLERTIDIG_LONNSTILSKUDD:
            case VARIG_LONNSTILSKUDD:
                return "Dere kan nå søke om refusjon for lønnstilskudd på https://tiltak-refusjon.nav.no";
            default:
                throw new RuntimeException();
        }

    }
}
