package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.ArrayList;

@UtilityClass
public class VarslbarHendelseFactory {
    static final BedriftNr NAV_ORGNR = new BedriftNr("889640782");
    private static final String SELVBETJENINGSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nav.no/tiltaksgjennomforing";
    private static final String FAGSYSTEMSONE_VARSELTEKST = "Du har mottatt et nytt varsel på https://arbeidsgiver.nais.adeo.no/tiltaksgjennomforing";

    private static Varsel veilederVarsel(Avtale avtale) {
        return new Varsel(NAV_ORGNR, avtale.getVeilederTlf(), FAGSYSTEMSONE_VARSELTEKST);
    }

    private static Varsel arbeidsgiverVarsel(Avtale avtale) {
        return new Varsel(avtale.getBedriftNr(), avtale.getArbeidsgiverTlf(), SELVBETJENINGSONE_VARSELTEKST);
    }

    private static Varsel deltakerVarsel(Avtale avtale) {
        return new Varsel(avtale.getDeltakerFnr(), avtale.getDeltakerTlf(), SELVBETJENINGSONE_VARSELTEKST);
    }

    private static VarslbarHendelse lagVarslbarHendelse(Avtale avtale, String hendelse, boolean deltaker, boolean arbeidsgiver, boolean veileder) {
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
        return new VarslbarHendelse(LocalDateTime.now(), avtale.getId(), hendelse, varsler);
    }

    public static VarslbarHendelse avtaleOpprettet(Avtale avtale) {
        return lagVarslbarHendelse(avtale, "Avtale opprettet av NAV-veileder", true, true, false);
    }

    public static VarslbarHendelse avtaleGodkjentAvDeltaker(Avtale avtale) {
        return lagVarslbarHendelse(avtale, "Avtale godkjent av deltaker", false, false, true);
    }

    public static VarslbarHendelse avtaleGodkjentAvArbeidsgiver(Avtale avtale) {
        return lagVarslbarHendelse(avtale, "Avtale godkjent av arbeidsgiver", false, false, true);
    }

    public static VarslbarHendelse avtaleGodkjentAvVeileder(Avtale avtale) {
        return lagVarslbarHendelse(avtale, "Avtale godkjent av NAV-veileder", true, true, false);
    }

    public static VarslbarHendelse avtaleGodkjentPaVegneAv(Avtale avtale) {
        return lagVarslbarHendelse(avtale, "Avtale godkjent av NAV-veileder", false, true, false);
    }

    public static VarslbarHendelse godkjenningerOpphevet(Avtale avtale) {
        return lagVarslbarHendelse(avtale, "Avtalens godkjenninger opphevet", true, true, true);
    }
}
