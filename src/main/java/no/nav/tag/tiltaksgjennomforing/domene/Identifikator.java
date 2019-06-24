package no.nav.tag.tiltaksgjennomforing.domene;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@EqualsAndHashCode
@ToString
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
