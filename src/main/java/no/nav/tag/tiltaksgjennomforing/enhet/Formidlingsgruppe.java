package no.nav.tag.tiltaksgjennomforing.enhet;

public enum Formidlingsgruppe {
    IKKE_ARBEIDSSOKER("IARBS"), // Person er ikke tilgjengelig for søk etter arbeidskraft
    INAKTIVERT_JOBBSKIFTER("IJOBS"); // Jobbskifter som er inaktivert fra nav.no

    private final String formidlingskode;

    Formidlingsgruppe(String formidlingskode) { this.formidlingskode = formidlingskode; }

    public String getKode() {
        return formidlingskode;
    }
}
