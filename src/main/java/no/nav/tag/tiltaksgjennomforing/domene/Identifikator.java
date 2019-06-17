package no.nav.tag.tiltaksgjennomforing.domene;

import com.fasterxml.jackson.annotation.JsonValue;

public class Identifikator {
    private final String verdi;

    public Identifikator(String verdi) {
        this.verdi = verdi;
    }

    @JsonValue
    public String asString() {
        return verdi;
    }
}
