package no.nav.tag.tiltaksgjennomforing;

import lombok.Getter;

@Getter
public class Fnr {

    private final String fnr;

    public Fnr(String fnr) {
        if (fnr.length() != 11 || !inneholderKunTall(fnr))
            throw new TiltaksgjennomforingException("Ugyldig fødselsnummer");
        this.fnr = fnr;
    }

    private boolean inneholderKunTall(String fnr) {
        return fnr.matches("[0-9]+");
    }
}
