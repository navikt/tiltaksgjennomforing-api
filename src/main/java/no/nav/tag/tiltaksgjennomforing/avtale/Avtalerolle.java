package no.nav.tag.tiltaksgjennomforing.avtale;

public enum Avtalerolle {
    DELTAKER, MENTOR, ARBEIDSGIVER, VEILEDER, BESLUTTER;

    public boolean erInternBruker() {
        return this == VEILEDER || this == BESLUTTER;
    }

    public boolean erBeslutter() {
        return this == BESLUTTER;
    }
}
