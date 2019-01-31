package no.nav.tag.tiltaksgjennomforing.domene;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;

@Value
public class Fnr implements Identifikator {

    private final String fnr;

    public Fnr(String fnr) {
        if (!erGyldigFnr(fnr)) {
            throw new TiltaksgjennomforingException("Ugyldig fødselsnummer. Må bestå av 11 tegn.");
        }
        this.fnr = fnr;
    }

    public static boolean erGyldigFnr(String fnr) {
        return fnr.matches("^[0-9]{11}$");
    }

    @JsonValue
    @Override
    public String asString() {
        return fnr;
    }
}
