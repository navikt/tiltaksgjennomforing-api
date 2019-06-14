package no.nav.tag.tiltaksgjennomforing.domene;

import java.util.ArrayList;

public class VarslbarHendelseFactory {
    private static final BedriftNr NAV_ORGNR = new BedriftNr("889640782");
    private static final String SELVBETJENINGSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing";
    private static final String FAGSYSTEMSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing";

    private static Varsel veilederVarsel(Avtale avtale) {
        return new Varsel(NAV_ORGNR, avtale.getVeilederTlf(), FAGSYSTEMSONE_VARSELTEKST);
    }

    private static Varsel arbeidsgiverVarsel(Avtale avtale) {
        return new Varsel(avtale.getBedriftNr(), avtale.getArbeidsgiverTlf(), SELVBETJENINGSONE_VARSELTEKST);
    }

    private static Varsel deltakerVarsel(Avtale avtale) {
        return new Varsel(avtale.getDeltakerFnr(), null, SELVBETJENINGSONE_VARSELTEKST);
    }

    private static VarslbarHendelse varslerFor(Avtale avtale, boolean deltaker, boolean arbeidsgiver, boolean veileder) {
        var varsler = new ArrayList<Varsel>();
        if (deltaker) {
            varsler.add(deltakerVarsel(avtale));
        }
        if (arbeidsgiver) {
            varsler.add(arbeidsgiverVarsel(avtale));
        }
        if (veileder) {
            varsler.add(veilederVarsel(avtale));
        }
        return new VarslbarHendelse(avtale.getId(), varsler);
    }

    public static VarslbarHendelse avtaleOpprettet(Avtale avtale) {
        return varslerFor(avtale, true, true, false);
    }

    public static VarslbarHendelse avtaleGodkjentAvDeltaker(Avtale avtale) {
        return varslerFor(avtale, false, false, true);
    }

    public static VarslbarHendelse avtaleGodkjentAvArbeidsgiver(Avtale avtale) {
        return varslerFor(avtale, false, false, true);
    }

    public static VarslbarHendelse avtaleGodkjentAvVeileder(Avtale avtale) {
        return varslerFor(avtale, true, true, false);
    }

    public static VarslbarHendelse godkjenningerOpphevet(Avtale avtale) {
        return varslerFor(avtale, true, true, true);
    }
}
