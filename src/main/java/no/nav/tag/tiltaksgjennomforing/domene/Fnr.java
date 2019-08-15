package no.nav.tag.tiltaksgjennomforing.domene;

import static no.bekk.bekkopen.person.FodselsnummerValidator.isValid;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;

@Value
public class Fnr implements Identifikator {

    private final String fnr;

    public Fnr(String fnr) {
        if (!erGyldigFnr(fnr)) {
            throw new TiltaksgjennomforingException("Ugyldig fødselsnummer.");
        }
        this.fnr = fnr;
    }

    public static boolean erGyldigFnr(String fnr) {
        return isValid(fnr);
    }

    @JsonValue
    @Override
    public String asString() {
        return fnr;
    }
}
