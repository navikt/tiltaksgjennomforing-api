package no.nav.tag.tiltaksgjennomforing.persondata.aktsomhet;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.Diskresjonskode;

public record Aktsomhet(
    boolean kreverAktsomhet,
    Diskresjonskode diskresjonskode
) {
    public static Aktsomhet intern(Diskresjonskode diskresjonskode) {
        return new Aktsomhet(
            diskresjonskode.erKode6Eller7(),
            diskresjonskode
        );
    }

    public static Aktsomhet ekstern(Diskresjonskode diskresjonskode) {
        return new Aktsomhet(
            diskresjonskode.erKode6Eller7(),
            null
        );
    }

    public static Aktsomhet tom() {
        return new Aktsomhet(
            false,
            null
        );
    }
}
