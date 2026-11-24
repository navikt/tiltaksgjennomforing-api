package no.nav.tag.tiltaksgjennomforing.avtale;

public enum TilskuddPeriodeStatus {
    ANNULLERT, GODKJENT, AVSLÅTT, UBEHANDLET, BEHANDLET_I_ARENA;

    public boolean erUbehandletEllerGodkjent() {
        return this == UBEHANDLET || this == GODKJENT;
    }

    public boolean erGodkjent() {
        return this == GODKJENT;
    }
}
