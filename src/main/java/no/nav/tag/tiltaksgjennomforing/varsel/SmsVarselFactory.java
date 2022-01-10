package no.nav.tag.tiltaksgjennomforing.varsel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.jetbrains.annotations.NotNull;

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

    public List<SmsVarsel> arbeidsgiverRefusjonKlar() {
        String smsTekst = refusjonTekst(avtale.getTiltakstype(), avtale.getAvtaleNr());
        ArrayList<SmsVarsel> hovedkontaktForAvtalen = hentSmsVarselForHovedKontakt(smsTekst);
        return hentSMSVarselForRefusjonHvisValgt(avtale, hovedkontaktForAvtalen, smsTekst, hendelse);
    }

    @NotNull
    private ArrayList<SmsVarsel> hentSmsVarselForHovedKontakt(String melding) {
        ArrayList<SmsVarsel> hovedkontaktForAvtalen = new ArrayList<>(Arrays.asList(SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(),
            melding, hendelse.getId())));
        return hovedkontaktForAvtalen;
    }


    public List<SmsVarsel> arbeidsgiverRefusjonKlarRevarsel() {
        ArrayList<SmsVarsel> hovedkontaktForAvtalen = new ArrayList<>(Arrays.asList(SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(),
            refusjonTekstRevarsel(avtale.getTiltakstype(), avtale.getAvtaleNr()), hendelse.getId())));

        return hentSMSVarselForRefusjonHvisValgt(avtale, hovedkontaktForAvtalen, refusjonTekstRevarsel(avtale.getTiltakstype(), avtale.getAvtaleNr()), hendelse);
    }

    public List<SmsVarsel> arbeidsgiverRefusjonForlengetVarsel() {
        ArrayList<SmsVarsel> hovedkontaktForAvtalen = new ArrayList<>(Arrays.asList(SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(),
            refusjonForlengetTekst(avtale.getTiltakstype(), avtale.getAvtaleNr()), hendelse.getId())));

        return hentSMSVarselForRefusjonHvisValgt(avtale, hovedkontaktForAvtalen, refusjonForlengetTekst(avtale.getTiltakstype(), avtale.getAvtaleNr()), hendelse);
    }

    public List<SmsVarsel> arbeidsgiverRefusjonKorrigertVarsel() {
        ArrayList<SmsVarsel> hovedkontaktForAvtalen = new ArrayList<>(Arrays.asList(SmsVarsel.nyttVarsel(avtale.getArbeidsgiverTlf(), avtale.getBedriftNr(),
            refusjonTekstKorrigert(avtale.getTiltakstype(), avtale.getAvtaleNr()), hendelse.getId())));

        return hentSMSVarselForRefusjonHvisValgt(avtale, hovedkontaktForAvtalen, refusjonTekstKorrigert(avtale.getTiltakstype(), avtale.getAvtaleNr()), hendelse);
    }

    public SmsVarsel veileder() {
        return SmsVarsel.nyttVarsel(avtale.getVeilederTlf(), NAV_ORGNR, FAGSYSTEMSONE_VARSELTEKST, hendelse.getId());
    }

    private List<SmsVarsel> hentSMSVarselForRefusjonHvisValgt(Avtale avtale, List<SmsVarsel> smsVarsel, String avtale1, VarslbarHendelse hendelse) {
        if(avtale.gjeldendeInnhold().getRefusjonKontaktperson() != null) {
            if(!avtale.gjeldendeInnhold().isØnskerInformasjonOmRefusjon()) {
                smsVarsel.clear();
            }
            smsVarsel.add(SmsVarsel.nyttVarsel(avtale.gjeldendeInnhold().getRefusjonKontaktperson().getRefusjonKontaktpersonTlf(), avtale.getBedriftNr(),
                avtale1, hendelse.getId()));
        }
        return smsVarsel;
    }

    private static String refusjonForlengetTekst(Tiltakstype tiltakstype, Integer avtaleNr) {
        switch (tiltakstype) {
            case SOMMERJOBB:
                return String.format("Fristen for å godkjenne refusjon for avtale med nr: %s har blitt forlenget. Du kan sjekke fristen og søke om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtaleNr);
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
                return String.format("Fristen nærmer seg for å søke om refusjon for tilskudd til sommerjobb for avtale med nr: %s. Søk om refusjon her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtaleNr);
            default:
                throw new RuntimeException();
        }
    }

    private static String refusjonTekstKorrigert(Tiltakstype tiltakstype, Integer avtaleNr) {
        switch (tiltakstype) {
            case SOMMERJOBB:
                return String.format("Tidligere innsendt refusjon på avtale med nr %d er korrigert. Se detaljer her: https://tiltak-refusjon.nav.no. Hilsen NAV.", avtaleNr);
            default:
                throw new RuntimeException();
        }
    }
}
