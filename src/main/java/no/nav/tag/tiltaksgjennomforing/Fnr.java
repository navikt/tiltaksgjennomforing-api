package no.nav.tag.tiltaksgjennomforing;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Fnr {

    private final String fnr;

    public Fnr(String fnr) {
        if (!inneholderKun11Tall(fnr)) {
            throw new TiltaksgjennomforingException("Ugyldig fødselsnummer. Må bestå av 11 tegn.");
        }
        this.fnr = fnr;
    }

    private boolean inneholderKun11Tall(String fnr) {
        return fnr.matches("^[0-9]{11}$");
    }

    @JsonValue
    public String getFnr() {
        return fnr;
    }
}
