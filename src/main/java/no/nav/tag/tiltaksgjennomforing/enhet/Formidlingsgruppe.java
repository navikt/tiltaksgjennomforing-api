package no.nav.tag.tiltaksgjennomforing.enhet;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Formidlingsgruppe {
    ARBEIDSSOKER("ARBS"),            // Person er tilgjengelig for alt søk etter   arbeidskraft, ordinær og vikar
    IKKE_ARBEIDSSOKER("IARBS"),      // Person er ikke tilgjengelig for søk etter arbeidskraft
    INAKTIVERT_JOBBSKIFTER("IJOBS"), // Jobbskifter som er inaktivert fra nav.no
    IKKE_SERVICEBEHOV("ISERV"),      // Inaktivering, person mottar ikke bistand fra NAV
    FRA_NAV_NO("JOBBS"),             // Personen er ikke tilgjengelig for søk
    PRE_ARBEIDSSOKER("PARBS"),       // Personen fra nav.no som ønsker å bli arbeidssøker, men som enda ikke er   verifisert
    PRE_REAKTIVERT_ARBEIDSSOKER("RARBS"); //Person som er reaktivert fra nav.no
    private final String formidlingskode;

    Formidlingsgruppe(String formidlingskode) { this.formidlingskode = formidlingskode; }

    @JsonValue
    public String getKode() {
        return formidlingskode;
    }

    public static Formidlingsgruppe parse(String kode) {
        return Arrays.stream(Formidlingsgruppe.values())
            .filter(gruppe -> gruppe.getKode().equals(kode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ukjent formidlingsgruppe: " + kode));
    }
}
