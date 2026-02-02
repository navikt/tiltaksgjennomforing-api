package no.nav.tag.tiltaksgjennomforing.avtale;

public enum MentorValgtLonnstype {
    ÅRSLØNN, MÅNEDSLØNN, UKELØNN, DAGSLØNN, TIMELØNN;

    public boolean erTimelonn() {
        return this == TIMELØNN;
    }
}
