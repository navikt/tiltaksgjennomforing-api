package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;

public class Fnr extends Identifikator {

    public Fnr(String fnr) {
        super(fnr);
        if (!erGyldigFnr(fnr)) {
            throw new TiltaksgjennomforingException("Ugyldig fødselsnummer. Må bestå av 11 tegn.");
        }
    }

    public static boolean erGyldigFnr(String fnr) {
        return fnr.matches("^[0-9]{11}$");
    }
}
