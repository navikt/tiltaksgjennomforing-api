package no.nav.tag.tiltaksgjennomforing.datadeling;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;

public enum AvtaleHendelseUtførtAvRolle {
    VEILEDER, ARBEIDSGIVER, SYSTEM, DELTAKER, MENTOR, BESLUTTER;

    public static AvtaleHendelseUtførtAvRolle fraAvtalerolle(Avtalerolle avtalerolle) {
        return switch (avtalerolle) {
            case VEILEDER -> VEILEDER;
            case ARBEIDSGIVER -> ARBEIDSGIVER;
            case BESLUTTER -> BESLUTTER;
            case DELTAKER -> DELTAKER;
            case MENTOR -> MENTOR;
        };
    }

    public boolean erInternBruker() {
        return this == VEILEDER || this == BESLUTTER;
    }
}
