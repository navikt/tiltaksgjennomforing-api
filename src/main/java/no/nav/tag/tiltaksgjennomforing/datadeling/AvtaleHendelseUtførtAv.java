package no.nav.tag.tiltaksgjennomforing.datadeling;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;

public record AvtaleHendelseUtførtAv(
    Rolle rolle,
    Identifikator identifikator
) {
    public enum Rolle {
        VEILEDER, ARBEIDSGIVER, SYSTEM, DELTAKER, MENTOR, BESLUTTER;

        public boolean erInternBruker() {
            return this == VEILEDER || this == BESLUTTER;
        }

        public boolean erBeslutter() {
            return this == BESLUTTER;
        }

        public static Rolle fra(Avtalerolle avtalerolle) {
            return switch (avtalerolle) {
                case VEILEDER -> VEILEDER;
                case ARBEIDSGIVER -> ARBEIDSGIVER;
                case BESLUTTER -> BESLUTTER;
                case DELTAKER -> DELTAKER;
                case MENTOR -> MENTOR;
            };
        }
    }

    public static AvtaleHendelseUtførtAv system(Identifikator identifikator) {
        return new AvtaleHendelseUtførtAv(Rolle.SYSTEM, identifikator);
    }

    public static AvtaleHendelseUtførtAv arbeidsgiver(Avtale avtale) {
        return new AvtaleHendelseUtførtAv(Rolle.ARBEIDSGIVER, avtale.getBedriftNr());
    }

    public static AvtaleHendelseUtførtAv beslutter(Identifikator identifikator) {
        return new AvtaleHendelseUtførtAv(Rolle.BESLUTTER, identifikator);
    }

    public static AvtaleHendelseUtførtAv deltaker(Identifikator identifikator) {
        return new AvtaleHendelseUtførtAv(Rolle.DELTAKER, identifikator);
    }

    public static AvtaleHendelseUtførtAv mentor(Identifikator identifikator) {
        return new AvtaleHendelseUtførtAv(Rolle.MENTOR, identifikator);
    }

    public static AvtaleHendelseUtførtAv veileder(Identifikator identifikator) {
        return new AvtaleHendelseUtførtAv(Rolle.VEILEDER, identifikator);
    }

    public static AvtaleHendelseUtførtAv fra(Avtale avtale, Identifikator identifikator, Rolle rolle) {
        return switch (rolle) {
            case ARBEIDSGIVER -> AvtaleHendelseUtførtAv.arbeidsgiver(avtale);
            case BESLUTTER -> AvtaleHendelseUtførtAv.beslutter(identifikator);
            case SYSTEM -> AvtaleHendelseUtførtAv.system(identifikator);
            case DELTAKER -> AvtaleHendelseUtførtAv.deltaker(identifikator);
            case MENTOR -> AvtaleHendelseUtførtAv.mentor(identifikator);
            case VEILEDER -> AvtaleHendelseUtførtAv.veileder(identifikator);
        };
    }
}
