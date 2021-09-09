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
        return SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(), refusjonTekst(avtale.getTiltakstype(), avtale.getAvtaleNr()), hendelse.getId());
    }

    public SmsVarsel arbeidsgiverRefusjonKlarRevarsel() {
        return SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(), refusjonTekstRevarsel(avtale.getTiltakstype(), avtale.getAvtaleNr()), hendelse.getId());
    }

    public SmsVarsel arbeidsgiverRefusjonForlengetVarsel() {
        return SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(), refusjonForlengetTekst(avtale.getTiltakstype(), avtale.getAvtaleNr()), hendelse.getId());
    }

    public SmsVarsel veileder() {
        return SmsVarsel.nyttVarsel(avtale.getVeilederTlf(), NAV_ORGNR, FAGSYSTEMSONE_VARSELTEKST, hendelse.getId());
    }

    private static String refusjonForlengetTekst(Tiltakstype tiltakstype, Integer avtaleNr) {
        switch (tiltakstype) {
            case SOMMERJOBB:
                return String.format("Fristen for å godkjenne refusjon for avtale med nr: %s. har blitt forlenget. Du kan sjekke fristen og søke om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtaleNr);
            default:
                throw new RuntimeException();
        }
    }

    private static String refusjonTekst(Tiltakstype tiltakstype, Integer avtaleNr) {
        switch (tiltakstype) {
            case SOMMERJOBB:
                return String.format("Dere kan nå søke om refusjon for tilskudd til sommerjobb for avtale med nr: %s. Frist for å søke er om to måneder. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtaleNr);
            default:
                throw new RuntimeException();
        }
    }

    private static String refusjonTekstRevarsel(Tiltakstype tiltakstype, Integer avtaleNr) {
        switch (tiltakstype) {
            case SOMMERJOBB:
                return String.format("Fristen nærmer seg for å søke om refusjon for tilskudd til sommerjobb for avtale med nr: %d. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtaleNr);
            default:
                throw new RuntimeException();
        }
    }
}
